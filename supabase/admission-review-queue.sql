-- Incremental migration: parent admission review queue.
-- Safe to run multiple times. Does not delete existing admissions or students.

alter table public.admissions
add column if not exists filled_by text not null default 'Parent / Guardian';

alter table public.students
add column if not exists filled_by text not null default '';

alter table public.admissions
add column if not exists review_status text;

alter table public.admissions
add column if not exists reviewed_by text;

alter table public.admissions
add column if not exists reviewed_at timestamptz;

alter table public.admissions
add column if not exists review_notes text not null default '';

alter table public.admissions
add column if not exists approved_student_id uuid references public.students(id) on delete set null;

update public.admissions
set review_status = 'approved'
where review_status is null;

alter table public.admissions
alter column review_status set default 'pending';

alter table public.admissions
alter column review_status set not null;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'admissions_review_status_check'
      and conrelid = 'public.admissions'::regclass
  ) then
    alter table public.admissions
    add constraint admissions_review_status_check
    check (review_status in ('pending', 'approved', 'rejected', 'archived'));
  end if;
end;
$$;

create index if not exists admissions_review_status_created_idx
on public.admissions (review_status, created_at desc);

create or replace function public.sync_student_from_admission()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  -- Parent submissions must wait for manager review.
  if coalesce(new.review_status, 'pending') <> 'approved' then
    return new;
  end if;

  if exists (
    select 1
    from public.students s
    where s.admission_id = new.id
       or (new.reg_no is not null and s.reg_no = new.reg_no)
  ) then
    return new;
  end if;

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
    coalesce(new.filled_by, 'Parent / Guardian'),
    new.father_guardian_name,
    new.parent_contact_no,
    new.emergency_contact_no,
    new.school_college,
    '',
    new.address,
    '{}',
    false,
    null,
    coalesce(new.reviewed_by, 'Admission Review'),
    coalesce(new.reviewed_by, 'Admission Review')
  );

  return new;
end;
$$;

drop trigger if exists admissions_create_student on public.admissions;

create trigger admissions_create_student
after insert on public.admissions
for each row
execute function public.sync_student_from_admission();

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
      reviewed_at = timezone('utc', now()),
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

create or replace function public.reject_admission(
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
      reviewed_at = timezone('utc', now()),
      review_notes = coalesce(p_review_notes, '')
  where id = p_admission_id
    and review_status <> 'approved';

  if not found then
    raise exception 'Admission not found or already approved.';
  end if;
end;
$$;

grant execute on function public.approve_admission(uuid, text, text) to authenticated;
grant execute on function public.reject_admission(uuid, text, text) to authenticated;
