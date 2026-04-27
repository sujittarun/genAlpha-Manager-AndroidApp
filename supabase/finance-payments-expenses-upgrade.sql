-- Incremental migration: structured renewal payments + expenses.
-- Safe to run multiple times. Does not drop existing data.

create table if not exists public.student_payments (
  id uuid primary key default gen_random_uuid(),
  student_id uuid not null references public.students(id) on delete cascade,
  payment_type text not null default 'renewal',
  plan_type text not null default 'monthly',
  cycle_start_date date not null,
  months_covered integer not null default 1,
  amount numeric(10, 2) not null default 0,
  paid_on date not null default current_date,
  comment text not null default '',
  recorded_by text not null default 'Unknown',
  created_at timestamptz not null default timezone('utc', now())
);

create index if not exists student_payments_student_paid_idx
on public.student_payments (student_id, paid_on desc);

create table if not exists public.academy_expenses (
  id uuid primary key default gen_random_uuid(),
  expense_date date not null default current_date,
  expense_type text not null,
  amount numeric(10, 2) not null check (amount >= 0),
  comment text not null default '',
  paid_by text not null,
  created_by text not null default 'Unknown',
  created_at timestamptz not null default timezone('utc', now())
);

create index if not exists academy_expenses_date_idx
on public.academy_expenses (expense_date desc);

create or replace function public.log_student_payment_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.student_timeline (
    student_id,
    event_type,
    event_date,
    title,
    details,
    changed_by
  )
  values (
    new.student_id,
    'payment',
    new.paid_on,
    case when new.payment_type = 'renewal' then 'Renewal fee paid' else 'Fee payment recorded' end,
    concat(
      'Rs ', new.amount,
      ' • ', new.plan_type,
      ' • ', new.months_covered, ' month',
      case when new.months_covered = 1 then '' else 's' end,
      ' • cycle from ', new.cycle_start_date
    ),
    new.recorded_by
  );

  return new;
end;
$$;

drop trigger if exists student_payments_log_timeline on public.student_payments;
create trigger student_payments_log_timeline
after insert on public.student_payments
for each row
execute function public.log_student_payment_timeline();

alter table public.student_payments enable row level security;
alter table public.academy_expenses enable row level security;

drop policy if exists "student_payments_authenticated_all" on public.student_payments;
create policy "student_payments_authenticated_all"
on public.student_payments
for all
to authenticated
using (true)
with check (true);

drop policy if exists "academy_expenses_authenticated_all" on public.academy_expenses;
create policy "academy_expenses_authenticated_all"
on public.academy_expenses
for all
to authenticated
using (true)
with check (true);

grant select, insert, update, delete on public.student_payments to authenticated;
grant select, insert, update, delete on public.academy_expenses to authenticated;
