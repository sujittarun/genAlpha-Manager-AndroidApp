-- Incremental migration: richer player profile details + timeline audit.
-- Safe to run multiple times. It does not drop existing data.

alter table public.students
add column if not exists father_guardian_name text not null default '';

alter table public.students
add column if not exists parent_contact_no text not null default '';

alter table public.students
add column if not exists alternate_contact_no text not null default '';

create table if not exists public.student_timeline (
  id uuid primary key default gen_random_uuid(),
  student_id uuid not null references public.students(id) on delete cascade,
  event_type text not null,
  event_date date not null default current_date,
  title text not null,
  details text not null default '',
  changed_by text not null default 'System',
  created_at timestamptz not null default timezone('utc', now())
);

create index if not exists student_timeline_student_created_idx
on public.student_timeline (student_id, created_at desc);

update public.students s
set
  father_guardian_name = coalesce(nullif(s.father_guardian_name, ''), a.father_guardian_name, ''),
  parent_contact_no = coalesce(nullif(s.parent_contact_no, ''), a.parent_contact_no, ''),
  alternate_contact_no = coalesce(nullif(s.alternate_contact_no, ''), a.emergency_contact_no, '')
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

create or replace function public.log_student_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if tg_op = 'INSERT' then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (new.id, 'created', new.join_date, 'Player record created', 'Created from manager roster.', new.added_by);
    return new;
  end if;

  if new.renewals is distinct from old.renewals then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      'renewal_paid',
      current_date,
      'Renewal fee marked paid',
      concat('Renewal cycle date: ', coalesce(new.renewals[array_length(new.renewals, 1)]::text, '')),
      new.updated_by
    );
  end if;

  if new.fees_paid is distinct from old.fees_paid or new.amount_paid is distinct from old.amount_paid then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      'fees_updated',
      current_date,
      'Fees updated',
      concat('Fees paid: ', case when new.fees_paid then 'Yes' else 'No' end, ', Amount: Rs ', new.amount_paid),
      new.updated_by
    );
  end if;

  if new.discontinued is distinct from old.discontinued then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (
      new.id,
      case when new.discontinued then 'discontinued' else 'active' end,
      current_date,
      case when new.discontinued then 'Player discontinued' else 'Player marked active' end,
      '',
      new.updated_by
    );
  end if;

  if row(new.name, new.age, new.time_slot, new.join_date, new.jersey_size, new.jersey_pairs, new.father_guardian_name, new.parent_contact_no, new.alternate_contact_no)
     is distinct from
     row(old.name, old.age, old.time_slot, old.join_date, old.jersey_size, old.jersey_pairs, old.father_guardian_name, old.parent_contact_no, old.alternate_contact_no) then
    insert into public.student_timeline (student_id, event_type, event_date, title, details, changed_by)
    values (new.id, 'profile_updated', current_date, 'Player details updated', 'Profile, slot, jersey, or parent details changed.', new.updated_by);
  end if;

  return new;
end;
$$;

drop trigger if exists students_log_timeline_insert on public.students;
create trigger students_log_timeline_insert
after insert on public.students
for each row
when (new.added_by <> 'Admission Form')
execute function public.log_student_timeline();

drop trigger if exists students_log_timeline_update on public.students;
create trigger students_log_timeline_update
after update on public.students
for each row
execute function public.log_student_timeline();

alter table public.student_timeline enable row level security;

drop policy if exists "student_timeline_authenticated_read" on public.student_timeline;
create policy "student_timeline_authenticated_read"
on public.student_timeline
for select
to authenticated
using (true);

drop policy if exists "student_timeline_authenticated_insert" on public.student_timeline;
create policy "student_timeline_authenticated_insert"
on public.student_timeline
for insert
to authenticated
with check (true);

grant select, insert on public.student_timeline to authenticated;
