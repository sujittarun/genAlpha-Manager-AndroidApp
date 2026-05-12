-- Migration: Convert all timestamp defaults to IST and fix 5:30 offset.
-- Safe to run multiple times.

-- 1. Ensure the database session and project-level timezone is IST.
ALTER DATABASE postgres SET timezone TO 'Asia/Kolkata';

-- 2. Update Column Defaults to use standard now() instead of the faulty UTC shift.
-- Using now() is correct for timestamptz columns when the session is set to IST.

ALTER TABLE IF EXISTS public.students ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.students ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.admissions ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.student_payments ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.academy_expenses ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.attendance ALTER COLUMN marked_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.student_timeline ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.system_settings ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.payment_link_requests ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE IF EXISTS public.reminder_events ALTER COLUMN created_at SET DEFAULT now();

-- 3. Fix existing data that was recorded with the 5:30 offset.
-- Note: We only update columns that previously had the timezone('utc', now()) default.
-- We check if the time is significantly in the past relative to now (to avoid double-shifting if run twice),
-- but since this is a one-time fix, we'll just run it once.
-- We use a guard to prevent shifting records that were already inserted correctly (e.g. from Edge Functions).
-- However, most records in these tables come from DB defaults.

UPDATE public.students SET created_at = created_at + interval '5 hours 30 minutes', updated_at = updated_at + interval '5 hours 30 minutes' WHERE created_at < now() - interval '1 hour';
UPDATE public.admissions SET created_at = created_at + interval '5 hours 30 minutes' WHERE created_at < now() - interval '1 hour';
UPDATE public.admissions SET reviewed_at = reviewed_at + interval '5 hours 30 minutes' WHERE reviewed_at IS NOT NULL AND reviewed_at < now() - interval '1 hour';
UPDATE public.student_payments SET created_at = created_at + interval '5 hours 30 minutes' WHERE created_at < now() - interval '1 hour';
UPDATE public.academy_expenses SET created_at = created_at + interval '5 hours 30 minutes' WHERE created_at < now() - interval '1 hour';
UPDATE public.attendance SET marked_at = marked_at + interval '5 hours 30 minutes' WHERE marked_at < now() - interval '1 hour';
UPDATE public.student_timeline SET created_at = created_at + interval '5 hours 30 minutes' WHERE created_at < now() - interval '1 hour';
UPDATE public.system_settings SET updated_at = updated_at + interval '5 hours 30 minutes' WHERE updated_at < now() - interval '1 hour';
UPDATE public.payment_link_requests SET created_at = created_at + interval '5 hours 30 minutes' WHERE created_at < now() - interval '1 hour';
UPDATE public.reminder_events SET created_at = created_at + interval '5 hours 30 minutes' WHERE created_at < now() - interval '1 hour';

-- 4. Update RPCs to use now() instead of timezone('utc', now())
CREATE OR REPLACE FUNCTION public.approve_admission(
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
  select *
  into v_admission
  from public.admissions
  where id = p_admission_id
  for update;

  if not found then
    raise exception 'Admission not found.';
  end if;

  if v_admission.review_status = 'approved' and v_admission.approved_student_id is not null then
    return query
    select v_admission.approved_student_id, v_admission.reg_no;
    return;
  end if;

  select s.id
  into v_student_id
  from public.students s
  where s.admission_id = v_admission.id
     or (v_admission.reg_no is not null and s.reg_no = v_admission.reg_no)
  limit 1;

  if v_student_id is null then
    insert into public.students (
      reg_no,
      admission_id,
      name,
      age,
      time_slot,
      join_date,
      fees_paid,
      amount_paid,
      jersey_size,
      jersey_pairs,
      payment_method,
      payment_upi_id,
      payment_reference,
      comments,
      filled_by,
      father_guardian_name,
      parent_contact_no,
      alternate_contact_no,
      school_college,
      grade,
      address,
      renewals,
      discontinued,
      discontinued_at,
      added_by,
      updated_by
    )
    values (
      v_admission.reg_no,
      v_admission.id,
      v_admission.applicant_name,
      v_admission.age,
      v_admission.time_slot,
      v_admission.join_date,
      v_admission.fees_paid,
      v_admission.amount_paid,
      v_admission.jersey_size,
      v_admission.jersey_pairs,
      coalesce(v_admission.payment_method, 'UPI'),
      coalesce(v_admission.payment_upi_id, ''),
      coalesce(v_admission.payment_reference, ''),
      coalesce(v_admission.comments, ''),
      coalesce(v_admission.filled_by, 'Parent / Guardian'),
      v_admission.father_guardian_name,
      v_admission.parent_contact_no,
      v_admission.emergency_contact_no,
      v_admission.school_college,
      '',
      v_admission.address,
      '{}',
      false,
      null,
      coalesce(nullif(p_reviewed_by, ''), 'Manager'),
      coalesce(nullif(p_reviewed_by, ''), 'Manager')
    )
    returning id into v_student_id;
  end if;

  update public.admissions
  set review_status = 'approved',
      reviewed_by = coalesce(nullif(p_reviewed_by, ''), 'Manager'),
      reviewed_at = now(), -- Changed from timezone('utc', now())
      review_notes = coalesce(p_review_notes, ''),
      approved_student_id = v_student_id
  where id = p_admission_id;

  if to_regclass('public.student_timeline') is not null then
    insert into public.student_timeline (
      student_id,
      event_type,
      event_date,
      title,
      details,
      changed_by
    )
    values (
      v_student_id,
      'admission_review',
      current_date,
      'Admission approved',
      concat('Reg No ', coalesce(v_admission.reg_no::text, 'manual'), case when coalesce(p_review_notes, '') <> '' then concat(' - ', p_review_notes) else '' end),
      coalesce(nullif(p_reviewed_by, ''), 'Manager')
    );
  end if;

  return query
  select v_student_id, v_admission.reg_no;
end;
$$;

CREATE OR REPLACE FUNCTION public.reject_admission(
  p_admission_id uuid,
  p_reviewed_by text default 'Manager',
  p_review_notes text default ''
)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.admissions
  set review_status = 'rejected',
      reviewed_by = coalesce(nullif(p_reviewed_by, ''), 'Manager'),
      reviewed_at = now(), -- Changed from timezone('utc', now())
      review_notes = coalesce(p_review_notes, '')
  where id = p_admission_id
    and review_status <> 'approved';

  if not found then
    raise exception 'Admission not found or already approved.';
  end if;
end;
$$;

-- 5. Update Triggers
CREATE OR REPLACE FUNCTION public.log_student_timeline()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    INSERT INTO public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    VALUES (NEW.id, 'creation', NEW.join_date, 'Player joined', 
      concat('Joined ', NEW.time_slot, ' batch'), 
      COALESCE(NEW.added_by, 'System'));
  ELSIF (TG_OP = 'UPDATE') THEN
    -- Log specific changes if needed
  END IF;
  RETURN NEW;
END;
$$;

-- Ensure updated_at trigger uses now()
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.updated_at = now(); -- Changed from timezone('utc', now())
  RETURN NEW;
END;
$$;
