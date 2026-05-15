-- Incremental migration: detailed WhatsApp reminder/payment audit trail.
-- Safe to run multiple times. Adds first-class timestamps and an append-only
-- event ledger so we can troubleshoot every parent reminder/payment step.

alter table public.reminder_events
  add column if not exists payment_link_sent_at timestamptz,
  add column if not exists payment_attempted_at timestamptz,
  add column if not exists payment_pending_verification_at timestamptz,
  add column if not exists payment_confirmed_at timestamptz,
  add column if not exists confirmation_message_id text not null default '',
  add column if not exists confirmation_sent_at timestamptz,
  add column if not exists confirmation_delivered_at timestamptz,
  add column if not exists confirmation_read_at timestamptz,
  add column if not exists confirmation_failed_at timestamptz,
  add column if not exists confirmation_meta_response jsonb not null default '{}'::jsonb,
  add column if not exists confirmation_meta_error jsonb not null default '{}'::jsonb;

alter table public.payment_link_requests
  add column if not exists payment_link_sent_at timestamptz,
  add column if not exists payment_attempted_at timestamptz,
  add column if not exists payment_pending_verification_at timestamptz,
  add column if not exists payment_confirmed_at timestamptz;

create table if not exists public.whatsapp_flow_events (
  id uuid primary key default gen_random_uuid(),
  flow_step bigserial not null,
  student_id uuid references public.students(id) on delete cascade,
  admission_id uuid references public.admissions(id) on delete set null,
  reminder_event_id uuid references public.reminder_events(id) on delete set null,
  payment_link_request_id uuid references public.payment_link_requests(id) on delete set null,
  event_type text not null,
  direction text not null default 'system',
  channel text not null default 'whatsapp',
  parent_phone text not null default '',
  message_kind text not null default '',
  message_body text not null default '',
  message_id text not null default '',
  status text not null default '',
  status_at timestamptz,
  sent_at timestamptz,
  accepted_at timestamptz,
  delivered_at timestamptz,
  read_at timestamptz,
  failed_at timestamptz,
  error_code text not null default '',
  error_message text not null default '',
  payment_plan text not null default '',
  payment_amount numeric(10, 2),
  payment_months integer,
  payment_from_date date,
  payment_to_date date,
  proof_bucket text not null default '',
  proof_path text not null default '',
  provider_payload jsonb not null default '{}'::jsonb,
  created_by text not null default 'System',
  created_at timestamptz not null default now()
);

create index if not exists whatsapp_flow_events_student_created_idx
on public.whatsapp_flow_events (student_id, created_at desc);

create index if not exists whatsapp_flow_events_admission_created_idx
on public.whatsapp_flow_events (admission_id, created_at desc);

create index if not exists whatsapp_flow_events_reminder_step_idx
on public.whatsapp_flow_events (reminder_event_id, flow_step desc);

create index if not exists whatsapp_flow_events_message_idx
on public.whatsapp_flow_events (message_id)
where message_id <> '';

create index if not exists whatsapp_flow_events_status_idx
on public.whatsapp_flow_events (status, created_at desc);

alter table public.whatsapp_flow_events enable row level security;

drop policy if exists "whatsapp_flow_events_authenticated_read"
on public.whatsapp_flow_events;

create policy "whatsapp_flow_events_authenticated_read"
on public.whatsapp_flow_events
for select
to authenticated
using (true);

drop policy if exists "whatsapp_flow_events_authenticated_insert"
on public.whatsapp_flow_events;

create policy "whatsapp_flow_events_authenticated_insert"
on public.whatsapp_flow_events
for insert
to authenticated
with check (true);

grant select, insert on public.whatsapp_flow_events to authenticated;
grant usage, select on sequence public.whatsapp_flow_events_flow_step_seq to authenticated;

create or replace function public.whatsapp_flow_event_title(p_event_type text, p_status text)
returns text
language sql
stable
as $$
  select case
    when p_event_type = 'reminder_created' then 'WhatsApp reminder prepared'
    when p_event_type = 'reminder_send_failed' then 'WhatsApp reminder failed'
    when p_event_type = 'reminder_message_status' then concat('Reminder ', coalesce(nullif(p_status, ''), 'status updated'))
    when p_event_type = 'whatsapp_message_status' then concat('WhatsApp message ', coalesce(nullif(p_status, ''), 'status updated'))
    when p_event_type = 'parent_plan_selected' then 'Parent selected renewal plan'
    when p_event_type = 'payment_link_sent' then 'Payment link sent'
    when p_event_type = 'payment_attempted' then 'Parent tapped Pay Now'
    when p_event_type = 'payment_followup_message_sent' then 'Payment follow-up sent'
    when p_event_type = 'payment_pending_verification' then 'Parent payment proof received'
    when p_event_type = 'payment_verification_reply_sent' then 'Payment verification reply sent'
    when p_event_type = 'payment_confirmed' then 'Payment confirmed by academy'
    when p_event_type = 'confirmation_message_sent' then 'Payment confirmation sent'
    when p_event_type = 'confirmation_message_status' then concat('Confirmation ', coalesce(nullif(p_status, ''), 'status updated'))
    when p_event_type = 'parent_help_requested' then 'Parent requested help'
    when p_event_type = 'help_reply_sent' then 'Help message sent'
    when p_event_type = 'admission_reminder_sent' then 'Admission payment reminder sent'
    when p_event_type = 'admission_reminder_failed' then 'Admission payment reminder failed'
    else initcap(replace(coalesce(nullif(p_event_type, ''), 'whatsapp_event'), '_', ' '))
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
begin
  if new.student_id is null then
    return new;
  end if;

  v_details := concat_ws(
    ' • ',
    nullif(new.status, ''),
    case when nullif(new.message_kind, '') is not null then concat('Message: ', new.message_kind) end,
    case when nullif(new.parent_phone, '') is not null then concat('Parent: ', new.parent_phone) end,
    case when nullif(new.payment_plan, '') is not null then concat('Plan: ', new.payment_plan) end,
    case when new.payment_amount is not null then concat('Amount: Rs ', trim(to_char(new.payment_amount, 'FM999999990.00'))) end,
    case when new.payment_months is not null then concat('Months: ', new.payment_months) end,
    case when new.payment_from_date is not null then concat('From: ', new.payment_from_date) end,
    case when new.payment_to_date is not null then concat('To: ', new.payment_to_date) end,
    nullif(new.error_message, ''),
    case when nullif(new.proof_path, '') is not null then concat('payment-proofs/', new.proof_path) end,
    case when nullif(new.message_body, '') is not null then left(new.message_body, 180) end
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
    'whatsapp_flow',
    coalesce(new.status_at, new.created_at)::date,
    public.whatsapp_flow_event_title(new.event_type, new.status),
    coalesce(nullif(v_details, ''), 'WhatsApp flow event recorded.'),
    coalesce(nullif(new.created_by, ''), 'WhatsApp')
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
