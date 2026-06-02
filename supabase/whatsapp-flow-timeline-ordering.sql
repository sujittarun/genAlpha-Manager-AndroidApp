-- Keep WhatsApp flow timeline rows meaningful and ordered by provider event time.
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

create or replace function public.log_whatsapp_flow_event_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_details text;
  v_title text;
  v_happened_at timestamptz;
begin
  if new.student_id is null then
    return new;
  end if;

  v_title := public.whatsapp_flow_event_title(new.event_type, new.status);
  if nullif(v_title, '') is null then
    return new;
  end if;

  v_happened_at := coalesce(
    new.status_at,
    new.read_at,
    new.delivered_at,
    new.failed_at,
    new.accepted_at,
    new.created_at,
    now()
  );

  v_details := concat_ws(
    ' • ',
    nullif(new.status, ''),
    case when nullif(new.payment_plan, '') is not null then concat('Plan: ', new.payment_plan) end,
    case when new.payment_amount is not null then concat('Amount: Rs ', trim(to_char(new.payment_amount, 'FM999999990.00'))) end,
    case when new.payment_months is not null then concat('Months: ', new.payment_months) end,
    case when new.payment_from_date is not null then concat('From: ', new.payment_from_date) end,
    case when new.payment_to_date is not null then concat('To: ', new.payment_to_date) end,
    nullif(new.error_message, ''),
    case when nullif(new.proof_path, '') is not null then concat('payment-proofs/', new.proof_path) end
  );

  insert into public.student_timeline (
    student_id,
    event_type,
    event_date,
    title,
    details,
    changed_by,
    created_at
  )
  values (
    new.student_id,
    'whatsapp_flow',
    v_happened_at::date,
    v_title,
    coalesce(nullif(v_details, ''), 'WhatsApp flow event recorded.'),
    coalesce(nullif(new.created_by, ''), 'WhatsApp'),
    v_happened_at
  );

  return new;
end;
$$;

drop trigger if exists whatsapp_flow_events_log_timeline
on public.whatsapp_flow_events;

create trigger whatsapp_flow_events_log_timeline
after insert on public.whatsapp_flow_events
for each row
execute function public.log_whatsapp_flow_event_timeline();

grant select, insert on public.student_timeline to authenticated;
grant select, insert on public.student_timeline to service_role;
grant select, insert on public.whatsapp_flow_events to authenticated;
grant select, insert on public.whatsapp_flow_events to service_role;
