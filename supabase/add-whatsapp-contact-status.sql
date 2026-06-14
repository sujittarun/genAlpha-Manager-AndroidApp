-- Persistent WhatsApp contact status for players.
-- Safe to run multiple times.

alter table public.students
  add column if not exists whatsapp_contact_status text not null default 'active';

alter table public.reminder_events
  add column if not exists manual_followup_reason text not null default '';

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'students_whatsapp_contact_status_check'
      and conrelid = 'public.students'::regclass
  ) then
    alter table public.students
      add constraint students_whatsapp_contact_status_check
      check (whatsapp_contact_status in ('active', 'wrong_number', 'opted_out'));
  end if;
end;
$$;

create index if not exists students_whatsapp_contact_status_idx
on public.students (whatsapp_contact_status)
where whatsapp_contact_status <> 'active';

create or replace function public.sync_whatsapp_contact_followup()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_actor text;
begin
  if new.whatsapp_contact_status is not distinct from old.whatsapp_contact_status then
    return new;
  end if;

  v_actor := coalesce(nullif(new.updated_by, ''), 'System');

  if new.whatsapp_contact_status in ('wrong_number', 'opted_out') then
    update public.reminder_events
    set
      status = 'manual_followup',
      manual_followup_required = true,
      manual_followup_reason = case
        when new.whatsapp_contact_status = 'wrong_number' then 'wrong_phone_number'
        else 'whatsapp_opted_out'
      end,
      next_retry_at = null,
      retry_reason = case
        when new.whatsapp_contact_status = 'wrong_number'
          then 'Saved WhatsApp number is marked wrong. Automatic reminders and retries are paused.'
        else 'Parent has opted out of WhatsApp reminders. Automatic reminders and retries are paused.'
      end
    where student_id = new.id
      and delivered_at is null
      and read_at is null
      and status in (
        'queued', 'retry_scheduled', 'failed', 'send_failed',
        'delivery_failed', 'undelivered', 'manual_followup'
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
      new.id,
      'whatsapp_contact_blocked',
      current_date,
      case
        when new.whatsapp_contact_status = 'wrong_number' then 'WhatsApp number marked wrong'
        else 'WhatsApp reminders opted out'
      end,
      'Automatic WhatsApp reminders and queued retries were stopped. Update the contact status after the number is corrected.',
      v_actor
    );
  else
    update public.reminder_events
    set
      status = case when status = 'manual_followup' then 'cancelled' else status end,
      manual_followup_required = false,
      manual_followup_reason = '',
      next_retry_at = null,
      retry_reason = 'WhatsApp contact reactivated. Future reminders may follow the normal schedule.'
    where student_id = new.id
      and manual_followup_reason in ('wrong_phone_number', 'whatsapp_opted_out');

    insert into public.student_timeline (
      student_id,
      event_type,
      event_date,
      title,
      details,
      changed_by
    )
    values (
      new.id,
      'whatsapp_contact_reactivated',
      current_date,
      'WhatsApp contact reactivated',
      'The saved WhatsApp number is active again. Future reminders will follow the normal fee schedule.',
      v_actor
    );
  end if;

  return new;
end;
$$;

drop trigger if exists students_sync_whatsapp_contact_followup on public.students;
create trigger students_sync_whatsapp_contact_followup
after update of whatsapp_contact_status on public.students
for each row
execute function public.sync_whatsapp_contact_followup();

-- Preserve useful reasons for existing manual-follow-up rows.
update public.reminder_events
set manual_followup_reason = case
  when coalesce(retry_reason, '') ilike '%overdue%automatic reminders paused%'
    then 'overdue_15_days'
  when coalesce(retry_reason, '') ilike '%retry limit%'
    or coalesce(retry_reason, '') ilike '%repeatedly%'
    then 'retry_exhausted'
  else 'delivery_failure'
end
where manual_followup_required = true
  and coalesce(manual_followup_reason, '') = '';

-- Known incorrect parent numbers confirmed by the academy manager.
update public.students
set
  whatsapp_contact_status = 'wrong_number',
  updated_by = 'Manager - wrong phone audit'
where lower(trim(name)) in ('leela krishna c', 'jeevan reddy c')
  and whatsapp_contact_status <> 'wrong_number';

grant select, insert, update, delete on public.students to authenticated;
grant select, insert, update, delete on public.students to service_role;
grant select, insert, update, delete on public.reminder_events to authenticated;
grant select, insert, update, delete on public.reminder_events to service_role;

notify pgrst, 'reload schema';
