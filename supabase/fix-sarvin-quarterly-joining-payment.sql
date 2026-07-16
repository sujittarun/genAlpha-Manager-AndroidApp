-- Correct Sarvin Kanuru's manager-confirmed joining payment without changing
-- the Rs 10,000 actually received. The AgentAlpha session and admission both
-- explicitly selected quarterly / 3 months; only the later manager fallback
-- payment row was incorrectly written as monthly / 1 month.

begin;

do $$
declare
  v_session public.admission_intake_sessions%rowtype;
  v_admission public.admissions%rowtype;
  v_student public.students%rowtype;
  v_payment public.student_payments%rowtype;
begin
  select * into v_session
  from public.admission_intake_sessions
  where display_id = 'GACA-AI-2026-0045';

  if not found or v_session.admission_id is null then
    raise exception 'Sarvin AgentAlpha admission session was not found.';
  end if;
  if v_session.draft->>'fee_plan' <> 'quarterly'
     or coalesce((v_session.draft->>'months_covered')::integer, 0) <> 3 then
    raise exception 'Sarvin session no longer contains the verified quarterly / 3-month plan.';
  end if;

  select * into v_admission
  from public.admissions
  where id = v_session.admission_id;

  if not found or upper(btrim(v_admission.applicant_name)) <> 'SARVIN KANURU'
     or v_admission.fee_plan <> 'quarterly'
     or v_admission.approved_student_id is null then
    raise exception 'Sarvin approved quarterly admission no longer matches the correction guard.';
  end if;

  select * into v_student
  from public.students
  where id = v_admission.approved_student_id;

  select * into v_payment
  from public.student_payments
  where student_id = v_student.id
    and payment_type = 'joining'
    and amount = 10000
  order by created_at desc
  limit 1;

  if not found then
    raise exception 'Sarvin Rs 10,000 joining payment was not found.';
  end if;

  update public.student_payments
  set plan_type = 'quarterly',
      months_covered = 3,
      coaching_fee = v_admission.coaching_fee,
      admission_fee = v_admission.admission_fee,
      jersey_amount = v_admission.jersey_amount,
      total_fee_amount = v_admission.total_fee_amount,
      jersey_size = v_admission.jersey_size,
      jersey_pairs = v_admission.jersey_pairs,
      comment = 'Joining fee confirmed by manager. Plan corrected to quarterly / 3 months from the verified AgentAlpha admission.'
  where id = v_payment.id;

  update public.students
  set fee_plan = 'quarterly',
      payment_status = 'paid',
      updated_by = 'AgentAlpha plan correction'
  where id = v_student.id;

  update public.admissions
  set fee_plan = 'quarterly',
      payment_verification_status = 'verified'
  where id = v_admission.id;

  insert into public.student_timeline (
    student_id, event_type, event_date, title, details, changed_by
  )
  select
    v_student.id,
    'data_correction',
    current_date,
    'Joining plan corrected',
    'Rs 10,000 retained as the amount received. Coverage corrected from monthly / 1 month to quarterly / 3 months using the confirmed AgentAlpha admission. The earlier 16 Aug due-date confirmation was incorrect; paid-through is 16 Oct 2026.',
    'AgentAlpha plan correction'
  where not exists (
    select 1
    from public.student_timeline
    where student_id = v_student.id
      and event_type = 'data_correction'
      and title = 'Joining plan corrected'
  );
end;
$$;

commit;
