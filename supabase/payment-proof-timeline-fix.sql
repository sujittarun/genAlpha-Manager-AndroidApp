-- Incremental migration: add proof_path to student_payments and include it in timeline.
-- Safe to run multiple times.

alter table public.student_payments
add column if not exists proof_path text not null default '';

create or replace function public.log_student_payment_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.student_timeline (
    student_id,
    event_type,
    event_date,
    title,
    details,
    changed_by
  )
  values (
    new.student_id,
    'payment',
    new.paid_on,
    case when new.payment_type = 'renewal' then 'Renewal fee paid' else 'Fee payment recorded' end,
    concat(
      'Rs ', new.amount,
      ' • ', new.plan_type,
      ' • ', new.months_covered, ' month',
      case when new.months_covered = 1 then '' else 's' end,
      ' • cycle from ', new.cycle_start_date,
      case when coalesce(new.proof_path, '') <> '' then concat(' • payment-proofs/', new.proof_path) else '' end
    ),
    new.recorded_by
  );

  return new;
end;
$$;
