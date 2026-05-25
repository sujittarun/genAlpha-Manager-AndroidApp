create or replace function public.ensure_student_discontinued_at()
returns trigger
language plpgsql
set search_path = public
as $$
begin
  if new.discontinued is true
     and new.discontinued_at is null
     and (tg_op = 'INSERT' or old.discontinued is distinct from true) then
    new.discontinued_at = (timezone('Asia/Kolkata', now()))::date;
  end if;

  return new;
end;
$$;

drop trigger if exists students_ensure_discontinued_at on public.students;

create trigger students_ensure_discontinued_at
before insert or update of discontinued, discontinued_at on public.students
for each row
execute function public.ensure_student_discontinued_at();

update public.students
set discontinued_at = coalesce((updated_at at time zone 'Asia/Kolkata')::date, (timezone('Asia/Kolkata', now()))::date)
where discontinued is true
  and discontinued_at is null;
