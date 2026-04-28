-- Incremental migration: editable player contact, school, grade, and address fields.
-- Safe to run multiple times. It only adds columns and backfills from admissions.

alter table public.students
add column if not exists father_guardian_name text not null default '';

alter table public.students
add column if not exists parent_contact_no text not null default '';

alter table public.students
add column if not exists alternate_contact_no text not null default '';

alter table public.students
add column if not exists school_college text not null default '';

alter table public.students
add column if not exists grade text not null default '';

alter table public.students
add column if not exists address text not null default '';

update public.students s
set
  father_guardian_name = coalesce(nullif(s.father_guardian_name, ''), a.father_guardian_name, ''),
  parent_contact_no = coalesce(nullif(s.parent_contact_no, ''), a.parent_contact_no, ''),
  alternate_contact_no = coalesce(nullif(s.alternate_contact_no, ''), a.emergency_contact_no, ''),
  school_college = coalesce(nullif(s.school_college, ''), a.school_college, ''),
  address = coalesce(nullif(s.address, ''), a.address, '')
from public.admissions a
where s.admission_id = a.id;

create or replace function public.sync_student_from_admission()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_student_id uuid;
begin
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
    new.reg_no,
    new.id,
    new.applicant_name,
    new.age,
    new.time_slot,
    new.join_date,
    new.fees_paid,
    new.amount_paid,
    new.jersey_size,
    new.jersey_pairs,
    new.payment_method,
    new.payment_upi_id,
    new.payment_reference,
    new.comments,
    new.father_guardian_name,
    new.parent_contact_no,
    new.emergency_contact_no,
    new.school_college,
    '',
    new.address,
    '{}',
    false,
    null,
    'Admission Form',
    'Admission Form'
  )
  returning id into v_student_id;

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
    'admission',
    new.join_date,
    'Admission created',
    concat('Joined academy. Initial amount: Rs ', coalesce(new.amount_paid, 0)),
    'Admission Form'
  );

  return new;
end;
$$;
