-- WhatsApp reminder dry-run trigger function only.
-- Run this second, after whatsapp-reminder-tables-only.sql.
-- If Supabase asks about RLS for this file, choose normal run/no automatic RLS helper.

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
