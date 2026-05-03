-- Fix/resume migration for WhatsApp reminder dry-run support.
-- Run this if the first migration stopped at the log_reminder_event_timeline() function.

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
