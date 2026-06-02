-- Incremental migration: run due WhatsApp retry reminders every 5 minutes.
-- This clones the existing daily-whatsapp-reminder URL/auth headers so the
-- service-role token is not written into this repo.

create extension if not exists pg_net;
create extension if not exists pg_cron;

do $$
begin
  if exists (select 1 from cron.job where jobname = 'whatsapp-retry-reminders') then
    perform cron.unschedule('whatsapp-retry-reminders');
  end if;
end;
$$;

select cron.schedule(
  'whatsapp-retry-reminders',
  '*/5 * * * *',
  replace(
    (
      select command
      from cron.job
      where jobname = 'daily-whatsapp-reminder'
      order by jobid desc
      limit 1
    ),
    '{"action": "auto_schedule"}',
    '{"action": "retry_due_reminders"}'
  )
);
