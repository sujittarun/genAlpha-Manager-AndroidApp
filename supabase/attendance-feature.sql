create table if not exists public.attendance (
  id uuid primary key default gen_random_uuid(),
  student_id uuid not null references public.students(id) on delete cascade,
  attendance_date date not null default current_date,
  marked_at timestamptz not null default timezone('utc', now()),
  marked_by text not null default 'Player View',
  unique (student_id, attendance_date)
);

create or replace function public.mark_player_attendance(
  p_student_id uuid,
  p_attendance_date date default current_date
)
returns table(id uuid)
language plpgsql
security definer
set search_path = public
as $$
begin
  return query
  insert into public.attendance (
    student_id,
    attendance_date,
    marked_by
  )
  values (
    p_student_id,
    coalesce(p_attendance_date, current_date),
    'Player View'
  )
  on conflict (student_id, attendance_date)
  do update set marked_at = timezone('utc', now())
  returning attendance.id;
end;
$$;

create or replace function public.unmark_player_attendance(
  p_student_id uuid,
  p_attendance_date date default current_date
)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  delete from public.attendance
  where student_id = p_student_id
    and attendance_date = coalesce(p_attendance_date, current_date);
end;
$$;

alter table public.attendance enable row level security;

drop policy if exists "attendance_public_read" on public.attendance;
create policy "attendance_public_read"
on public.attendance
for select
to anon, authenticated
using (true);

grant execute on function public.mark_player_attendance(uuid, date) to anon, authenticated;
grant execute on function public.unmark_player_attendance(uuid, date) to anon, authenticated;
