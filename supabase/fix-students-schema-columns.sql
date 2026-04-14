-- Safe patch: ensures students table has columns expected by app/web payloads and sync triggers.
-- Run once in Supabase SQL editor.

alter table if exists public.students
  add column if not exists payment_method text not null default 'UPI',
  add column if not exists payment_upi_id text not null default '',
  add column if not exists payment_reference text not null default '',
  add column if not exists comments text not null default '';

-- Refresh the admission->student sync trigger function to use current schema.
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
    coalesce(new.payment_method, 'UPI'),
    coalesce(new.payment_upi_id, ''),
    coalesce(new.payment_reference, ''),
    coalesce(new.comments, ''),
    '{}',
    false,
    null,
    'Admission Form',
    'Admission Form'
  )
  on conflict (admission_id) do nothing;

  return new;
end;
$$;
