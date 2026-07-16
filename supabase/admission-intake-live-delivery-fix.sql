-- Production hardening for conversational WhatsApp intake.
-- 1. Serializes collecting-session creation so simultaneous text and image
--    webhooks cannot split one conversation into two sessions.
-- 2. Runs the idle-session processor every minute using the existing secure
--    WhatsApp cron secret stored in Supabase Vault.

begin;

create extension if not exists pg_net;
create extension if not exists pg_cron;

create or replace function public.get_or_create_admission_intake_session(
  p_channel text,
  p_source_chat_id text,
  p_source_sender_id text,
  p_source_sender_name text default '',
  p_message_timestamp timestamptz default now()
)
returns setof public.admission_intake_sessions
language plpgsql
security definer
set search_path = public
as $$
declare
  v_session public.admission_intake_sessions%rowtype;
  v_sender_key text;
  v_lock_key text;
begin
  if p_channel not in ('whatsapp', 'whatsapp_group', 'web') then
    raise exception 'Unsupported intake channel.';
  end if;
  if btrim(coalesce(p_source_chat_id, '')) = '' then
    raise exception 'Intake chat ID is required.';
  end if;

  v_sender_key := case when p_channel = 'whatsapp_group'
    then '' else coalesce(p_source_sender_id, '') end;
  v_lock_key := concat('admission-intake:', p_channel, ':', p_source_chat_id, ':', v_sender_key);
  perform pg_advisory_xact_lock(hashtextextended(v_lock_key, 0));

  select * into v_session
  from public.admission_intake_sessions s
  where s.source_chat_id = p_source_chat_id
    and (p_channel = 'whatsapp_group' or s.source_sender_id = coalesce(p_source_sender_id, ''))
    and s.status = 'collecting'
    and s.last_message_at >= now() - interval '30 minutes'
  order by s.last_message_at desc, s.created_at desc
  limit 1
  for update;

  if not found then
    insert into public.admission_intake_sessions (
      channel, source_chat_id, source_sender_id, source_sender_name,
      status, opened_at, last_message_at, expires_at, created_by
    ) values (
      p_channel, p_source_chat_id, coalesce(p_source_sender_id, ''),
      coalesce(p_source_sender_name, ''), 'collecting',
      coalesce(p_message_timestamp, now()), coalesce(p_message_timestamp, now()),
      now() + interval '24 hours',
      case when p_channel = 'web' then 'Manager web intake' else 'WhatsApp intake' end
    ) returning * into v_session;
  end if;

  return next v_session;
end;
$$;

revoke all on function public.get_or_create_admission_intake_session(text, text, text, text, timestamptz)
from public, anon, authenticated;
grant execute on function public.get_or_create_admission_intake_session(text, text, text, text, timestamptz)
to service_role;

do $$
declare
  v_job_id bigint;
begin
  for v_job_id in
    select jobid from cron.job where jobname = 'admission-intake-process-due'
  loop
    perform cron.unschedule(v_job_id);
  end loop;
end;
$$;

select cron.schedule(
  'admission-intake-process-due',
  '* * * * *',
  $cron$
  select net.http_post(
    url := 'https://hwxhigwaklzedxufwedv.supabase.co/functions/v1/admission-intake',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'Authorization', concat('Bearer ', (
        select decrypted_secret
        from vault.decrypted_secrets
        where name = 'supabase_anon_jwt'
        limit 1
      )),
      'apikey', (
        select decrypted_secret
        from vault.decrypted_secrets
        where name = 'supabase_anon_jwt'
        limit 1
      ),
      'x-cron-secret', (
        select decrypted_secret
        from vault.decrypted_secrets
        where name = 'whatsapp_cron_secret'
        limit 1
      )
    ),
    body := '{"action":"process_due"}'::jsonb
  ) as request_id;
  $cron$
);

notify pgrst, 'reload schema';

commit;
