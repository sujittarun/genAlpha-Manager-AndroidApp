-- Rollback-only regression test: an explicitly unpaid admission must not need a
-- selected fee plan and must not create a payment claim.
begin;

do $$
declare
  v_session_id uuid;
  v_admission_id uuid;
  v_plan text;
  v_amount numeric;
  v_claim_count integer;
begin
  insert into public.admission_intake_sessions (
    channel, source_chat_id, source_sender_id, source_sender_name,
    status, intake_type, draft, conflicts, missing_fields
  ) values (
    'web', 'rollback-unpaid-plan-test', 'rollback-test', 'Regression test',
    'waiting_for_confirmation', 'admission',
    jsonb_build_object(
      'applicant_name', 'UNPAID PLAN TEST', 'nationality', 'INDIAN',
      'date_of_birth', '2014-01-01', 'age', 12, 'gender', 'MALE',
      'father_guardian_name', 'TEST GUARDIAN',
      'parent_contact_no', '9999999991', 'alternate_contact_no', '9999999992',
      'city', 'HYDERABAD', 'address', 'TEST ADDRESS',
      'school_college', 'TEST SCHOOL', 'grade', '6',
      'time_slot', '5:30PM', 'join_date', current_date::text,
      'fee_plan', 'pending', 'months_covered', 0,
      'custom_coaching_fee', 0, 'jersey_size', '', 'jersey_pairs', 0,
      'parent_aadhaar_no', '', 'filled_by', 'Regression test', 'comments', '',
      'batsman_style', '', 'bowling_styles', '[]'::jsonb,
      'ready_to_start', true, 'consent_accepted', true, 'terms_accepted', true,
      'payment', jsonb_build_object(
        'amount', 0, 'claimed_paid', false, 'evidence_type', 'none',
        'payment_date', '', 'payment_method', '', 'transaction_id', '', 'utr', '',
        'proof_path', '', 'proof_bucket', ''
      )
    ),
    '[]'::jsonb, '{}'
  ) returning id into v_session_id;

  select admission_id into v_admission_id
  from public.finalize_admission_intake(v_session_id, 'rollback-test', 'Regression test');

  select fee_plan, amount_paid into v_plan, v_amount
  from public.admissions where id = v_admission_id;

  select count(*) into v_claim_count
  from public.admission_payment_claims where session_id = v_session_id;

  if v_plan <> 'pending' or v_amount <> 0 or v_claim_count <> 0 then
    raise exception 'Unpaid pending-plan regression: plan %, amount %, claims %',
      v_plan, v_amount, v_claim_count;
  end if;
end;
$$;

rollback;
