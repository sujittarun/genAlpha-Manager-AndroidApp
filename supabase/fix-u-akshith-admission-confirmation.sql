-- Exact correction for U. AKSHITH after manager confirmation replaced the
-- extracted ₹4,000 payment claim with the ₹3,500 coaching-only amount.
begin;

do $$
declare
  v_student_id constant uuid := '9eab6729-0a1e-4d28-9337-f25475035c59';
  v_admission_id constant uuid := 'def67e17-3f93-4022-b254-a30ab2db7584';
  v_session_id constant uuid := '468ff6f9-95f6-4a3a-8d3c-27aed274eabf';
  v_payment_id constant uuid := '8e73d7b9-3006-48f5-8bbf-a0fbfd9b4fd8';
  v_claim public.admission_payment_claims%rowtype;
begin
  select * into strict v_claim
  from public.admission_payment_claims
  where admission_id = v_admission_id
    and student_id = v_student_id
    and student_payment_id = v_payment_id;

  if v_claim.amount <> 4000 then
    raise exception 'Expected the immutable admission payment claim to be ₹4,000, found ₹%.', v_claim.amount;
  end if;

  update public.student_payments
  set amount = v_claim.amount,
      paid_on = coalesce(v_claim.payment_date, paid_on),
      proof_path = coalesce(nullif(v_claim.proof_path, ''), proof_path),
      payment_reference = coalesce(
        nullif(v_claim.payment_reference, ''),
        nullif(v_claim.utr, ''),
        payment_reference
      )
  where id = v_payment_id
    and student_id = v_student_id
    and payment_type = 'joining';

  update public.students
  set amount_paid = v_claim.amount,
      parent_contact_no = '8897021555',
      updated_by = 'Codex payment/contact correction',
      updated_at = now()
  where id = v_student_id
    and name = 'U. AKSHITH';

  update public.admissions
  set amount_paid = v_claim.amount,
      parent_contact_no = '8897021555'
  where id = v_admission_id
    and approved_student_id = v_student_id;

  update public.admission_intake_sessions
  set draft = jsonb_set(draft, '{parent_contact_no}', to_jsonb('8897021555'::text), true),
      updated_at = now()
  where id = v_session_id
    and admission_id = v_admission_id;

  insert into public.student_timeline (
    student_id, event_type, event_date, title, details, changed_by
  )
  select
    v_student_id,
    'admission_payment_corrected',
    current_date,
    'Admission payment and contact corrected',
    'Payment preserved from the original proof claim: Rs 4,000 total (Rs 3,500 coaching + Rs 500 admission). Parent contact corrected to 8897021555.',
    'Codex'
  where not exists (
    select 1 from public.student_timeline timeline
    where timeline.student_id = v_student_id
      and timeline.event_type = 'admission_payment_corrected'
      and timeline.details like 'Payment preserved from the original proof claim:%'
  );
end;
$$;

commit;
