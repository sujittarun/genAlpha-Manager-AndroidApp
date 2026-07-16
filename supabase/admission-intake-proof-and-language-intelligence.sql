begin;

alter table public.admission_ai_extractions
  add column if not exists provider_usage jsonb not null default '{}'::jsonb;

create table if not exists public.admission_intake_reply_interpretations (
  id uuid primary key default gen_random_uuid(),
  session_id uuid not null references public.admission_intake_sessions(id) on delete cascade,
  message_id uuid references public.admission_intake_messages(id) on delete set null,
  provider_message_id text not null default '',
  message_text text not null default '',
  deterministic_intent text not null default 'unknown'
    check (deterministic_intent in ('confirm', 'reject', 'correction', 'unknown')),
  model_intent text not null default ''
    check (model_intent in ('', 'confirm', 'reject', 'correction', 'unknown')),
  final_intent text not null
    check (final_intent in ('confirm', 'reject', 'correction', 'unknown')),
  confidence numeric(4, 3) not null default 0,
  mentioned_plan text not null default '',
  contains_new_facts boolean not null default false,
  reason text not null default '',
  model text not null default '',
  provider_response_id text not null default '',
  provider_usage jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create index if not exists admission_intake_reply_interpretations_session_idx
on public.admission_intake_reply_interpretations (session_id, created_at desc);

alter table public.admission_intake_reply_interpretations enable row level security;

drop policy if exists admission_intake_reply_interpretations_manager_all
on public.admission_intake_reply_interpretations;

create policy admission_intake_reply_interpretations_manager_all
on public.admission_intake_reply_interpretations
for all to authenticated
using (true)
with check (true);

grant select, insert, update, delete
on public.admission_intake_reply_interpretations
to authenticated, service_role;

create or replace function public.backfill_intake_payment_proof_path(
  p_session_id uuid,
  p_proof_path text
)
returns text
language plpgsql
security definer
set search_path = public
as $$
declare
  v_payment public.student_payments%rowtype;
  v_existing_timeline_ids uuid[] := '{}';
  v_old_marker text;
  v_new_marker text;
begin
  if coalesce(trim(p_proof_path), '') = '' then
    raise exception 'Canonical payment proof path is required';
  end if;

  select p.*
  into v_payment
  from public.admission_intake_sessions s
  join public.student_payments p on p.id = s.renewal_payment_id
  where s.id = p_session_id
  for update of p;

  if not found then
    raise exception 'Confirmed renewal payment not found for intake session %', p_session_id;
  end if;

  if v_payment.proof_path = p_proof_path then
    return p_proof_path;
  end if;

  select coalesce(array_agg(id), '{}')
  into v_existing_timeline_ids
  from public.student_timeline
  where student_id = v_payment.student_id;

  v_old_marker := 'payment-proofs/' || coalesce(v_payment.proof_path, '');
  v_new_marker := 'payment-proofs/' || p_proof_path;

  update public.student_payments
  set proof_path = p_proof_path
  where id = v_payment.id;

  -- Updating a payment normally creates a useful audit event. This maintenance
  -- update only relocates an existing proof, so remove only the row created by
  -- this transaction and preserve the original renewal event.
  delete from public.student_timeline
  where student_id = v_payment.student_id
    and event_type = 'payment_updated'
    and not (id = any(v_existing_timeline_ids))
    and details like '%' || v_new_marker || '%';

  if v_payment.proof_path is not null and v_payment.proof_path <> '' then
    update public.student_timeline
    set details = replace(details, v_old_marker, v_new_marker)
    where student_id = v_payment.student_id
      and details like '%' || v_old_marker || '%';
  end if;

  return p_proof_path;
end;
$$;

revoke all on function public.backfill_intake_payment_proof_path(uuid, text) from public, anon, authenticated;
grant execute on function public.backfill_intake_payment_proof_path(uuid, text) to service_role;

commit;
