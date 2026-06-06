create or replace function pg_temp.ga_ordinal_month(p_value text)
returns text
language plpgsql
as $$
declare
  v_date date;
  v_day int;
  v_suffix text;
begin
  begin
    v_date := p_value::date;
  exception when others then
    return coalesce(p_value, '');
  end;

  v_day := extract(day from v_date)::int;
  v_suffix := case
    when v_day between 11 and 13 then 'th'
    when v_day % 10 = 1 then 'st'
    when v_day % 10 = 2 then 'nd'
    when v_day % 10 = 3 then 'rd'
    else 'th'
  end;

  return v_day::text || v_suffix || ' ' || trim(to_char(v_date, 'Month'));
end;
$$;

create or replace function pg_temp.ga_reminder_due_text(p_reminder_type text, p_due_date text)
returns text
language sql
as $$
  select case
    when coalesce(p_reminder_type, '') = 'joining_fee'
      then pg_temp.ga_ordinal_month(p_due_date) || ' (Admission + 1st Month)'
    else pg_temp.ga_ordinal_month(p_due_date)
  end;
$$;

create or replace function pg_temp.ga_reminder_message_body(
  p_player_name text,
  p_due_date text,
  p_reminder_type text
)
returns text
language sql
as $$
  select case
    when coalesce(p_reminder_type, '') = 'heads_up' then
      'Academy fee renewal notice for ' || coalesce(nullif(p_player_name, ''), 'Player') || E'.\n\n' ||
      'Due date: ' || pg_temp.ga_reminder_due_text(p_reminder_type, p_due_date) || E'\n\n' ||
      'Please select a payment option below to complete the fee payment. For billing help, tap Need Help.'
    when coalesce(p_reminder_type, '') = 'renewal_day' then
      'Today is ' || coalesce(nullif(p_player_name, ''), 'Player') || '''s academy fee renewal date for ' ||
      pg_temp.ga_reminder_due_text(p_reminder_type, p_due_date) || E'.\n\n' ||
      'Please select a payment option below to complete the fee renewal. For billing help, tap Need Help.'
    else
      'Hi ' || coalesce(nullif(p_player_name, ''), 'Player') || E', this is an academy fee payment reminder from Gen Alpha Cricket Academy.\n\n' ||
      'Fee due from: ' || pg_temp.ga_reminder_due_text(p_reminder_type, p_due_date) || E'\n\n' ||
      'Please select a payment option below to complete the fee renewal. If payment is already completed or you need billing help, tap Need Help.'
  end;
$$;

with rendered as (
  select
    r.id as reminder_event_id,
    pg_temp.ga_reminder_message_body(s.name, r.due_date::text, r.reminder_type) as message_body
  from public.reminder_events r
  left join public.students s on s.id = r.student_id
  where r.created_at >= timestamptz '2026-06-04 00:00:00+05:30'
    and coalesce(r.channel, 'whatsapp') = 'whatsapp'
    and coalesce(r.reminder_type, '') in ('heads_up', 'renewal_day', 'renewal', 'joining_fee')
)
update public.reminder_events r
set message_preview = rendered.message_body
from rendered
where r.id = rendered.reminder_event_id;

with rendered as (
  select
    r.id as reminder_event_id,
    pg_temp.ga_reminder_message_body(s.name, r.due_date::text, r.reminder_type) as message_body
  from public.reminder_events r
  left join public.students s on s.id = r.student_id
  where r.created_at >= timestamptz '2026-06-04 00:00:00+05:30'
    and coalesce(r.channel, 'whatsapp') = 'whatsapp'
    and coalesce(r.reminder_type, '') in ('heads_up', 'renewal_day', 'renewal', 'joining_fee')
)
update public.whatsapp_flow_events w
set message_body = rendered.message_body
from rendered
where w.reminder_event_id = rendered.reminder_event_id
  and w.message_kind = 'template'
  and w.event_type in ('reminder_created', 'reminder_message_status');
