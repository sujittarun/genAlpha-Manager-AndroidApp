alter table public.students
add column if not exists time_slot text not null default '';
