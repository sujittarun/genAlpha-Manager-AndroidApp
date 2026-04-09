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

grant execute on function public.unmark_player_attendance(uuid, date) to anon, authenticated;
