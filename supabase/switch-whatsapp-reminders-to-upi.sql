-- Safe update: keeps existing reminder/payment logs and switches future links to UPI.
-- Run this once if you want the database defaults to match the app code.

insert into public.system_settings (setting_key, setting_value, updated_by)
values
  ('payment_links_enabled', 'false'::jsonb, 'System'),
  ('academy_manager_phone', '"8143960950"'::jsonb, 'System')
on conflict (setting_key) do update
set
  setting_value = excluded.setting_value,
  updated_by = excluded.updated_by,
  updated_at = now();

alter table public.payment_link_requests
alter column provider set default 'upi';
