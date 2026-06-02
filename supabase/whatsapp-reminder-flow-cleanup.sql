-- Canonical WhatsApp flow cleanup.
-- The app now reads WhatsApp history from whatsapp_flow_events, so direct
-- reminder_events insert timeline rows are legacy noise.
-- Safe to run multiple times.

drop trigger if exists reminder_events_log_timeline on public.reminder_events;

create or replace function public.log_reminder_event_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  -- Legacy no-op. Keep the function so older references do not fail, but do
  -- not create "Renewal reminder prepared" / "Joining fee reminder prepared"
  -- student_timeline rows anymore.
  return new;
end;
$$;
