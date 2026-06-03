-- Make WhatsApp timeline titles business-facing instead of provider-facing.
-- Safe to run multiple times.

create or replace function public.whatsapp_flow_event_title(p_event_type text, p_status text)
returns text
language sql
stable
as $$
  select case
    when p_event_type = 'reminder_created' then 'Fee reminder prepared'
    when p_event_type = 'reminder_send_failed' then 'Fee reminder failed to parent'
    when p_event_type = 'reminder_message_status' then
      case
        when p_status = 'delivered' then 'Fee reminder delivered to parent'
        when p_status = 'read' then 'Fee reminder read by parent'
        when p_status = 'failed' then 'Fee reminder failed to parent'
        else ''
      end
    when p_event_type = 'whatsapp_message_status' then
      case
        when p_status = 'delivered' then 'WhatsApp follow-up delivered'
        when p_status = 'read' then 'WhatsApp follow-up read'
        when p_status = 'failed' then 'WhatsApp follow-up failed'
        else ''
      end
    when p_event_type = 'confirmation_message_status' then
      case
        when p_status = 'delivered' then 'Payment confirmation delivered to parent'
        when p_status = 'read' then 'Payment confirmation read by parent'
        when p_status = 'failed' then 'Payment confirmation failed to parent'
        else ''
      end
    when p_event_type = 'manager_payment_alert_with_proof_sent' then 'Manager payment alert sent with proof'
    when p_event_type = 'manager_payment_alert_without_proof_sent' then 'Manager payment alert sent'
    when p_event_type = 'payment_verification_reply_sent' then 'Payment proof reply sent to parent'
    when p_event_type = 'parent_plan_selected' then 'Parent selected payment plan'
    when p_event_type = 'payment_link_sent' then 'Payment link sent to parent'
    when p_event_type = 'payment_attempted' then 'Parent tapped Pay Now'
    when p_event_type = 'payment_pending_verification' then 'Payment proof received from parent'
    when p_event_type = 'payment_confirmed' then 'Payment confirmed by academy'
    when p_event_type = 'parent_help_requested' then 'Parent requested help'
    else ''
  end;
$$;
