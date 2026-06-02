-- Incremental migration: add capped retry tracking for WhatsApp reminders.
-- Safe to run multiple times.

alter table if exists public.reminder_events
  add column if not exists retry_count integer not null default 0,
  add column if not exists max_retry_count integer not null default 3,
  add column if not exists next_retry_at timestamptz,
  add column if not exists last_retry_at timestamptz,
  add column if not exists retry_reason text,
  add column if not exists manual_followup_required boolean not null default false;

create index if not exists reminder_events_due_retry_idx
on public.reminder_events (next_retry_at asc)
where status = 'retry_scheduled' and next_retry_at is not null;

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

  if new.status = 'retry_scheduled' then
    v_title := 'Reminder retry scheduled';
    v_event_type := 'whatsapp_reminder_retry_scheduled';
    v_event_date := coalesce(new.failed_at, new.next_retry_at, now())::date;
    v_reason := coalesce(
      nullif(new.retry_reason, ''),
      nullif(new.meta_error->>'message', ''),
      nullif(new.meta_error->'error'->>'message', ''),
      'Meta limited delivery. The reminder will retry later.'
    );
  elsif new.status in ('failed', 'send_failed', 'delivery_failed', 'undelivered') then
    v_title := 'Reminder failed';
    v_event_type := 'whatsapp_reminder_failed';
    v_event_date := coalesce(new.failed_at, now())::date;
    v_reason := coalesce(
      nullif(new.meta_error->>'message', ''),
      nullif(new.meta_error->'error'->>'message', ''),
      nullif(new.meta_error->'error'->'error_data'->>'details', ''),
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
    case
      when new.status = 'retry_scheduled' and new.next_retry_at is not null
        then concat(v_reason, ' Next retry: ', to_char(new.next_retry_at at time zone 'Asia/Kolkata', 'DD Mon YYYY HH24:MI'), ' IST')
      else concat('Status: ', new.status, ' • Reason: ', v_reason)
    end,
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
