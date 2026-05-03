-- Full safe rerun migration for WhatsApp renewal reminder dry-run support.
-- Use this if the earlier migration failed before creating reminder_events.
-- Safe to run multiple times. Does not send WhatsApp messages or create Razorpay links.

create table if not exists public.system_settings (
  setting_key text primary key,
  setting_value jsonb not null default '{}'::jsonb,
  updated_by text not null default 'System',
  updated_at timestamptz not null default timezone('utc', now())
);

insert into public.system_settings (setting_key, setting_value, updated_by)
values
  ('whatsapp_reminders_enabled', 'false'::jsonb, 'System'),
  ('payment_links_enabled', 'false'::jsonb, 'System'),
  ('dry_run_mode', 'true'::jsonb, 'System'),
  ('academy_manager_phone', '"9059962499"'::jsonb, 'System')
on conflict (setting_key) do nothing;

create table if not exists public.reminder_events (
  id uuid primary key default gen_random_uuid(),
  student_id uuid not null references public.students(id) on delete cascade,
  reminder_type text not null default 'renewal',
  channel text not null default 'whatsapp',
  status text not null default 'dry_run',
  dry_run boolean not null default true,
  due_date date,
  overdue_days integer not null default 0,
  plan_options text[] not null default array['monthly', 'quarterly', 'halfyearly', 'need_help']::text[],
  selected_plan text,
  amount numeric(10, 2),
  payment_link_url text,
  payment_link_id text,
  parent_phone text not null default '',
  manager_phone text not null default '9059962499',
  message_preview text not null default '',
  help_requested boolean not null default false,
  created_by text not null default 'System',
  created_at timestamptz not null default timezone('utc', now())
);

create index if not exists reminder_events_student_created_idx
on public.reminder_events (student_id, created_at desc);

create index if not exists reminder_events_status_created_idx
on public.reminder_events (status, created_at desc);

create table if not exists public.payment_link_requests (
  id uuid primary key default gen_random_uuid(),
  reminder_event_id uuid references public.reminder_events(id) on delete set null,
  student_id uuid not null references public.students(id) on delete cascade,
  payment_type text not null default 'renewal',
  plan_type text not null default 'awaiting_parent_choice',
  months_covered integer not null default 0,
  amount numeric(10, 2) not null default 0,
  cycle_start_date date,
  provider text not null default 'razorpay',
  status text not null default 'dry_run',
  dry_run boolean not null default true,
  payment_link_url text,
  payment_link_id text,
  created_by text not null default 'System',
  created_at timestamptz not null default timezone('utc', now())
);

create index if not exists payment_link_requests_student_created_idx
on public.payment_link_requests (student_id, created_at desc);

create or replace function public.log_reminder_event_timeline()
returns trigger
language plpgsql
security definer
set search_path = public
as $function$
begin
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
    'whatsapp_reminder',
    current_date,
    case
      when new.help_requested then 'Parent requested help'
      when new.reminder_type = 'joining_fee' then 'Joining fee reminder prepared'
      else 'Renewal reminder prepared'
    end,
    concat(
      case when new.dry_run then 'Dry run only. ' else '' end,
      'Status: ', new.status,
      case
        when new.overdue_days > 0 then concat(
          ' - Overdue ',
          new.overdue_days,
          ' day',
          case when new.overdue_days = 1 then '' else 's' end
        )
        else ''
      end,
      case
        when coalesce(new.payment_link_url, '') <> '' then concat(' - Payment link: ', new.payment_link_url)
        else ' - Awaiting parent plan choice'
      end
    ),
    new.created_by
  );

  return new;
end;
$function$;

drop trigger if exists reminder_events_log_timeline on public.reminder_events;

create trigger reminder_events_log_timeline
after insert on public.reminder_events
for each row
execute function public.log_reminder_event_timeline();

alter table public.system_settings enable row level security;
alter table public.reminder_events enable row level security;
alter table public.payment_link_requests enable row level security;

drop policy if exists "system_settings_authenticated_all" on public.system_settings;
create policy "system_settings_authenticated_all"
on public.system_settings
for all
to authenticated
using (true)
with check (true);

drop policy if exists "reminder_events_authenticated_all" on public.reminder_events;
create policy "reminder_events_authenticated_all"
on public.reminder_events
for all
to authenticated
using (true)
with check (true);

drop policy if exists "payment_link_requests_authenticated_all" on public.payment_link_requests;
create policy "payment_link_requests_authenticated_all"
on public.payment_link_requests
for all
to authenticated
using (true)
with check (true);

grant select, insert, update on public.system_settings to authenticated;
grant select, insert, update, delete on public.reminder_events to authenticated;
grant select, insert, update, delete on public.payment_link_requests to authenticated;
