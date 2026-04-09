alter table public.students
add column if not exists added_by text not null default 'Unknown';

alter table public.students
add column if not exists updated_by text not null default 'Unknown';
