-- Split WhatsApp read receipts from delivered receipts in generated timeline rows.
-- Safe to run multiple times.

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
    when p_event_type = 'parent_plan_selected' then 'Parent selected renewal plan'
    when p_event_type = 'payment_link_sent' then 'Payment link sent'
    when p_event_type = 'payment_attempted' then 'Parent tapped Pay Now'
    when p_event_type = 'payment_pending_verification' then 'Parent payment proof received'
    when p_event_type = 'payment_confirmed' then 'Payment confirmed by academy'
    when p_event_type = 'parent_help_requested' then 'Parent requested help'
    else ''
  end;
$$;
