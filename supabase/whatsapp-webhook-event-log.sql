-- Incremental migration: diagnostic WhatsApp webhook event log.
-- Safe to run multiple times. This is append-only troubleshooting data.

create table if not exists public.whatsapp_webhook_events (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  event_type text not null,
  from_phone text,
  message_id text,
  reminder_event_id uuid references public.reminder_events(id) on delete set null,
  processed boolean not null default false,
  processing_note text,
  payload jsonb not null default '{}'::jsonb
);

create index if not exists whatsapp_webhook_events_created_idx
on public.whatsapp_webhook_events (created_at desc);

create index if not exists whatsapp_webhook_events_from_created_idx
on public.whatsapp_webhook_events (from_phone, created_at desc);

alter table public.whatsapp_webhook_events enable row level security;

drop policy if exists "whatsapp_webhook_events_authenticated_all"
on public.whatsapp_webhook_events;

create policy "whatsapp_webhook_events_authenticated_all"
on public.whatsapp_webhook_events
for all
to authenticated
using (true)
with check (true);

grant select, insert, update, delete on public.whatsapp_webhook_events to authenticated;
