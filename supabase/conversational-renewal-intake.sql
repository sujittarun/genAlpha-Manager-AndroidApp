-- Existing-player renewal support for the conversational intake pipeline.
-- Model output remains a draft. Only explicit staff confirmation calls the
-- transaction below to create a payment and advance the player's renewal.

begin;

alter table public.admission_intake_sessions
  add column if not exists intake_type text not null default 'admission',
  add column if not exists matched_student_id uuid references public.students(id) on delete set null,
  add column if not exists matched_student_snapshot jsonb not null default '{}'::jsonb,
  add column if not exists renewal_payment_id uuid references public.student_payments(id) on delete set null;

alter table public.admission_intake_sessions
  drop constraint if exists admission_intake_sessions_intake_type_check;

alter table public.admission_intake_sessions
  add constraint admission_intake_sessions_intake_type_check
  check (intake_type in ('admission', 'renewal', 'unknown'));

alter table public.student_payments
  add column if not exists payment_reference text not null default '',
  add column if not exists intake_session_id uuid references public.admission_intake_sessions(id) on delete set null,
  add column if not exists verification_status text not null default 'manager_confirmed';

create unique index if not exists student_payments_intake_session_unique
on public.student_payments (intake_session_id)
where intake_session_id is not null;

create unique index if not exists student_payments_reference_unique
on public.student_payments (lower(btrim(payment_reference)))
where btrim(payment_reference) <> '';

create index if not exists admission_intake_sessions_matched_student_idx
on public.admission_intake_sessions (matched_student_id, created_at desc)
where matched_student_id is not null;

create or replace function public.student_paid_through_date(p_student_id uuid)
returns date
language plpgsql
stable
security definer
set search_path = public
as $$
declare
  v_student public.students%rowtype;
  v_paid_through date;
  v_candidate date;
  v_renewal date;
  v_initial_months integer;
  v_fee_amount numeric;
  v_without_admission numeric;
  v_rounded_amount integer;
  v_special_candidate integer;
  v_has_payment_after_rejoin boolean := false;
begin
  select * into v_student from public.students where id = p_student_id;
  if not found then raise exception 'Player not found.'; end if;

  -- Mirror the web and Android paid-through logic. The current fee_plan can
  -- change on later renewals, so initial coverage is inferred from the original
  -- joining amount after removing jersey revenue, not from the latest plan.
  v_fee_amount := greatest(coalesce(v_student.amount_paid, 0) -
    (greatest(coalesce(v_student.jersey_pairs, 0), 0) * 750), 0);
  v_without_admission := greatest(v_fee_amount - 500, 0);
  v_rounded_amount := round(v_fee_amount)::integer;
  v_initial_months := 1;
  if lower(coalesce(v_student.fee_plan, '')) = 'special' then
    for v_special_candidate in 1..36 loop
      if round(10000 * v_special_candidate *
        (case when v_special_candidate >= 6 then 0.90
              when v_special_candidate >= 3 then 0.95 else 1 end))::integer = v_rounded_amount then
        v_initial_months := v_special_candidate;
        exit;
      end if;
    end loop;
    if v_initial_months = 1 and v_rounded_amount <> 10000 then
      v_initial_months := greatest(round(v_rounded_amount /
        (case when v_rounded_amount >= 54000 then 9000.0
              when v_rounded_amount >= 28500 then 9500.0 else 10000.0 end))::integer, 1);
    end if;
  elsif v_rounded_amount = 10000 then
    v_initial_months := 1;
  elsif v_without_admission >= 18900
     or v_rounded_amount in (18900, 19400, 20000, 20500, 21000) then
    v_initial_months := 6;
  elsif v_rounded_amount in (9000, 9500, 9975, 10475, 10500, 11000)
     or v_without_admission between 9000 and 10500 then
    v_initial_months := 3;
  end if;
  v_paid_through := case when v_student.fees_paid
    then (v_student.join_date + make_interval(months => v_initial_months))::date
    else v_student.join_date
  end;

  foreach v_renewal in array coalesce(v_student.renewals, '{}'::date[]) loop
    v_paid_through := greatest(v_paid_through, (v_renewal + interval '1 month')::date);
  end loop;

  select max((p.cycle_start_date + make_interval(months => greatest(p.months_covered, 1)))::date)
  into v_candidate
  from public.student_payments p
  where p.student_id = p_student_id
    and p.payment_type in ('joining', 'renewal');
  v_paid_through := greatest(v_paid_through, coalesce(v_candidate, v_paid_through));

  if v_student.rejoined_at is not null then
    select exists(
      select 1 from public.student_payments p
      where p.student_id = p_student_id
        and p.payment_type = 'renewal'
        and p.cycle_start_date >= v_student.rejoined_at
    ) into v_has_payment_after_rejoin;
  end if;

  if greatest(coalesce(v_student.fee_pause_days, 0), 0) > 0
     and not v_has_payment_after_rejoin then
    v_paid_through := v_paid_through + greatest(v_student.fee_pause_days, 0);
  end if;
  return v_paid_through;
end;
$$;

create or replace function public.finalize_renewal_intake(
  p_session_id uuid,
  p_confirmation_message_id text default '',
  p_confirmed_by text default 'WhatsApp staff'
)
returns table(
  payment_id uuid,
  student_id uuid,
  student_name text,
  reg_no bigint,
  cycle_start_date date,
  renewal_to_date date
)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_session public.admission_intake_sessions%rowtype;
  v_student public.students%rowtype;
  v_draft jsonb;
  v_payment jsonb;
  v_plan text;
  v_months integer;
  v_amount numeric(10, 2);
  v_paid_on date;
  v_cycle_start date;
  v_to_date date;
  v_reference text;
  v_proof_path text;
  v_is_joining boolean;
  v_coaching numeric(10, 2);
  v_admission_fee numeric(10, 2);
  v_jersey numeric(10, 2);
  v_payment_id uuid;
begin
  select * into v_session
  from public.admission_intake_sessions
  where id = p_session_id
  for update;
  if not found then raise exception 'Intake session not found.'; end if;

  if v_session.renewal_payment_id is not null then
    return query
    select p.id, s.id, s.name, s.reg_no, p.cycle_start_date,
           (p.cycle_start_date + make_interval(months => greatest(p.months_covered, 1)))::date
    from public.student_payments p
    join public.students s on s.id = p.student_id
    where p.id = v_session.renewal_payment_id;
    return;
  end if;

  if v_session.intake_type <> 'renewal' then
    raise exception 'This intake is not a renewal.';
  end if;
  if v_session.status not in ('waiting_for_confirmation', 'confirmed') then
    raise exception 'Renewal draft must be waiting for confirmation.';
  end if;
  if cardinality(coalesce(v_session.missing_fields, '{}'::text[])) > 0 then
    raise exception 'Renewal draft still has missing fields.';
  end if;
  if v_session.matched_student_id is null then
    raise exception 'A unique player match is required.';
  end if;

  select * into v_student
  from public.students
  where id = v_session.matched_student_id
  for update;
  if not found then raise exception 'Matched player no longer exists.'; end if;

  v_draft := coalesce(v_session.draft, '{}'::jsonb);
  v_payment := coalesce(v_draft->'payment', '{}'::jsonb);
  v_plan := lower(coalesce(nullif(v_draft->>'plan_type', ''), 'monthly'));
  if v_plan not in ('monthly', 'quarterly', 'halfyearly', 'special', 'custom') then
    raise exception 'Renewal plan is missing or invalid.';
  end if;
  v_months := case v_plan
    when 'monthly' then 1
    when 'quarterly' then 3
    when 'halfyearly' then 6
    else greatest(coalesce(nullif(v_draft->>'months_covered', '')::integer, 1), 1)
  end;
  v_amount := greatest(coalesce(nullif(v_payment->>'amount', '')::numeric, 0), 0);
  v_paid_on := nullif(v_payment->>'payment_date', '')::date;
  v_reference := btrim(coalesce(nullif(v_payment->>'transaction_id', ''), v_payment->>'utr', ''));
  v_proof_path := btrim(coalesce(v_payment->>'proof_path', ''));

  if v_amount <= 0 then raise exception 'Renewal payment amount is required.'; end if;
  if v_paid_on is null then raise exception 'Renewal payment date is required.'; end if;
  if v_reference = '' and v_proof_path = '' then
    raise exception 'A payment reference or screenshot is required.';
  end if;
  if lower(coalesce(v_payment->>'screenshot_status', 'unknown')) in ('failed', 'pending', 'processing') then
    raise exception 'The payment screenshot does not show a completed payment.';
  end if;
  if v_reference <> '' and exists(
    select 1 from public.student_payments p
    where lower(btrim(p.payment_reference)) = lower(v_reference)
  ) then
    raise exception 'This payment reference was already recorded.';
  end if;

  v_is_joining := not v_student.fees_paid;
  v_cycle_start := case when v_is_joining then v_student.join_date else public.student_paid_through_date(v_student.id) end;
  v_to_date := (v_cycle_start + make_interval(months => v_months))::date;
  if exists(
    select 1 from public.student_payments p
    where p.student_id = v_student.id
      and p.payment_type = case when v_is_joining then 'joining' else 'renewal' end
      and p.cycle_start_date = v_cycle_start
  ) then
    raise exception 'This player already has a payment for the current cycle.';
  end if;

  v_admission_fee := case when v_plan = 'special' then 0 else 500 end;
  v_jersey := greatest(coalesce(v_student.jersey_pairs, 0), 0) * 750;
  v_coaching := case v_plan
    when 'monthly' then 3500
    when 'quarterly' then 9975
    when 'halfyearly' then 18900
    when 'special' then round(10000 * v_months * case when v_months >= 6 then 0.90 when v_months >= 3 then 0.95 else 1 end, 2)
    else greatest(v_amount - v_admission_fee - v_jersey, 0)
  end;

  insert into public.student_payments (
    student_id, payment_type, plan_type, cycle_start_date, months_covered,
    amount, paid_on, comment, recorded_by, proof_path, payment_reference,
    intake_session_id, verification_status
  ) values (
    v_student.id, case when v_is_joining then 'joining' else 'renewal' end, v_plan, v_cycle_start, v_months,
    v_amount, v_paid_on,
    concat(case when v_is_joining then 'Confirmed conversational joining payment. ' else 'Confirmed conversational renewal. ' end, coalesce(v_draft->>'comments', '')),
    coalesce(nullif(p_confirmed_by, ''), 'WhatsApp staff'), v_proof_path,
    v_reference, v_session.id, 'staff_confirmed'
  ) returning id into v_payment_id;

  update public.students
  set renewals = case when v_is_joining then public.students.renewals else (
        select array_agg(distinct d order by d)
        from unnest(coalesce(public.students.renewals, '{}'::date[]) || array[v_cycle_start]) d
      ) end,
      fees_paid = case when v_is_joining then true else public.students.fees_paid end,
      amount_paid = case when v_is_joining then v_amount else public.students.amount_paid end,
      payment_status = case when v_is_joining then 'paid' else public.students.payment_status end,
      fee_plan = v_plan,
      coaching_fee = case when v_is_joining then v_coaching else public.students.coaching_fee end,
      admission_fee = case when v_is_joining then v_admission_fee else public.students.admission_fee end,
      jersey_amount = case when v_is_joining then v_jersey else public.students.jersey_amount end,
      total_fee_amount = case when v_is_joining then v_amount else public.students.total_fee_amount end,
      discontinued = false,
      rejoined_at = case when public.students.discontinued then v_paid_on else public.students.rejoined_at end,
      discontinued_at = case when public.students.discontinued then null else public.students.discontinued_at end,
      updated_by = coalesce(nullif(p_confirmed_by, ''), 'WhatsApp staff'),
      updated_at = now()
  where id = v_student.id;

  if v_is_joining then
    update public.admissions
    set fees_paid = true, amount_paid = v_amount, fee_plan = v_plan,
        coaching_fee = v_coaching, admission_fee = v_admission_fee,
        jersey_amount = v_jersey, total_fee_amount = v_amount,
        payment_verification_status = 'verified'
    where id = v_student.admission_id;
  end if;

  update public.admission_intake_sessions
  set status = 'confirmed', renewal_payment_id = v_payment_id,
      confirmation_message_id = coalesce(p_confirmation_message_id, ''),
      confirmed_by = coalesce(nullif(p_confirmed_by, ''), 'WhatsApp staff'),
      confirmed_at = coalesce(confirmed_at, now()), error_code = '', error_message = ''
  where id = v_session.id;

  return query select v_payment_id, v_student.id, v_student.name, v_student.reg_no,
                      v_cycle_start, v_to_date;
end;
$$;

create or replace function public.guard_intake_admission_type()
returns trigger
language plpgsql
set search_path = public
as $$
begin
  if new.intake_session_id is not null and exists(
    select 1 from public.admission_intake_sessions s
    where s.id = new.intake_session_id and s.intake_type <> 'admission'
  ) then
    raise exception 'Only admission intake sessions may create admissions.';
  end if;
  return new;
end;
$$;

drop trigger if exists admissions_guard_intake_type on public.admissions;
create trigger admissions_guard_intake_type
before insert or update of intake_session_id on public.admissions
for each row execute function public.guard_intake_admission_type();

revoke all on function public.student_paid_through_date(uuid) from public, anon, authenticated;
revoke all on function public.finalize_renewal_intake(uuid, text, text) from public, anon, authenticated;
grant execute on function public.student_paid_through_date(uuid) to service_role;
grant execute on function public.finalize_renewal_intake(uuid, text, text) to service_role;

notify pgrst, 'reload schema';

commit;
