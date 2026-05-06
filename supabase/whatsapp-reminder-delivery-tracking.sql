-- Incremental migration: WhatsApp reminder delivery tracking.
-- Safe to run multiple times. Does not delete existing reminder data.

alter table public.reminder_events
add column if not exists whatsapp_message_id text;

alter table public.reminder_events
add column if not exists meta_response jsonb not null default '{}'::jsonb;

alter table public.reminder_events
add column if not exists meta_error jsonb not null default '{}'::jsonb;

alter table public.reminder_events
add column if not exists accepted_at timestamptz;

alter table public.reminder_events
add column if not exists delivered_at timestamptz;

alter table public.reminder_events
add column if not exists read_at timestamptz;

alter table public.reminder_events
add column if not exists failed_at timestamptz;

create index if not exists reminder_events_whatsapp_message_idx
on public.reminder_events (whatsapp_message_id)
where whatsapp_message_id is not null;
