-- Incremental migration: richer student life-event timeline.
-- Safe to run multiple times. Attendance is intentionally excluded; it is shown as a calendar in the app.

create table if not exists public.student_timeline (
  id uuid primary key default gen_random_uuid(),
  student_id uuid not null references public.students(id) on delete cascade,
  event_type text not null,
  event_date date not null default current_date,
  title text not null,
  details text not null default '',
  changed_by text not null default 'System',
  created_at timestamptz not null default timezone('utc', now())
);

create index if not exists student_timeline_student_created_idx
on public.student_timeline (student_id, created_at desc);

alter table public.students
  add column if not exists payment_status text not null default '',
  add column if not exists fee_plan text not null default 'monthly',
  add column if not exists coaching_fee numeric(10, 2) not null default 0,
  add column if not exists admission_fee numeric(10, 2) not null default 0,
  add column if not exists jersey_amount numeric(10, 2) not null default 0,
  add column if not exists total_fee_amount numeric(10, 2) not null default 0;

alter table public.student_payments
  add column if not exists proof_path text not null default '';

create or replace function public.log_student_life_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_actor text;
  v_changes text[];
  v_latest_renewal date;
begin
  if tg_op = 'INSERT' then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      'student_created',
      coalesce(new.join_date, current_date),
      'Player record created',
      concat('Joined ', coalesce(new.time_slot, 'slot not set'), '. Fee plan: ', coalesce(nullif(new.fee_plan, ''), 'monthly'), '.'),
      coalesce(nullif(new.added_by, ''), 'System')
    );
    return new;
  end if;

  v_actor := coalesce(nullif(new.updated_by, ''), nullif(old.updated_by, ''), 'System');

  if new.discontinued is distinct from old.discontinued
     or new.discontinued_at is distinct from old.discontinued_at
     or new.rejoined_at is distinct from old.rejoined_at
     or new.fee_pause_days is distinct from old.fee_pause_days then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      case when new.discontinued then 'student_discontinued' else 'student_rejoined' end,
      case
        when new.discontinued then coalesce(new.discontinued_at, current_date)
        else coalesce(new.rejoined_at, current_date)
      end,
      case when new.discontinued then 'Player discontinued' else 'Player marked active' end,
      case
        when new.discontinued then concat('Paused from ', coalesce(new.discontinued_at::text, current_date::text), '.')
        else concat(
          'Rejoined on ',
          coalesce(new.rejoined_at::text, current_date::text),
          '. Billing pause days: ',
          coalesce(new.fee_pause_days, 0),
          '.'
        )
      end,
      v_actor
    );
  end if;

  if new.renewals is distinct from old.renewals then
    v_latest_renewal := new.renewals[array_length(new.renewals, 1)];
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      'renewal_updated',
      current_date,
      'Renewal updated',
      concat('Latest renewal cycle date: ', coalesce(v_latest_renewal::text, 'not set'), '.'),
      v_actor
    );
  end if;

  if new.fees_paid is distinct from old.fees_paid
     or new.amount_paid is distinct from old.amount_paid
     or new.payment_status is distinct from old.payment_status
     or new.payment_reference is distinct from old.payment_reference then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      'fee_status_updated',
      current_date,
      'Fee status updated',
      concat(
        'Status: ', case when new.fees_paid then 'Paid' else 'Not paid' end,
        '. Amount paid: Rs ', coalesce(new.amount_paid, 0),
        case when coalesce(nullif(new.payment_status, ''), '') <> '' then concat('. Payment status: ', new.payment_status) else '' end,
        case when coalesce(nullif(new.payment_reference, ''), '') <> '' then '. Reference saved.' else '' end
      ),
      v_actor
    );
  end if;

  if new.jersey_size is distinct from old.jersey_size
     or new.jersey_pairs is distinct from old.jersey_pairs
     or new.jersey_amount is distinct from old.jersey_amount then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      'jersey_updated',
      current_date,
      'Jersey details updated',
      concat(
        'Size: ', coalesce(nullif(new.jersey_size, ''), 'not set'),
        '. Pairs: ', coalesce(new.jersey_pairs, 0),
        '. Amount: Rs ', coalesce(new.jersey_amount, 0), '.'
      ),
      v_actor
    );
  end if;

  v_changes := array_remove(array[
    case when new.name is distinct from old.name then 'name' end,
    case when new.age is distinct from old.age then 'age' end,
    case when new.time_slot is distinct from old.time_slot then 'time slot' end,
    case when new.join_date is distinct from old.join_date then 'join date' end,
    case when new.father_guardian_name is distinct from old.father_guardian_name then 'guardian' end,
    case when new.parent_contact_no is distinct from old.parent_contact_no then 'parent phone' end,
    case when new.alternate_contact_no is distinct from old.alternate_contact_no then 'alternate phone' end,
    case when new.school_college is distinct from old.school_college then 'school' end,
    case when new.grade is distinct from old.grade then 'grade' end,
    case when new.address is distinct from old.address then 'address' end,
    case when new.comments is distinct from old.comments then 'notes' end,
    case when new.fee_plan is distinct from old.fee_plan then 'fee plan' end,
    case when new.coaching_fee is distinct from old.coaching_fee then 'coaching fee' end,
    case when new.admission_fee is distinct from old.admission_fee then 'admission fee' end,
    case when new.total_fee_amount is distinct from old.total_fee_amount then 'total fee' end
  ], null);

  if array_length(v_changes, 1) is not null then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      'profile_updated',
      current_date,
      'Player details updated',
      concat('Changed: ', array_to_string(v_changes, ', '), '.'),
      v_actor
    );
  end if;

  return new;
end;
$$;

drop trigger if exists students_log_timeline_insert on public.students;
create trigger students_log_timeline_insert
after insert on public.students
for each row
when (new.added_by <> 'Admission Form')
execute function public.log_student_life_timeline();

drop trigger if exists students_log_timeline_update on public.students;
create trigger students_log_timeline_update
after update on public.students
for each row
execute function public.log_student_life_timeline();

create or replace function public.log_student_payment_life_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_row public.student_payments%rowtype;
  v_event_type text;
  v_title text;
  v_actor text;
begin
  if tg_op = 'DELETE' then
    v_row := old;
    v_event_type := 'payment_deleted';
    v_title := case when old.payment_type = 'renewal' then 'Renewal payment deleted' else 'Payment deleted' end;
    v_actor := coalesce(nullif(old.recorded_by, ''), 'System');
  elsif tg_op = 'UPDATE' then
    v_row := new;
    v_event_type := 'payment_updated';
    v_title := case when new.payment_type = 'renewal' then 'Renewal payment updated' else 'Payment updated' end;
    v_actor := coalesce(nullif(new.recorded_by, ''), 'System');
  else
    v_row := new;
    v_event_type := case
      when new.payment_type = 'joining' then 'joining_fee_paid'
      when new.payment_type in ('jersey', 'jersey_refund') then 'jersey_payment'
      else 'renewal_paid'
    end;
    v_title := case
      when new.payment_type = 'joining' then 'Joining fee recorded'
      when new.payment_type = 'renewal' then 'Renewal fee paid'
      when new.payment_type = 'jersey_refund' then 'Jersey refund recorded'
      when new.payment_type = 'jersey' then 'Jersey payment recorded'
      else 'Fee payment recorded'
    end;
    v_actor := coalesce(nullif(new.recorded_by, ''), 'System');
  end if;

  insert into public.student_timeline (
    student_id,
    event_type,
    event_date,
    title,
    details,
    changed_by
  )
  values (
    v_row.student_id,
    v_event_type,
    coalesce(v_row.paid_on, current_date),
    v_title,
    concat(
      'Rs ', coalesce(v_row.amount, 0),
      ' • ', coalesce(nullif(v_row.plan_type, ''), 'plan not set'),
      ' • ', coalesce(v_row.months_covered, 0), ' month',
      case when coalesce(v_row.months_covered, 0) = 1 then '' else 's' end,
      ' • cycle from ', coalesce(v_row.cycle_start_date::text, 'not set'),
      case when coalesce(nullif(v_row.comment, ''), '') <> '' then concat(' • ', v_row.comment) else '' end,
      case when coalesce(nullif(v_row.proof_path, ''), '') <> '' then concat(' • payment-proofs/', v_row.proof_path) else '' end
    ),
    v_actor
  );

  if tg_op = 'DELETE' then
    return old;
  end if;
  return new;
end;
$$;

drop trigger if exists student_payments_log_timeline on public.student_payments;
create trigger student_payments_log_timeline
after insert or update or delete on public.student_payments
for each row
execute function public.log_student_payment_life_timeline();

alter table public.student_timeline enable row level security;

drop policy if exists "student_timeline_authenticated_read" on public.student_timeline;
create policy "student_timeline_authenticated_read"
on public.student_timeline
for select
to authenticated
using (true);

drop policy if exists "student_timeline_authenticated_insert" on public.student_timeline;
create policy "student_timeline_authenticated_insert"
on public.student_timeline
for insert
to authenticated
with check (true);

grant select, insert on public.student_timeline to authenticated;
grant select, insert on public.student_timeline to service_role;
