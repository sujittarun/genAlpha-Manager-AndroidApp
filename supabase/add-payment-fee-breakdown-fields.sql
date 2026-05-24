-- Incremental migration: keep joining-payment fee split details on payment rows.
-- Safe to run multiple times. Existing payment totals remain unchanged.

alter table public.student_payments
  add column if not exists coaching_fee numeric(10, 2) not null default 0,
  add column if not exists admission_fee numeric(10, 2) not null default 0,
  add column if not exists jersey_amount numeric(10, 2) not null default 0,
  add column if not exists total_fee_amount numeric(10, 2) not null default 0;

update public.student_payments
set
  total_fee_amount = case
    when coalesce(total_fee_amount, 0) > 0 then total_fee_amount
    else coalesce(amount, 0)
  end
where payment_type = 'joining'
  and coalesce(total_fee_amount, 0) = 0
  and coalesce(amount, 0) > 0;

