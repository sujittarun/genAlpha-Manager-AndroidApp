-- Keep reminders manual but make button-triggered sends live.
-- The app no longer exposes safety/dry-run flags in the manager UI.

insert into public.system_settings (setting_key, setting_value, updated_by, updated_at)
values
  ('whatsapp_reminders_enabled', 'true'::jsonb, 'system', now()),
  ('payment_links_enabled', 'true'::jsonb, 'system', now()),
  ('dry_run_mode', 'false'::jsonb, 'system', now())
on conflict (setting_key) do update
set
  setting_value = excluded.setting_value,
  updated_by = excluded.updated_by,
  updated_at = excluded.updated_at;
