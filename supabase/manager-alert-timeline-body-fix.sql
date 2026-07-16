-- Preserve readable manager payment-alert text from send through delivery/read.
-- Safe to run multiple times.

with manager_alerts as (
  select
    flow.id,
    flow.message_id,
    flow.student_id,
    flow.created_at,
    student.name as player_name,
    reminder.selected_plan,
    reminder.amount,
    reminder.due_date,
    case
      when reminder.selected_plan = 'monthly' then 1
      when reminder.selected_plan = 'quarterly' then 3
      when reminder.selected_plan = 'halfyearly' then 6
      when reminder.selected_plan = 'special' and reminder.amount = 28500 then 3
      when reminder.selected_plan = 'special' and reminder.amount = 54000 then 6
      when reminder.selected_plan = 'special' then greatest(round(reminder.amount / 10000.0), 1)::integer
      else null
    end as payment_months,
    concat_ws(
      E'\n',
      'Payment update for manager verification',
      concat('Player: ', student.name),
      case reminder.selected_plan
        when 'monthly' then 'Plan: 1 Month'
        when 'quarterly' then 'Plan: 3 Months'
        when 'halfyearly' then 'Plan: 6 Months'
        when 'special' then 'Plan: Special Training'
        else null
      end,
      case
        when reminder.selected_plan = 'monthly' then 'Duration: 1 month'
        when reminder.selected_plan = 'quarterly' then 'Duration: 3 months'
        when reminder.selected_plan = 'halfyearly' then 'Duration: 6 months'
        when reminder.selected_plan = 'special' and reminder.amount = 28500 then 'Duration: 3 months'
        when reminder.selected_plan = 'special' and reminder.amount = 54000 then 'Duration: 6 months'
        when reminder.selected_plan = 'special' then concat(
          'Duration: ', greatest(round(reminder.amount / 10000.0), 1)::integer, ' month',
          case when greatest(round(reminder.amount / 10000.0), 1)::integer = 1 then '' else 's' end
        )
        else null
      end,
      case when reminder.amount > 0 then concat('Amount: Rs ', trim(to_char(reminder.amount, 'FM999999990.00'))) end,
      case
        when flow.event_type = 'manager_payment_alert_with_proof_sent' then 'Payment proof attached.'
        else 'Payment proof not received yet.'
      end
    ) as readable_body
  from public.whatsapp_flow_events flow
  join public.students student on student.id = flow.student_id
  left join public.reminder_events reminder on reminder.id = flow.reminder_event_id
  where flow.event_type in (
    'manager_payment_alert_with_proof_sent',
    'manager_payment_alert_without_proof_sent'
  )
)
update public.whatsapp_flow_events flow
set
  message_body = manager_alerts.readable_body,
  payment_plan = coalesce(nullif(flow.payment_plan, ''), manager_alerts.selected_plan, ''),
  payment_amount = coalesce(flow.payment_amount, manager_alerts.amount),
  payment_months = coalesce(flow.payment_months, manager_alerts.payment_months),
  payment_from_date = coalesce(flow.payment_from_date, manager_alerts.due_date)
from manager_alerts
where flow.id = manager_alerts.id
  and (
    flow.message_body is null
    or btrim(flow.message_body) = ''
    or flow.message_body like 'manager_payment_alert%'
  );

-- Meta delivery/read callbacks share the outbound message id. Copy the exact
-- stored body and business fields so every timeline row remains self-contained.
with outbound as (
  select distinct on (message_id)
    message_id,
    message_body,
    payment_plan,
    payment_amount,
    payment_months,
    payment_from_date,
    payment_to_date,
    proof_bucket,
    proof_path
  from public.whatsapp_flow_events
  where direction = 'outbound'
    and message_id is not null
    and btrim(message_id) <> ''
    and message_kind like 'manager_alert%'
  order by message_id, created_at asc
)
update public.whatsapp_flow_events receipt
set
  message_body = outbound.message_body,
  payment_plan = coalesce(nullif(receipt.payment_plan, ''), outbound.payment_plan, ''),
  payment_amount = coalesce(receipt.payment_amount, outbound.payment_amount),
  payment_months = coalesce(receipt.payment_months, outbound.payment_months),
  payment_from_date = coalesce(receipt.payment_from_date, outbound.payment_from_date),
  payment_to_date = coalesce(receipt.payment_to_date, outbound.payment_to_date),
  proof_bucket = coalesce(nullif(receipt.proof_bucket, ''), outbound.proof_bucket, ''),
  proof_path = coalesce(nullif(receipt.proof_path, ''), outbound.proof_path, '')
from outbound
where receipt.message_id = outbound.message_id
  and receipt.event_type = 'whatsapp_message_status';

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

  if new.event_type = 'whatsapp_message_status' and new.message_kind like 'manager_alert%' then
    v_title := case new.status
      when 'delivered' then 'Manager payment alert delivered'
      when 'read' then 'Manager payment alert read'
      when 'failed' then 'Manager payment alert failed'
      else ''
    end;
  else
    v_title := public.whatsapp_flow_event_title(new.event_type, new.status);
  end if;
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
    nullif(new.message_body, ''),
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

-- Repair already-generated manager-alert timeline rows using the nearest
-- outbound alert for the same player.
with nearest_alert as (
  select
    timeline.id,
    flow.message_body,
    row_number() over (
      partition by timeline.id
      order by abs(extract(epoch from (timeline.created_at - coalesce(flow.status_at, flow.created_at))))
    ) as match_rank
  from public.student_timeline timeline
  join public.whatsapp_flow_events flow
    on flow.student_id = timeline.student_id
   and flow.event_type in (
     'manager_payment_alert_with_proof_sent',
     'manager_payment_alert_without_proof_sent'
   )
   and abs(extract(epoch from (timeline.created_at - coalesce(flow.status_at, flow.created_at)))) <= 10
  where timeline.event_type = 'whatsapp_flow'
    and timeline.title in (
      'Manager payment alert sent with proof',
      'Manager payment alert sent'
    )
)
update public.student_timeline timeline
set details = nearest_alert.message_body
from nearest_alert
where timeline.id = nearest_alert.id
  and nearest_alert.match_rank = 1
  and nearest_alert.message_body is not null
  and btrim(nearest_alert.message_body) <> '';
