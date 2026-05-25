alter table public.students
add column if not exists rejoined_at date,
add column if not exists fee_pause_days integer not null default 0;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'students_fee_pause_days_nonnegative'
      and conrelid = 'public.students'::regclass
  ) then
    alter table public.students
    add constraint students_fee_pause_days_nonnegative
    check (fee_pause_days >= 0) not valid;
  end if;
end;
$$;

alter table public.students
validate constraint students_fee_pause_days_nonnegative;

update public.students
set discontinued_at = coalesce((updated_at at time zone 'Asia/Kolkata')::date, (timezone('Asia/Kolkata', now()))::date)
where discontinued is true
  and discontinued_at is null;

create or replace function public.ensure_student_pause_billing_fields()
returns trigger
language plpgsql
set search_path = public
as $$
declare
  today_ist date := (timezone('Asia/Kolkata', now()))::date;
  pause_start date;
  pause_days integer;
begin
  if new.fee_pause_days is null then
    new.fee_pause_days := 0;
  end if;

  if new.discontinued is true
     and (tg_op = 'INSERT' or old.discontinued is distinct from true) then
    new.discontinued_at := coalesce(new.discontinued_at, today_ist);
  end if;

  if tg_op = 'UPDATE'
     and old.discontinued is true
     and new.discontinued is false then
    new.rejoined_at := coalesce(new.rejoined_at, today_ist);
    pause_start := coalesce(old.discontinued_at, new.discontinued_at, old.updated_at::date, old.created_at::date, today_ist);
    pause_days := greatest(new.rejoined_at - pause_start, 0);

    if new.fee_pause_days is not distinct from old.fee_pause_days then
      new.fee_pause_days := coalesce(old.fee_pause_days, 0) + pause_days;
    end if;
  end if;

  return new;
end;
$$;

drop trigger if exists students_ensure_discontinued_at on public.students;
drop trigger if exists students_pause_billing_fields on public.students;

create trigger students_pause_billing_fields
before insert or update of discontinued, discontinued_at, rejoined_at, fee_pause_days on public.students
for each row
execute function public.ensure_student_pause_billing_fields();

notify pgrst, 'reload schema';
