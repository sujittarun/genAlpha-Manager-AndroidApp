-- Manager confirmation verifies an admission payment claim. It must not
-- recalculate or replace the amount, payment date, proof, plan, or fee split
-- that were already saved from the admission and payment evidence.
begin;

create or replace function public.preserve_admission_claim_payment_values()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_claim public.admission_payment_claims%rowtype;
  v_admission public.admissions%rowtype;
begin
  if new.payment_type <> 'joining' then
    return new;
  end if;

  select * into v_claim
  from public.admission_payment_claims claim
  where claim.student_id = new.student_id
    and claim.verification_status in ('pending', 'conflict')
    and claim.student_payment_id is null
  order by claim.created_at
  limit 1
  for update;

  if not found then
    return new;
  end if;

  select * into v_admission
  from public.admissions admission
  where admission.id = v_claim.admission_id;

  if found then
    new.plan_type := v_admission.fee_plan;
    new.cycle_start_date := v_admission.join_date;
    new.coaching_fee := v_admission.coaching_fee;
    new.admission_fee := v_admission.admission_fee;
    new.jersey_amount := v_admission.jersey_amount;
    new.total_fee_amount := v_admission.total_fee_amount;
    new.jersey_size := v_admission.jersey_size;
    new.jersey_pairs := v_admission.jersey_pairs;
  end if;

  new.amount := v_claim.amount;
  new.paid_on := coalesce(v_claim.payment_date, new.paid_on);
  new.proof_path := coalesce(nullif(v_claim.proof_path, ''), new.proof_path);
  new.payment_reference := coalesce(
    nullif(v_claim.payment_reference, ''),
    nullif(v_claim.utr, ''),
    new.payment_reference
  );

  return new;
end;
$$;

drop trigger if exists student_payments_preserve_admission_claim_values
on public.student_payments;

create trigger student_payments_preserve_admission_claim_values
before insert on public.student_payments
for each row
execute function public.preserve_admission_claim_payment_values();

commit;
