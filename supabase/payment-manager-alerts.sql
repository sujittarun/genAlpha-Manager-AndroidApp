-- Incremental migration: track manager WhatsApp alerts for parent payments/proofs.
-- Safe to run multiple times.

alter table if exists public.reminder_events
  add column if not exists manager_payment_alert_status text not null default 'none',
  add column if not exists manager_payment_alert_due_at timestamptz,
  add column if not exists manager_payment_alert_sent_at timestamptz,
  add column if not exists manager_payment_alert_meta_response jsonb,
  add column if not exists manager_payment_alert_error jsonb;

create index if not exists reminder_events_manager_payment_alert_due_idx
on public.reminder_events (manager_payment_alert_due_at asc)
where manager_payment_alert_status = 'scheduled'
  and manager_payment_alert_due_at is not null;

grant select, insert, update, delete on public.reminder_events to authenticated;
grant select, insert, update, delete on public.reminder_events to service_role;
