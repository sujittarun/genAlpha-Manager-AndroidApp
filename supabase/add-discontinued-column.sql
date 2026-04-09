alter table public.students
add column if not exists discontinued boolean not null default false;
