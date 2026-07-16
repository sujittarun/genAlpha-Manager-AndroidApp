begin;

alter table public.admissions alter column fee_plan set default 'pending';
alter table public.students alter column fee_plan set default 'pending';

-- This migration is applied after add-fee-breakdown-fields.sql. It makes the
-- conversational intake path enforce the same required fields as the online
-- admission form and preserves every extracted admission field.

create or replace function public.finalize_admission_intake(
  p_session_id uuid,
  p_confirmation_message_id text default '',
  p_confirmed_by text default 'WhatsApp staff'
)
returns table(admission_id uuid, reg_no bigint, payment_claim_id uuid)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_session public.admission_intake_sessions%rowtype;
  v_draft jsonb;
  v_payment jsonb;
  v_name text;
  v_nationality text;
  v_gender text;
  v_guardian text;
  v_phone text;
  v_alt_phone text;
  v_school text;
  v_address text;
  v_slot text;
  v_dob date;
  v_join_date date;
  v_age integer;
  v_plan text;
  v_months integer;
  v_jersey_pairs integer;
  v_coaching numeric(10, 2);
  v_admission_fee numeric(10, 2) := 500;
  v_jersey numeric(10, 2);
  v_total numeric(10, 2);
  v_claim_amount numeric(10, 2);
  v_claimed_paid boolean;
  v_evidence_type text;
  v_payment_date date;
  v_payment_method text;
  v_payment_reference text;
  v_bowling_styles text[] := '{}';
  v_ready_to_start boolean;
  v_consent_accepted boolean;
  v_terms_accepted boolean;
  v_reg_no bigint;
  v_admission_id uuid;
  v_claim_id uuid;
begin
  select * into v_session
  from public.admission_intake_sessions
  where id = p_session_id
  for update;

  if not found then
    raise exception 'Admission intake session not found.';
  end if;

  if v_session.admission_id is not null then
    return query
    select v_session.admission_id, a.reg_no, c.id
    from public.admissions a
    left join public.admission_payment_claims c on c.session_id = v_session.id
    where a.id = v_session.admission_id;
    return;
  end if;

  if v_session.status not in ('waiting_for_confirmation', 'confirmed') then
    raise exception 'Admission draft must be waiting for confirmation.';
  end if;

  if coalesce(array_length(v_session.missing_fields, 1), 0) > 0 then
    raise exception 'Admission still has missing required fields: %', array_to_string(v_session.missing_fields, ', ');
  end if;

  if jsonb_array_length(coalesce(v_session.conflicts, '[]'::jsonb)) > 0 then
    raise exception 'Admission still has unresolved conflicts.';
  end if;

  v_draft := v_session.draft;
  v_payment := coalesce(v_draft->'payment', '{}'::jsonb);
  v_name := btrim(coalesce(v_draft->>'applicant_name', ''));
  v_nationality := btrim(coalesce(v_draft->>'nationality', ''));
  v_gender := btrim(coalesce(v_draft->>'gender', ''));
  v_guardian := btrim(coalesce(v_draft->>'father_guardian_name', ''));
  v_phone := right(regexp_replace(coalesce(v_draft->>'parent_contact_no', ''), '\D', '', 'g'), 10);
  v_alt_phone := right(regexp_replace(coalesce(v_draft->>'alternate_contact_no', ''), '\D', '', 'g'), 10);
  v_school := btrim(coalesce(v_draft->>'school_college', ''));
  v_address := btrim(coalesce(v_draft->>'address', ''));
  v_slot := public.normalize_admission_intake_slot(v_draft->>'time_slot');
  v_dob := nullif(v_draft->>'date_of_birth', '')::date;
  v_join_date := nullif(v_draft->>'join_date', '')::date;
  v_age := case
    when v_dob is not null and v_join_date is not null then
      extract(year from age(v_join_date, v_dob))::integer
    else nullif(v_draft->>'age', '')::integer
  end;
  v_plan := lower(btrim(coalesce(v_draft->>'fee_plan', '')));
  v_ready_to_start := coalesce((v_draft->>'ready_to_start')::boolean, false);
  v_consent_accepted := coalesce((v_draft->>'consent_accepted')::boolean, false);
  v_terms_accepted := coalesce((v_draft->>'terms_accepted')::boolean, false);

  if jsonb_typeof(v_draft->'bowling_styles') = 'array' then
    select coalesce(array_agg(value), '{}')
    into v_bowling_styles
    from jsonb_array_elements_text(v_draft->'bowling_styles');
  end if;

  if v_name = '' then raise exception 'Student name is required.'; end if;
  if v_nationality = '' then raise exception 'Nationality is required.'; end if;
  if v_dob is null then raise exception 'Date of birth is required.'; end if;
  if v_age not between 4 and 18 then raise exception 'Student age is outside the app admission range.'; end if;
  if v_gender = '' then raise exception 'Gender is required.'; end if;
  if v_guardian = '' then raise exception 'Father or guardian name is required.'; end if;
  if length(v_phone) <> 10 then raise exception 'A valid 10-digit parent contact is required.'; end if;
  if length(v_alt_phone) <> 10 then raise exception 'A valid 10-digit alternate contact is required.'; end if;
  if v_school = '' then raise exception 'School or college is required.'; end if;
  if v_address = '' then raise exception 'Home address is required.'; end if;
  if v_slot = '' then raise exception 'Batch timing is missing or invalid.'; end if;
  if v_join_date is null then raise exception 'Joining date is required.'; end if;
  if not v_consent_accepted or not v_terms_accepted then
    raise exception 'Signed parent consent and academy terms are required.';
  end if;

  v_claim_amount := greatest(coalesce(nullif(v_payment->>'amount', '')::numeric, 0), 0);
  v_claimed_paid := coalesce((v_payment->>'claimed_paid')::boolean, false)
    or v_claim_amount > 0
    or coalesce(v_payment->>'proof_path', '') <> '';
  v_evidence_type := lower(coalesce(v_payment->>'evidence_type', 'none'));
  v_payment_date := nullif(v_payment->>'payment_date', '')::date;
  v_payment_method := btrim(coalesce(v_payment->>'payment_method', ''));
  v_payment_reference := coalesce(nullif(v_payment->>'transaction_id', ''), v_payment->>'utr', '');

  if v_claimed_paid then
    if v_plan not in ('monthly', 'quarterly', 'halfyearly', 'special', 'custom') then
      raise exception 'A valid fee plan is required when payment is marked paid.';
    end if;
  elsif v_plan not in ('monthly', 'quarterly', 'halfyearly', 'special', 'custom') then
    v_plan := 'pending';
  end if;

  v_months := greatest(coalesce(nullif(v_draft->>'months_covered', '')::integer, 1), 1);
  v_jersey_pairs := greatest(coalesce(nullif(v_draft->>'jersey_pairs', '')::integer, 0), 0);
  v_coaching := case v_plan
    when 'monthly' then 3500
    when 'quarterly' then 9975
    when 'halfyearly' then 18900
    when 'special' then round(10000 * v_months * case when v_months >= 6 then 0.90 when v_months >= 3 then 0.95 else 1 end, 2)
    when 'custom' then greatest(coalesce(nullif(v_draft->>'custom_coaching_fee', '')::numeric, 0), 0)
    else 0
  end;
  if v_plan = 'custom' and v_coaching <= 0 then
    raise exception 'A positive custom coaching fee is required.';
  end if;
  v_admission_fee := case when v_plan in ('pending', 'special') then 0 else 500 end;
  v_jersey := case when v_plan = 'pending' then 0 else v_jersey_pairs * 750 end;
  v_total := v_coaching + v_admission_fee + v_jersey;

  if v_claimed_paid then
    if v_payment_date is null then raise exception 'Payment date is required for the marked-paid fee.'; end if;
    if v_claim_amount <= 0 then raise exception 'Payment amount is required for the marked-paid fee.'; end if;
    if coalesce(v_payment->>'proof_path', '') = ''
      and v_payment_reference = ''
      and v_evidence_type <> 'cash_statement'
      and v_payment_method !~* '\mcash\M' then
      raise exception 'Send a payment screenshot or confirm the cash amount before saving.';
    end if;
  end if;

  if v_payment_method = '' then
    v_payment_method := case when v_evidence_type = 'cash_statement' then 'Cash' else 'UPI' end;
  end if;

  insert into public.registration_counters (counter_name, next_reg_no)
  values ('admissions', 1001)
  on conflict (counter_name) do nothing;

  update public.registration_counters
  set next_reg_no = next_reg_no + 1
  where counter_name = 'admissions'
  returning next_reg_no - 1 into v_reg_no;

  insert into public.admissions (
    reg_no, applicant_name, nationality, date_of_birth, age, gender,
    father_guardian_name, emergency_contact_no, parent_contact_no,
    city, address, school_college, grade, parent_aadhaar_no,
    time_slot, join_date, fees_paid, amount_paid, fee_plan,
    coaching_fee, admission_fee, jersey_amount, total_fee_amount,
    jersey_size, jersey_pairs, payment_method, payment_upi_id,
    payment_reference, comments, filled_by, batsman_style,
    bowling_styles, ready_to_start, consent_accepted, terms_accepted,
    review_status, intake_session_id, source_channel, source_confidence,
    payment_verification_status
  ) values (
    v_reg_no, v_name, v_nationality, v_dob, v_age, v_gender,
    v_guardian, v_alt_phone, v_phone,
    coalesce(v_draft->>'city', ''), v_address, v_school,
    coalesce(v_draft->>'grade', ''), coalesce(v_draft->>'parent_aadhaar_no', ''),
    v_slot, v_join_date, false, v_claim_amount, v_plan,
    v_coaching, v_admission_fee, v_jersey, v_total,
    coalesce(v_draft->>'jersey_size', ''), v_jersey_pairs,
    v_payment_method, coalesce(v_payment->>'upi_id', ''), v_payment_reference,
    coalesce(v_draft->>'comments', ''),
    coalesce(nullif(v_draft->>'filled_by', ''), 'Parent / Guardian'),
    coalesce(v_draft->>'batsman_style', ''), v_bowling_styles,
    v_ready_to_start, v_consent_accepted, v_terms_accepted,
    'pending', v_session.id, v_session.channel, v_session.overall_confidence,
    case when v_claimed_paid then 'pending_verification' else '' end
  ) returning id into v_admission_id;

  if v_claimed_paid then
    insert into public.admission_payment_claims (
      session_id, admission_id, amount, payment_date, payment_time,
      payment_method, payment_reference, utr, payer_name, receiver_name,
      screenshot_status, verification_status, confidence,
      proof_bucket, proof_path, extracted_data
    ) values (
      v_session.id, v_admission_id, v_claim_amount, v_payment_date,
      nullif(v_payment->>'payment_time', '')::time,
      v_payment_method, coalesce(v_payment->>'transaction_id', ''),
      coalesce(v_payment->>'utr', ''), coalesce(v_payment->>'payer_name', ''),
      coalesce(v_payment->>'receiver_name', ''),
      case lower(coalesce(v_payment->>'screenshot_status', 'unknown'))
        when 'successful' then 'successful' when 'failed' then 'failed'
        when 'pending' then 'pending' when 'processing' then 'processing'
        else 'unknown' end,
      'pending', nullif(v_payment->>'confidence', '')::numeric,
      coalesce(v_payment->>'proof_bucket', ''), coalesce(v_payment->>'proof_path', ''),
      v_payment
    ) returning id into v_claim_id;
  end if;

  update public.admission_intake_sessions
  set status = 'confirmed', admission_id = v_admission_id,
      confirmation_message_id = coalesce(p_confirmation_message_id, ''),
      confirmed_by = coalesce(nullif(p_confirmed_by, ''), 'WhatsApp staff'),
      confirmed_at = coalesce(confirmed_at, now()), error_code = '', error_message = ''
  where id = v_session.id;

  return query select v_admission_id, v_reg_no, v_claim_id;
end;
$$;

create or replace function public.approve_admission(
  p_admission_id uuid,
  p_reviewed_by text default 'Manager',
  p_review_notes text default ''
)
returns table(student_id uuid, reg_no bigint)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_admission public.admissions%rowtype;
  v_student_id uuid;
begin
  select * into v_admission
  from public.admissions
  where id = p_admission_id
  for update;

  if not found then raise exception 'Admission not found.'; end if;
  if v_admission.review_status = 'approved' and v_admission.approved_student_id is not null then
    return query select v_admission.approved_student_id, v_admission.reg_no;
    return;
  end if;

  select s.id into v_student_id
  from public.students s
  where s.admission_id = v_admission.id
     or (v_admission.reg_no is not null and s.reg_no = v_admission.reg_no)
  limit 1;

  if v_student_id is null then
    insert into public.students (
      reg_no, admission_id, name, age, time_slot, join_date,
      fees_paid, amount_paid, fee_plan, coaching_fee, admission_fee,
      jersey_amount, total_fee_amount, jersey_size, jersey_pairs,
      payment_method, payment_upi_id, payment_reference, payment_status,
      comments, filled_by, father_guardian_name, parent_contact_no,
      alternate_contact_no, school_college, grade, address,
      renewals, discontinued, discontinued_at, added_by, updated_by
    ) values (
      v_admission.reg_no, v_admission.id, v_admission.applicant_name,
      v_admission.age, v_admission.time_slot, v_admission.join_date,
      v_admission.fees_paid, v_admission.amount_paid, v_admission.fee_plan,
      v_admission.coaching_fee, v_admission.admission_fee,
      v_admission.jersey_amount, v_admission.total_fee_amount,
      v_admission.jersey_size, v_admission.jersey_pairs,
      coalesce(v_admission.payment_method, 'UPI'),
      coalesce(v_admission.payment_upi_id, ''),
      coalesce(v_admission.payment_reference, ''),
      coalesce(v_admission.payment_verification_status, ''),
      coalesce(v_admission.comments, ''),
      coalesce(v_admission.filled_by, 'Parent / Guardian'),
      v_admission.father_guardian_name, v_admission.parent_contact_no,
      v_admission.emergency_contact_no, v_admission.school_college,
      coalesce(v_admission.grade, ''), v_admission.address,
      '{}', false, null,
      coalesce(nullif(p_reviewed_by, ''), 'Manager'),
      coalesce(nullif(p_reviewed_by, ''), 'Manager')
    ) returning id into v_student_id;
  end if;

  update public.admissions
  set review_status = 'approved',
      reviewed_by = coalesce(nullif(p_reviewed_by, ''), 'Manager'),
      reviewed_at = now(), review_notes = coalesce(p_review_notes, ''),
      approved_student_id = v_student_id
  where id = p_admission_id;

  if to_regclass('public.student_timeline') is not null then
    insert into public.student_timeline (
      student_id, event_type, event_date, title, details, changed_by
    ) values (
      v_student_id, 'admission_review', current_date, 'Admission approved',
      concat('Reg No ', coalesce(v_admission.reg_no::text, 'manual'),
        case when coalesce(p_review_notes, '') <> '' then concat(' - ', p_review_notes) else '' end),
      coalesce(nullif(p_reviewed_by, ''), 'Manager')
    );
  end if;

  return query select v_student_id, v_admission.reg_no;
end;
$$;

grant execute on function public.finalize_admission_intake(uuid, text, text) to authenticated, service_role;
grant execute on function public.approve_admission(uuid, text, text) to authenticated, service_role;

commit;
