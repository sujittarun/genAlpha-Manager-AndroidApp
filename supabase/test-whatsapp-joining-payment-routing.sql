-- Rollback-only regression test: the first WhatsApp payment for an unpaid
-- approved player must be recorded as a joining payment, not a renewal.
begin;

do $$
declare
  v_admission_id uuid;
  v_student_id uuid;
  v_session_id uuid;
  v_payment_id uuid;
  v_payment_type text;
  v_student public.students%rowtype;
  v_admission public.admissions%rowtype;
begin
  insert into public.admissions (
    reg_no, applicant_name, nationality, date_of_birth, age, gender,
    father_guardian_name, emergency_contact_no, parent_contact_no,
    city, address, school_college, time_slot, join_date,
    fees_paid, amount_paid, fee_plan, coaching_fee, admission_fee,
    jersey_amount, total_fee_amount, consent_accepted, terms_accepted,
    review_status
  ) values (
    999998, 'JOINING PAYMENT ROUTE TEST', 'Indian', '2014-01-01', 12, 'Male',
    'Test Guardian', '9999999982', '9999999981', 'Hyderabad', 'Test address',
    'Test School', '5:30PM', current_date, false, 0, 'pending', 0, 0, 0, 0,
    true, true, 'pending'
  ) returning id into v_admission_id;

  select student_id into v_student_id
  from public.approve_admission(v_admission_id, 'Regression test', 'Joining payment routing');

  insert into public.admission_intake_sessions (
    channel, source_chat_id, source_sender_id, source_sender_name,
    status, intake_type, matched_student_id, draft, conflicts, missing_fields
  ) values (
    'web', 'rollback-joining-payment-test', 'rollback-test', 'Regression test',
    'waiting_for_confirmation', 'renewal', v_student_id,
    jsonb_build_object(
      'player_name', 'JOINING PAYMENT ROUTE TEST', 'reg_no', 999998,
      'plan_type', 'monthly', 'months_covered', 1, 'comments', '',
      'payment', jsonb_build_object(
        'amount', 4000, 'payment_date', current_date::text,
        'transaction_id', 'ROLLBACK-JOINING-PAYMENT-TEST', 'utr', '',
        'proof_path', '', 'screenshot_status', 'successful'
      )
    ),
    '[]'::jsonb, '{}'
  ) returning id into v_session_id;

  select payment_id into v_payment_id
  from public.finalize_renewal_intake(v_session_id, 'rollback-test', 'Regression test');

  select payment_type into v_payment_type
  from public.student_payments where id = v_payment_id;
  select * into v_student from public.students where id = v_student_id;
  select * into v_admission from public.admissions where id = v_admission_id;

  if v_payment_type <> 'joining'
    or not v_student.fees_paid
    or v_student.fee_plan <> 'monthly'
    or v_student.amount_paid <> 4000
    or not v_admission.fees_paid then
    raise exception 'WhatsApp joining-payment routing regression failed.';
  end if;
end;
$$;

rollback;
