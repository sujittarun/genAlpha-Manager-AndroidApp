-- Add gender column to students table and update sync function.
alter table public.students add column if not exists gender text not null default '';

-- Update sync function to include gender
create or replace function public.sync_student_from_admission()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.students (
    reg_no,
    admission_id,
    name,
    gender,
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
    new.gender,
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
  );

  return new;
end;
$$;
