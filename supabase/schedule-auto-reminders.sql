-- 1. Enable necessary extensions
create extension if not exists pg_net;
create extension if not exists pg_cron;

-- 2. Clear existing schedule if any
select cron.unschedule('whatsapp-auto-reminders');

-- 3. Schedule the reminder task
-- Runs daily at 3:00 PM IST (09:30 AM UTC)
-- Replace [PROJECT_REF] with your actual Supabase project reference (e.g. abcdefghijklm)
-- Replace [SERVICE_ROLE_KEY] with your service_role key from Settings -> API
select
  cron.schedule(
    'whatsapp-auto-reminders',
    '30 9 * * *',
    $$
    select
      net.http_post(
        url := 'https://[PROJECT_REF].supabase.co/functions/v1/whatsapp-reminder',
        headers := '{"Content-Type": "application/json", "Authorization": "Bearer [SERVICE_ROLE_KEY]"}'::jsonb,
        body := '{"action": "auto_schedule"}'::jsonb
      ) as request_id;
    $$
  );

-- Verification: You can check the status of the cron jobs by running:
-- select * from cron.job;
-- select * from cron.job_run_details order by start_time desc limit 10;
