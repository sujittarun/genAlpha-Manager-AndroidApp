-- Resume parent WhatsApp reminders after an operational pause.
-- Requires a Supabase Vault secret named whatsapp_cron_secret whose value
-- matches the WHATSAPP_CRON_SECRET configured on the whatsapp-reminder function.

create extension if not exists pg_net;
create extension if not exists pg_cron;

insert into public.system_settings (
  setting_key,
  setting_value,
  updated_by,
  updated_at
)
values (
  'whatsapp_reminders_enabled',
  'true'::jsonb,
  'system_resume',
  now()
)
on conflict (setting_key) do update
set
  setting_value = excluded.setting_value,
  updated_by = excluded.updated_by,
  updated_at = excluded.updated_at;

select cron.unschedule(jobname)
from cron.job
where jobname in ('daily-whatsapp-reminder', 'whatsapp-retry-reminders');

select cron.schedule(
  'daily-whatsapp-reminder',
  '30 9 * * *',
  $cron$
  select net.http_post(
    url := 'https://hwxhigwaklzedxufwedv.supabase.co/functions/v1/whatsapp-reminder',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'x-cron-secret', (
        select decrypted_secret
        from vault.decrypted_secrets
        where name = 'whatsapp_cron_secret'
        limit 1
      )
    ),
    body := '{"action":"auto_schedule"}'::jsonb
  ) as request_id;
  $cron$
);

select cron.schedule(
  'whatsapp-retry-reminders',
  '*/5 * * * *',
  $cron$
  select net.http_post(
    url := 'https://hwxhigwaklzedxufwedv.supabase.co/functions/v1/whatsapp-reminder',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'x-cron-secret', (
        select decrypted_secret
        from vault.decrypted_secrets
        where name = 'whatsapp_cron_secret'
        limit 1
      )
    ),
    body := '{"action":"retry_due_reminders"}'::jsonb
  ) as request_id;
  $cron$
);
