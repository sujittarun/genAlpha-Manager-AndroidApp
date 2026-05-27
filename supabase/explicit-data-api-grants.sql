-- Explicit Data API grants for Supabase's May/October 2026 public-schema change.
-- Grants make tables reachable through PostgREST/supabase-js; RLS policies still
-- decide which rows and operations are actually allowed.

alter default privileges for role postgres in schema public
  revoke select, insert, update, delete on tables from anon, authenticated, service_role;

alter default privileges for role postgres in schema public
  revoke usage, select on sequences from anon, authenticated, service_role;

revoke all on table public.students from anon, authenticated, service_role;
revoke all on table public.admissions from anon, authenticated, service_role;
revoke all on table public.attendance from anon, authenticated, service_role;
revoke all on table public.student_payments from anon, authenticated, service_role;
revoke all on table public.academy_expenses from anon, authenticated, service_role;
revoke all on table public.system_settings from anon, authenticated, service_role;
revoke all on table public.reminder_events from anon, authenticated, service_role;
revoke all on table public.payment_link_requests from anon, authenticated, service_role;
revoke all on table public.student_timeline from anon, authenticated, service_role;
revoke all on table public.whatsapp_flow_events from anon, authenticated, service_role;
revoke all on table public.whatsapp_webhook_events from anon, authenticated, service_role;
revoke all on table public.player_activity_consolidated from anon, authenticated, service_role;
revoke all on sequence public.whatsapp_flow_events_flow_step_seq from anon, authenticated, service_role;

grant select on table public.students to anon;
grant select, insert, update, delete on table public.students to authenticated;
grant select, insert, update, delete on table public.students to service_role;

grant select, insert on table public.admissions to anon;
grant select, insert, update, delete on table public.admissions to authenticated;
grant select, insert, update, delete on table public.admissions to service_role;

grant select on table public.attendance to anon, authenticated;
grant select, insert, update, delete on table public.attendance to service_role;

grant select, insert, update, delete on table public.student_payments to authenticated;
grant select, insert, update, delete on table public.student_payments to service_role;

grant select, insert, update, delete on table public.academy_expenses to authenticated;
grant select, insert, update, delete on table public.academy_expenses to service_role;

grant select, insert, update on table public.system_settings to authenticated;
grant select, insert, update on table public.system_settings to service_role;

grant select, insert, update, delete on table public.reminder_events to authenticated;
grant select, insert, update, delete on table public.reminder_events to service_role;

grant select, insert, update, delete on table public.payment_link_requests to authenticated;
grant select, insert, update, delete on table public.payment_link_requests to service_role;

grant select, insert on table public.student_timeline to authenticated;
grant select, insert on table public.student_timeline to service_role;

grant select, insert on table public.whatsapp_flow_events to authenticated;
grant select, insert on table public.whatsapp_flow_events to service_role;
grant usage, select on sequence public.whatsapp_flow_events_flow_step_seq to authenticated;
grant usage, select on sequence public.whatsapp_flow_events_flow_step_seq to service_role;

grant select, insert, update, delete on table public.whatsapp_webhook_events to authenticated;
grant select, insert, update, delete on table public.whatsapp_webhook_events to service_role;

grant select on table public.player_activity_consolidated to authenticated;
grant select on table public.player_activity_consolidated to service_role;

revoke all on table public.registration_counters from public;
revoke all on table public.registration_counters from anon;
revoke all on table public.registration_counters from authenticated;
revoke all on table public.registration_counters from service_role;
