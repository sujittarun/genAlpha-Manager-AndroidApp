-- Preserve exact outbound WhatsApp message text on provider status rows.
-- Safe to run multiple times.

with source_rows as (
  select distinct on (status_row.id)
    status_row.id,
    source.message_body
  from public.whatsapp_flow_events status_row
  join public.whatsapp_flow_events source
    on source.message_id = status_row.message_id
   and source.message_body is not null
   and btrim(source.message_body) <> ''
  where status_row.event_type in (
    'reminder_message_status',
    'whatsapp_message_status',
    'confirmation_message_status'
  )
    and status_row.status in ('delivered', 'read')
    and status_row.message_id is not null
    and btrim(status_row.message_id) <> ''
    and (status_row.message_body is null or btrim(status_row.message_body) = '')
  order by
    status_row.id,
    case
      when source.direction = 'outbound' then 0
      when source.status in ('accepted', 'sent') then 1
      else 2
    end,
    source.created_at asc
)
update public.whatsapp_flow_events status_row
set message_body = source_rows.message_body
from source_rows
where status_row.id = source_rows.id;

create or replace function public.whatsapp_flow_event_title(p_event_type text, p_status text)
returns text
language sql
stable
as $$
  select case
    when p_event_type = 'reminder_created' then 'WhatsApp reminder prepared'
    when p_event_type = 'reminder_send_failed' then 'WhatsApp reminder failed'
    when p_event_type = 'reminder_message_status' then
      case
        when p_status = 'delivered' then 'Reminder delivered'
        when p_status = 'read' then 'Reminder read'
        when p_status = 'failed' then 'Reminder failed'
        else ''
      end
    when p_event_type = 'whatsapp_message_status' then
      case
        when p_status = 'delivered' then 'WhatsApp message delivered'
        when p_status = 'read' then 'WhatsApp message read'
        when p_status = 'failed' then 'WhatsApp message failed'
        else ''
      end
    when p_event_type = 'confirmation_message_status' then
      case
        when p_status = 'delivered' then 'Payment confirmation delivered'
        when p_status = 'read' then 'Payment confirmation read'
        when p_status = 'failed' then 'Payment confirmation failed'
        else ''
      end
    when p_event_type = 'parent_plan_selected' then 'Parent selected renewal plan'
    when p_event_type = 'payment_link_sent' then 'Payment link sent'
    when p_event_type = 'payment_attempted' then 'Parent tapped Pay Now'
    when p_event_type = 'payment_pending_verification' then 'Parent payment proof received'
    when p_event_type = 'payment_confirmed' then 'Payment confirmed by academy'
    when p_event_type = 'parent_help_requested' then 'Parent requested help'
    else ''
  end;
$$;
