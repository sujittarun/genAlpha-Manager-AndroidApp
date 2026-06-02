-- Stabilize same-day WhatsApp retry processing and reduce noisy timeline rows.
-- Safe to run multiple times.

alter table if exists public.reminder_events
  alter column max_retry_count set default 3;

create index if not exists reminder_events_interrupted_retry_idx
on public.reminder_events (last_retry_at asc)
where status = 'queued' and retry_count > 0;

update public.reminder_events
set next_retry_at = coalesce(last_retry_at, failed_at, now())
where status = 'retry_scheduled'
  and next_retry_at is null
  and coalesce(retry_count, 0) < coalesce(max_retry_count, 3);

create or replace function public.log_reminder_status_change_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_reason text;
  v_title text;
  v_event_type text;
  v_event_date date;
begin
  if new.student_id is null or coalesce(new.status, '') = coalesce(old.status, '') then
    return new;
  end if;

  -- Retry scheduling is operational state for the fee pill/worker, not a
  -- student-history event. Keeping it out of student_timeline prevents the
  -- profile timeline from filling with every 5/30/60 minute retry transition.
  if new.status = 'retry_scheduled' then
    return new;
  end if;

  if new.status in ('failed', 'send_failed', 'delivery_failed', 'undelivered') then
    v_title := 'Reminder failed';
    v_event_type := 'whatsapp_reminder_failed';
    v_event_date := coalesce(new.failed_at, now())::date;
    v_reason := coalesce(
      nullif(new.meta_error->>'message', ''),
      nullif(new.meta_error->'error'->>'message', ''),
      nullif(new.meta_error->'error'->'error_data'->>'details', ''),
      nullif(new.retry_reason, ''),
      'Provider did not return a detailed reason.'
    );
  else
    return new;
  end if;

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
    v_event_type,
    v_event_date,
    v_title,
    concat('Status: ', new.status, ' • Reason: ', v_reason),
    coalesce(nullif(new.created_by, ''), 'WhatsApp')
  );

  return new;
end;
$$;

drop trigger if exists reminder_events_status_change_timeline on public.reminder_events;

create trigger reminder_events_status_change_timeline
after update of status, meta_error, failed_at, next_retry_at, retry_reason on public.reminder_events
for each row
execute function public.log_reminder_status_change_timeline();

grant select, insert, update, delete on public.reminder_events to authenticated;
grant select, insert, update, delete on public.reminder_events to service_role;
