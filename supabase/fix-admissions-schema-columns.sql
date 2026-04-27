-- Safe patch: ensures admissions table has columns expected by app/web payloads.
-- Run this in the Supabase SQL editor to fix the "couldn't find comments column" error.

alter table if exists public.admissions
  add column if not exists payment_method text not null default 'UPI',
  add column if not exists payment_upi_id text not null default '',
  add column if not exists payment_reference text not null default '',
  add column if not exists comments text not null default '',
  add column if not exists batsman_style text not null default '',
  add column if not exists bowling_styles text[] not null default '{}',
  add column if not exists ready_to_start boolean not null default false;

-- Refresh the schema cache so PostgREST API recognizes the new columns immediately
NOTIFY pgrst, 'reload schema';
