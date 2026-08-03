-- Rejoining starts a fresh billing period. Pause days remain available for
-- active-tenure reporting but must never move a fee-cycle date.
begin;

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

  if v_student.rejoined_at is not null and not v_has_payment_after_rejoin then
    v_paid_through := v_student.rejoined_at;
  end if;

  return v_paid_through;
end;
$$;

commit;
