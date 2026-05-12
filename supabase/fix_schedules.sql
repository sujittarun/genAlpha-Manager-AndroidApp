-- 1. Remove old schedules
select cron.unschedule('daily-whatsapp-reminder');
select cron.unschedule('weekly-data-backup-gmail');

-- 2. Daily Reminder (3:00 PM IST / 9:30 AM UTC)
select cron.schedule(
    'daily-whatsapp-reminder',
    '30 9 * * *',
    $$
    select
      net.http_post(
        url:='https://hwxhigwaklzedxufwedv.supabase.co/functions/v1/whatsapp-reminder',
        headers:='{"Content-Type": "application/json", "Authorization": "Bearer YOUR_SERVICE_ROLE_KEY"}'::jsonb,
        body:='{"action": "auto_schedule"}'::jsonb
      ) as request_id;
    $$
);

-- 3. Weekly Backup (10:30 PM IST / 5:00 PM UTC)
select cron.schedule(
    'weekly-data-backup-gmail',
    '0 17 * * 0',
    $$
    select
      net.http_post(
        url:='https://hwxhigwaklzedxufwedv.supabase.co/functions/v1/whatsapp-reminder',
        headers:='{"Content-Type": "application/json", "Authorization": "Bearer YOUR_SERVICE_ROLE_KEY"}'::jsonb,
        body:='{"action": "auto_backup"}'::jsonb
      ) as request_id;
    $$
);
