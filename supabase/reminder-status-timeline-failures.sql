-- Incremental migration: write exact WhatsApp reminder delivery failures to the player timeline.
-- Safe to run multiple times. The app also reads reminder_events directly as a UI fallback.

create or replace function public.log_reminder_status_change_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_reason text;
begin
  if new.student_id is null or coalesce(new.status, '') = coalesce(old.status, '') then
    return new;
  end if;

  if new.status not in ('failed', 'send_failed', 'delivery_failed', 'undelivered') then
    return new;
  end if;

  v_reason := coalesce(
    nullif(new.meta_error->>'message', ''),
    nullif(new.meta_error->'error'->>'message', ''),
    nullif(new.meta_error->'error'->'error_data'->>'details', ''),
    'Provider did not return a detailed reason.'
  );

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
    'whatsapp_reminder_failed',
    coalesce(new.failed_at, now())::date,
    'Reminder failed',
    concat('Status: ', new.status, ' • Reason: ', v_reason),
    coalesce(nullif(new.created_by, ''), 'WhatsApp')
  );

  return new;
end;
$$;

drop trigger if exists reminder_events_status_change_timeline on public.reminder_events;

create trigger reminder_events_status_change_timeline
after update of status, meta_error, failed_at on public.reminder_events
for each row
execute function public.log_reminder_status_change_timeline();
