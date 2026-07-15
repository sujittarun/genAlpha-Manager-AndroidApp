-- Conversational admission intake for WhatsApp and the manager web fallback.
-- Safe incremental migration. AI output remains a draft until a human confirms it.

begin;

create extension if not exists pgcrypto;

create sequence if not exists public.admission_intake_display_seq start 1;

create table if not exists public.admission_intake_sessions (
  id uuid primary key default gen_random_uuid(),
  display_id text not null unique default (
    'GACA-AI-' || to_char(current_date, 'YYYY') || '-' ||
    lpad(nextval('public.admission_intake_display_seq')::text, 4, '0')
  ),
  channel text not null check (channel in ('whatsapp', 'whatsapp_group', 'web')),
  source_chat_id text not null default '',
  source_sender_id text not null default '',
  source_sender_name text not null default '',
  provider_session_key text unique,
  status text not null default 'collecting' check (status in (
    'collecting', 'ready_for_processing', 'processing',
    'waiting_for_confirmation', 'confirmed', 'duplicate_review',
    'rejected', 'expired', 'error'
  )),
  draft jsonb not null default '{}'::jsonb,
  conflicts jsonb not null default '[]'::jsonb,
  missing_fields text[] not null default '{}',
  overall_confidence numeric(4, 3),
  extraction_version integer not null default 0,
  confirmation_message_id text not null default '',
  confirmed_by text not null default '',
  confirmed_at timestamptz,
  opened_at timestamptz not null default now(),
  last_message_at timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '24 hours'),
  error_code text not null default '',
  error_message text not null default '',
  created_by text not null default 'Admission intake',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists admission_intake_sessions_queue_idx
on public.admission_intake_sessions (status, last_message_at desc);

create index if not exists admission_intake_sessions_chat_idx
on public.admission_intake_sessions (source_chat_id, source_sender_id, last_message_at desc);

create table if not exists public.admission_intake_messages (
  id uuid primary key default gen_random_uuid(),
  session_id uuid references public.admission_intake_sessions(id) on delete set null,
  provider_message_id text not null unique,
  source_chat_id text not null default '',
  source_sender_id text not null default '',
  source_sender_name text not null default '',
  reply_to_provider_message_id text not null default '',
  message_type text not null default 'text' check (message_type in (
    'text', 'image', 'document', 'audio', 'video', 'interactive', 'system'
  )),
  text_body text not null default '',
  media_id text not null default '',
  media_mime_type text not null default '',
  media_filename text not null default '',
  storage_bucket text not null default '',
  storage_path text not null default '',
  message_timestamp timestamptz not null default now(),
  raw_payload jsonb not null default '{}'::jsonb,
  processing_status text not null default 'received' check (processing_status in (
    'received', 'assigned', 'downloaded', 'processed', 'ignored', 'error'
  )),
  created_at timestamptz not null default now()
);

create index if not exists admission_intake_messages_session_time_idx
on public.admission_intake_messages (session_id, message_timestamp);

create index if not exists admission_intake_messages_unassigned_idx
on public.admission_intake_messages (source_chat_id, message_timestamp)
where session_id is null;

create table if not exists public.admission_ai_extractions (
  id uuid primary key default gen_random_uuid(),
  session_id uuid not null references public.admission_intake_sessions(id) on delete cascade,
  version integer not null,
  model text not null,
  prompt_version text not null,
  provider_response_id text not null default '',
  source_message_ids uuid[] not null default '{}',
  extracted_data jsonb not null,
  conflicts jsonb not null default '[]'::jsonb,
  missing_fields text[] not null default '{}',
  overall_confidence numeric(4, 3),
  created_at timestamptz not null default now(),
  unique (session_id, version)
);

create table if not exists public.admission_intake_corrections (
  id uuid primary key default gen_random_uuid(),
  session_id uuid not null references public.admission_intake_sessions(id) on delete cascade,
  provider_message_id text not null default '',
  correction_text text not null,
  before_draft jsonb not null,
  patch jsonb not null,
  after_draft jsonb not null,
  interpreted_by text not null default 'AI',
  created_by text not null default 'WhatsApp staff',
  created_at timestamptz not null default now()
);

create unique index if not exists admission_intake_corrections_message_unique
on public.admission_intake_corrections (provider_message_id)
where provider_message_id <> '';

create table if not exists public.admission_payment_claims (
  id uuid primary key default gen_random_uuid(),
  session_id uuid not null references public.admission_intake_sessions(id) on delete restrict,
  admission_id uuid references public.admissions(id) on delete set null,
  student_id uuid references public.students(id) on delete set null,
  student_payment_id uuid references public.student_payments(id) on delete set null,
  amount numeric(10, 2) not null default 0 check (amount >= 0),
  payment_date date,
  payment_time time,
  payment_method text not null default '',
  payment_reference text not null default '',
  utr text not null default '',
  payer_name text not null default '',
  receiver_name text not null default '',
  screenshot_status text not null default 'unknown' check (screenshot_status in (
    'successful', 'failed', 'pending', 'processing', 'unknown'
  )),
  verification_status text not null default 'pending' check (verification_status in (
    'pending', 'conflict', 'verified', 'rejected'
  )),
  confidence numeric(4, 3),
  proof_bucket text not null default '',
  proof_path text not null default '',
  extracted_data jsonb not null default '{}'::jsonb,
  verified_by text not null default '',
  verified_at timestamptz,
  rejection_reason text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (session_id)
);

create unique index if not exists admission_payment_claims_student_payment_unique
on public.admission_payment_claims (student_payment_id)
where student_payment_id is not null;

alter table public.admission_intake_sessions
  add column if not exists admission_id uuid references public.admissions(id) on delete set null;

alter table public.admissions
  add column if not exists intake_session_id uuid references public.admission_intake_sessions(id) on delete set null,
  add column if not exists source_channel text not null default 'public_form',
  add column if not exists source_confidence numeric(4, 3),
  add column if not exists payment_verification_status text not null default '';

create unique index if not exists admissions_intake_session_unique
on public.admissions (intake_session_id)
where intake_session_id is not null;

create or replace function public.touch_admission_intake_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists admission_intake_sessions_touch on public.admission_intake_sessions;
create trigger admission_intake_sessions_touch
before update on public.admission_intake_sessions
for each row execute function public.touch_admission_intake_updated_at();

drop trigger if exists admission_payment_claims_touch on public.admission_payment_claims;
create trigger admission_payment_claims_touch
before update on public.admission_payment_claims
for each row execute function public.touch_admission_intake_updated_at();

create or replace function public.normalize_admission_intake_slot(p_value text)
returns text
language sql
immutable
as $$
  select case regexp_replace(lower(coalesce(p_value, '')), '[^0-9a-z]+', '', 'g')
    when '6am' then '6AM'
    when '600am730am' then '6AM'
    when '730am' then '7:30AM'
    when '730am900am' then '7:30AM'
    when '4pm' then '4PM'
    when '400pm530pm' then '4PM'
    when '530pm' then '5:30PM'
    when '530pm700pm' then '5:30PM'
    when '7pm' then '7PM'
    when '700pm830pm' then '7PM'
    else ''
  end;
$$;

create or replace function public.finalize_admission_intake(
  p_session_id uuid,
  p_confirmation_message_id text default '',
  p_confirmed_by text default 'WhatsApp staff'
)
returns table(admission_id uuid, reg_no bigint, payment_claim_id uuid)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_session public.admission_intake_sessions%rowtype;
  v_draft jsonb;
  v_payment jsonb;
  v_name text;
  v_phone text;
  v_alt_phone text;
  v_slot text;
  v_dob date;
  v_join_date date;
  v_age integer;
  v_plan text;
  v_months integer;
  v_jersey_pairs integer;
  v_coaching numeric(10, 2);
  v_admission_fee numeric(10, 2) := 500;
  v_jersey numeric(10, 2);
  v_total numeric(10, 2);
  v_claim_amount numeric(10, 2);
  v_reg_no bigint;
  v_admission_id uuid;
  v_claim_id uuid;
begin
  select * into v_session
  from public.admission_intake_sessions
  where id = p_session_id
  for update;

  if not found then
    raise exception 'Admission intake session not found.';
  end if;

  if v_session.admission_id is not null then
    return query
    select v_session.admission_id, a.reg_no, c.id
    from public.admissions a
    left join public.admission_payment_claims c on c.session_id = v_session.id
    where a.id = v_session.admission_id;
    return;
  end if;

  if v_session.status not in ('waiting_for_confirmation', 'confirmed') then
    raise exception 'Admission draft must be waiting for confirmation.';
  end if;

  v_draft := v_session.draft;
  v_payment := coalesce(v_draft->'payment', '{}'::jsonb);
  v_name := btrim(coalesce(v_draft->>'applicant_name', ''));
  v_phone := right(regexp_replace(coalesce(v_draft->>'parent_contact_no', ''), '\D', '', 'g'), 10);
  v_alt_phone := right(regexp_replace(coalesce(v_draft->>'alternate_contact_no', ''), '\D', '', 'g'), 10);
  v_slot := public.normalize_admission_intake_slot(v_draft->>'time_slot');
  v_dob := nullif(v_draft->>'date_of_birth', '')::date;
  v_join_date := coalesce(nullif(v_draft->>'join_date', '')::date, current_date);
  v_age := case
    when v_dob is not null then
      extract(year from age(v_join_date, v_dob))::integer
    else nullif(v_draft->>'age', '')::integer
  end;

  if v_name = '' then raise exception 'Student name is required.'; end if;
  if length(v_phone) <> 10 then raise exception 'A valid 10-digit parent contact is required.'; end if;
  if v_dob is null then raise exception 'Date of birth is required.'; end if;
  if v_age not between 4 and 18 then raise exception 'Student age is outside the app admission range.'; end if;
  if v_slot = '' then raise exception 'Batch timing is missing or invalid.'; end if;

  v_plan := lower(coalesce(nullif(v_draft->>'fee_plan', ''), 'monthly'));
  if v_plan not in ('monthly', 'quarterly', 'halfyearly', 'special', 'custom') then
    v_plan := 'monthly';
  end if;
  v_months := greatest(coalesce(nullif(v_draft->>'months_covered', '')::integer, 1), 1);
  v_jersey_pairs := greatest(coalesce(nullif(v_draft->>'jersey_pairs', '')::integer, 0), 0);
  v_coaching := case v_plan
    when 'monthly' then 3500
    when 'quarterly' then 9975
    when 'halfyearly' then 18900
    when 'special' then round(10000 * v_months * case when v_months >= 6 then 0.90 when v_months >= 3 then 0.95 else 1 end, 2)
    else greatest(coalesce(nullif(v_draft->>'custom_coaching_fee', '')::numeric, 0), 0)
  end;
  v_jersey := v_jersey_pairs * 750;
  v_total := v_coaching + v_admission_fee + v_jersey;
  v_claim_amount := greatest(coalesce(nullif(v_payment->>'amount', '')::numeric, 0), 0);

  insert into public.registration_counters (counter_name, next_reg_no)
  values ('admissions', 1001)
  on conflict (counter_name) do nothing;

  update public.registration_counters
  set next_reg_no = next_reg_no + 1
  where counter_name = 'admissions'
  returning next_reg_no - 1 into v_reg_no;

  insert into public.admissions (
    reg_no, applicant_name, nationality, date_of_birth, age, gender,
    father_guardian_name, emergency_contact_no, parent_contact_no,
    city, address, school_college, grade, parent_aadhaar_no,
    time_slot, join_date, fees_paid, amount_paid, fee_plan,
    coaching_fee, admission_fee, jersey_amount, total_fee_amount,
    jersey_size, jersey_pairs, payment_method, payment_upi_id,
    payment_reference, comments, filled_by, batsman_style,
    bowling_styles, ready_to_start, consent_accepted, terms_accepted,
    review_status, intake_session_id, source_channel, source_confidence,
    payment_verification_status
  ) values (
    v_reg_no, v_name, coalesce(nullif(v_draft->>'nationality', ''), 'Indian'),
    v_dob, v_age, coalesce(v_draft->>'gender', ''),
    coalesce(v_draft->>'father_guardian_name', ''), v_alt_phone, v_phone,
    coalesce(v_draft->>'city', ''), coalesce(v_draft->>'address', ''),
    coalesce(v_draft->>'school_college', ''), coalesce(v_draft->>'grade', ''), '',
    v_slot, v_join_date, false, v_claim_amount, v_plan,
    v_coaching, v_admission_fee, v_jersey, v_total,
    coalesce(v_draft->>'jersey_size', ''), v_jersey_pairs,
    coalesce(nullif(v_payment->>'payment_method', ''), 'UPI'),
    coalesce(v_payment->>'upi_id', ''),
    coalesce(nullif(v_payment->>'transaction_id', ''), v_payment->>'utr', ''),
    coalesce(v_draft->>'comments', ''),
    coalesce(nullif(v_draft->>'filled_by', ''), 'Coach'),
    coalesce(v_draft->>'batsman_style', ''), '{}', false, false, false,
    'pending', v_session.id, v_session.channel, v_session.overall_confidence,
    case when v_claim_amount > 0 or coalesce(v_payment->>'proof_path', '') <> ''
      then 'pending_verification' else '' end
  ) returning id into v_admission_id;

  if v_claim_amount > 0 or coalesce(v_payment->>'proof_path', '') <> '' then
    insert into public.admission_payment_claims (
      session_id, admission_id, amount, payment_date, payment_time,
      payment_method, payment_reference, utr, payer_name, receiver_name,
      screenshot_status, verification_status, confidence,
      proof_bucket, proof_path, extracted_data
    ) values (
      v_session.id, v_admission_id, v_claim_amount,
      nullif(v_payment->>'payment_date', '')::date,
      nullif(v_payment->>'payment_time', '')::time,
      coalesce(v_payment->>'payment_method', ''),
      coalesce(v_payment->>'transaction_id', ''), coalesce(v_payment->>'utr', ''),
      coalesce(v_payment->>'payer_name', ''), coalesce(v_payment->>'receiver_name', ''),
      case lower(coalesce(v_payment->>'screenshot_status', 'unknown'))
        when 'successful' then 'successful' when 'failed' then 'failed'
        when 'pending' then 'pending' when 'processing' then 'processing'
        else 'unknown' end,
      case when jsonb_array_length(coalesce(v_session.conflicts, '[]'::jsonb)) > 0
        then 'conflict' else 'pending' end,
      nullif(v_payment->>'confidence', '')::numeric,
      coalesce(v_payment->>'proof_bucket', ''), coalesce(v_payment->>'proof_path', ''),
      v_payment
    ) returning id into v_claim_id;
  end if;

  update public.admission_intake_sessions
  set status = 'confirmed', admission_id = v_admission_id,
      confirmation_message_id = coalesce(p_confirmation_message_id, ''),
      confirmed_by = coalesce(nullif(p_confirmed_by, ''), 'WhatsApp staff'),
      confirmed_at = coalesce(confirmed_at, now()), error_code = '', error_message = ''
  where id = v_session.id;

  return query select v_admission_id, v_reg_no, v_claim_id;
end;
$$;

create or replace function public.verify_admission_payment_claim(
  p_claim_id uuid,
  p_verified_by text default 'Manager',
  p_paid_on date default current_date
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_claim public.admission_payment_claims%rowtype;
  v_admission public.admissions%rowtype;
  v_payment_id uuid;
  v_months integer;
begin
  select * into v_claim from public.admission_payment_claims
  where id = p_claim_id for update;
  if not found then raise exception 'Payment claim not found.'; end if;
  if v_claim.student_payment_id is not null then return v_claim.student_payment_id; end if;
  if v_claim.screenshot_status in ('failed', 'pending', 'processing') then
    raise exception 'The payment screenshot does not show a completed payment.';
  end if;
  if v_claim.amount <= 0 then raise exception 'Payment amount must be greater than zero.'; end if;

  select * into v_admission from public.admissions
  where id = v_claim.admission_id for update;
  if v_admission.approved_student_id is null then
    raise exception 'Approve the admission before verifying its payment.';
  end if;

  v_months := case v_admission.fee_plan
    when 'quarterly' then 3 when 'halfyearly' then 6
    else greatest(coalesce((v_claim.extracted_data->>'months_covered')::integer, 1), 1)
  end;

  insert into public.student_payments (
    student_id, payment_type, plan_type, cycle_start_date, months_covered,
    amount, paid_on, comment, recorded_by, proof_path,
    coaching_fee, admission_fee, jersey_amount, total_fee_amount,
    jersey_size, jersey_pairs
  ) values (
    v_admission.approved_student_id, 'joining', v_admission.fee_plan,
    v_admission.join_date, v_months, v_claim.amount, p_paid_on,
    concat_ws(' • ', 'Admission intake payment verified',
      nullif(v_claim.payment_reference, ''), nullif(v_claim.utr, '')),
    coalesce(nullif(p_verified_by, ''), 'Manager'), v_claim.proof_path,
    v_admission.coaching_fee, v_admission.admission_fee,
    v_admission.jersey_amount, v_admission.total_fee_amount,
    v_admission.jersey_size, v_admission.jersey_pairs
  ) returning id into v_payment_id;

  update public.students
  set fees_paid = true, amount_paid = v_claim.amount,
      payment_method = coalesce(nullif(v_claim.payment_method, ''), payment_method),
      payment_reference = coalesce(nullif(v_claim.payment_reference, ''), nullif(v_claim.utr, ''), payment_reference),
      payment_status = 'paid', updated_by = coalesce(nullif(p_verified_by, ''), 'Manager')
  where id = v_admission.approved_student_id;

  update public.admissions
  set fees_paid = true, amount_paid = v_claim.amount,
      payment_verification_status = 'verified'
  where id = v_admission.id;

  update public.admission_payment_claims
  set student_id = v_admission.approved_student_id,
      student_payment_id = v_payment_id, verification_status = 'verified',
      verified_by = coalesce(nullif(p_verified_by, ''), 'Manager'), verified_at = now()
  where id = v_claim.id;

  return v_payment_id;
end;
$$;

-- Keep the new claim ledger in sync when the existing web/Android approval and
-- payment actions are used. This preserves backward compatibility while the UI
-- gradually moves to the first-class claim RPC.
create or replace function public.link_admission_claim_to_approved_student()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if new.approved_student_id is not null
     and new.approved_student_id is distinct from old.approved_student_id then
    update public.admission_payment_claims
    set student_id = new.approved_student_id
    where admission_id = new.id;
  end if;
  return new;
end;
$$;

drop trigger if exists admissions_link_intake_payment_claim on public.admissions;
create trigger admissions_link_intake_payment_claim
after update of approved_student_id on public.admissions
for each row execute function public.link_admission_claim_to_approved_student();

create or replace function public.reconcile_admission_claim_from_payment()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_claim_id uuid;
  v_admission_id uuid;
begin
  if new.payment_type <> 'joining' then return new; end if;

  select c.id, c.admission_id into v_claim_id, v_admission_id
  from public.admission_payment_claims c
  where c.student_id = new.student_id
    and c.verification_status in ('pending', 'conflict')
    and c.student_payment_id is null
  order by c.created_at
  limit 1
  for update;

  if v_claim_id is not null then
    update public.admission_payment_claims
    set student_payment_id = new.id, verification_status = 'verified',
        verified_by = new.recorded_by, verified_at = now()
    where id = v_claim_id;

    update public.admissions
    set fees_paid = true, amount_paid = new.amount,
        payment_verification_status = 'verified'
    where id = v_admission_id;
  end if;
  return new;
end;
$$;

drop trigger if exists student_payments_reconcile_intake_claim on public.student_payments;
create trigger student_payments_reconcile_intake_claim
after insert on public.student_payments
for each row execute function public.reconcile_admission_claim_from_payment();

alter table public.admission_intake_sessions enable row level security;
alter table public.admission_intake_messages enable row level security;
alter table public.admission_ai_extractions enable row level security;
alter table public.admission_intake_corrections enable row level security;
alter table public.admission_payment_claims enable row level security;

do $$
declare v_table text;
begin
  foreach v_table in array array[
    'admission_intake_sessions', 'admission_intake_messages',
    'admission_ai_extractions', 'admission_intake_corrections',
    'admission_payment_claims'
  ] loop
    execute format('drop policy if exists %I on public.%I', v_table || '_manager_all', v_table);
    execute format(
      'create policy %I on public.%I for all to authenticated using (true) with check (true)',
      v_table || '_manager_all', v_table
    );
    execute format('grant select, insert, update, delete on public.%I to authenticated, service_role', v_table);
  end loop;
end;
$$;

grant usage, select on sequence public.admission_intake_display_seq to authenticated, service_role;
grant execute on function public.finalize_admission_intake(uuid, text, text) to authenticated, service_role;
grant execute on function public.verify_admission_payment_claim(uuid, text, date) to authenticated, service_role;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'admission-intake', 'admission-intake', false, 15728640,
  array['image/jpeg', 'image/png', 'image/webp', 'application/pdf']
)
on conflict (id) do update set
  public = false,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Managers upload admission intake files" on storage.objects;
create policy "Managers upload admission intake files"
on storage.objects for insert to authenticated
with check (bucket_id = 'admission-intake');

drop policy if exists "Managers read admission intake files" on storage.objects;
create policy "Managers read admission intake files"
on storage.objects for select to authenticated
using (bucket_id = 'admission-intake');

notify pgrst, 'reload schema';

commit;
