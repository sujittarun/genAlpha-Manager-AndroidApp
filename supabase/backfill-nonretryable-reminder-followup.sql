-- Mark current-day permanent WhatsApp reminder failures for staff follow-up.
-- Temporary healthy-ecosystem failures (131049) remain eligible for retries.

update public.reminder_events
set
  manual_followup_required = true,
  next_retry_at = null,
  retry_reason = coalesce(
    nullif(meta_error #>> '{errors,0,message}', ''),
    nullif(meta_error #>> '{statuses,0,errors,0,message}', ''),
    nullif(meta_error ->> 'message', ''),
    retry_reason,
    'WhatsApp reminder failed permanently; manual follow-up required.'
  )
where failed_at >= (
    (now() at time zone 'Asia/Kolkata')::date::timestamp
    at time zone 'Asia/Kolkata'
  )
  and status in ('failed', 'send_failed', 'delivery_failed', 'undelivered')
  and delivered_at is null
  and read_at is null
  and coalesce(
    meta_error #>> '{errors,0,code}',
    meta_error #>> '{statuses,0,errors,0,code}',
    meta_error ->> 'code',
    ''
  ) <> '131049';

-- A successful retry is the current state. Preserve the append-only failure
-- event in whatsapp_flow_events, but remove stale failure fields from the
-- mutable reminder row used by roster status chips.
update public.reminder_events
set
  meta_error = '{}'::jsonb,
  failed_at = null,
  manual_followup_required = false,
  next_retry_at = null,
  retry_reason = null
where accepted_at is not null
  and failed_at is not null
  and accepted_at > failed_at
  and created_at >= (
    (now() at time zone 'Asia/Kolkata')::date::timestamp
    at time zone 'Asia/Kolkata'
  )
  and status not in ('failed', 'send_failed', 'delivery_failed', 'undelivered');
