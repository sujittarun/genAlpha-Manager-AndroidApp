-- Per-player automatic WhatsApp reminder pause.
-- Safe to run repeatedly.

alter table public.students
  add column if not exists whatsapp_reminders_paused boolean not null default false,
  add column if not exists whatsapp_reminders_paused_at timestamptz,
  add column if not exists whatsapp_reminders_paused_by text not null default '';

create index if not exists students_whatsapp_reminders_paused_idx
on public.students (whatsapp_reminders_paused)
where whatsapp_reminders_paused = true;

create or replace function public.set_student_reminder_pause_audit()
returns trigger
language plpgsql
security invoker
set search_path = public
as $$
begin
  if new.whatsapp_reminders_paused is distinct from old.whatsapp_reminders_paused then
    if new.whatsapp_reminders_paused then
      new.whatsapp_reminders_paused_at := now();
      new.whatsapp_reminders_paused_by :=
        coalesce(nullif(new.updated_by, ''), nullif(new.added_by, ''), 'Manager');
    else
      new.whatsapp_reminders_paused_at := null;
      new.whatsapp_reminders_paused_by := '';
    end if;
  end if;
  return new;
end;
$$;

drop trigger if exists students_reminder_pause_audit on public.students;
create trigger students_reminder_pause_audit
before update of whatsapp_reminders_paused on public.students
for each row
execute function public.set_student_reminder_pause_audit();

comment on column public.students.whatsapp_reminders_paused is
  'When true, scheduled WhatsApp reminders and automatic retries are skipped for this player. Manual sends remain available.';

