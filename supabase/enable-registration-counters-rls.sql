-- Keep the admission registration-number counter private.
-- The counter is advanced/read only through security definer RPCs such as
-- submit_admission_form() and peek_next_admission_reg_no().

create table if not exists public.registration_counters (
  counter_name text primary key,
  next_reg_no bigint not null
);

alter table public.registration_counters enable row level security;

revoke all on table public.registration_counters from public;
revoke all on table public.registration_counters from anon;
revoke all on table public.registration_counters from authenticated;
revoke all on table public.registration_counters from service_role;

comment on table public.registration_counters is
  'Private registration sequence table. Direct PostgREST access is blocked by RLS; admission RPCs maintain the counter.';
