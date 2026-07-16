begin;

do $$
declare
  v_admission_id uuid;
  v_student_id uuid;
  v_student public.students%rowtype;
begin
  insert into public.admissions (
    reg_no, applicant_name, nationality, date_of_birth, age, gender,
    father_guardian_name, emergency_contact_no, parent_contact_no,
    city, address, school_college, grade, parent_aadhaar_no,
    time_slot, join_date, fees_paid, amount_paid, fee_plan,
    coaching_fee, admission_fee, jersey_amount, total_fee_amount,
    jersey_size, jersey_pairs, payment_method, payment_reference,
    comments, filled_by, batsman_style, bowling_styles,
    ready_to_start, consent_accepted, terms_accepted, review_status,
    payment_verification_status
  ) values (
    999999, 'INTAKE FIELD AUDIT', 'Indian', '2014-09-26', 11, 'Male',
    'Audit Guardian', '9000000002', '9000000001',
    'Hyderabad', 'Audit home address', 'Audit School', '7', '',
    '5:30PM', current_date, false, 11975, 'quarterly',
    9975, 500, 1500, 11975, '32', 2, 'Cash', '',
    'Rollback-only admission mapping audit', 'Parent / Guardian',
    'Right Handed Batsman', array['Right Arm Fast Bowler'],
    true, true, true, 'pending', 'pending_verification'
  ) returning id into v_admission_id;

  select student_id into v_student_id
  from public.approve_admission(v_admission_id, 'Field mapping audit', 'Rollback-only test');

  select * into v_student
  from public.students
  where id = v_student_id;

  if v_student.grade <> '7'
    or v_student.school_college <> 'Audit School'
    or v_student.address <> 'Audit home address'
    or v_student.father_guardian_name <> 'Audit Guardian'
    or v_student.parent_contact_no <> '9000000001'
    or v_student.alternate_contact_no <> '9000000002'
    or v_student.fee_plan <> 'quarterly'
    or v_student.coaching_fee <> 9975
    or v_student.admission_fee <> 500
    or v_student.jersey_amount <> 1500
    or v_student.total_fee_amount <> 11975 then
    raise exception 'Admission approval field mapping audit failed: %', row_to_json(v_student);
  end if;
end;
$$;

rollback;
