-- Safe incremental migration: store first-admission fee split separately.
-- This keeps amount_paid as the actual amount received while preserving the
-- expected coaching/admission/jersey split for admission review and roster edits.

alter table public.admissions
  add column if not exists fee_plan text not null default 'monthly',
  add column if not exists coaching_fee numeric(10, 2) not null default 0,
  add column if not exists admission_fee numeric(10, 2) not null default 0,
  add column if not exists jersey_amount numeric(10, 2) not null default 0,
  add column if not exists total_fee_amount numeric(10, 2) not null default 0;

alter table public.students
  add column if not exists fee_plan text not null default 'monthly',
  add column if not exists coaching_fee numeric(10, 2) not null default 0,
  add column if not exists admission_fee numeric(10, 2) not null default 0,
  add column if not exists jersey_amount numeric(10, 2) not null default 0,
  add column if not exists total_fee_amount numeric(10, 2) not null default 0;

update public.admissions
set
  fee_plan = coalesce(nullif(fee_plan, ''), 'monthly'),
  jersey_amount = case
    when coalesce(jersey_amount, 0) > 0 then jersey_amount
    else greatest(coalesce(jersey_pairs, 0), 0) * 750
  end,
  admission_fee = case
    when coalesce(admission_fee, 0) > 0 then admission_fee
    else 500
  end,
  coaching_fee = case
    when coalesce(coaching_fee, 0) > 0 then coaching_fee
    else greatest(coalesce(amount_paid, 0) - (greatest(coalesce(jersey_pairs, 0), 0) * 750) - 500, 0)
  end,
  total_fee_amount = case
    when coalesce(total_fee_amount, 0) > 0 then total_fee_amount
    else
      case
        when coalesce(coaching_fee, 0) > 0 then coaching_fee
        else greatest(coalesce(amount_paid, 0) - (greatest(coalesce(jersey_pairs, 0), 0) * 750) - 500, 0)
      end
      + case
          when coalesce(admission_fee, 0) > 0 then admission_fee
          else 500
        end
      + case
          when coalesce(jersey_amount, 0) > 0 then jersey_amount
          else greatest(coalesce(jersey_pairs, 0), 0) * 750
        end
  end
where coalesce(total_fee_amount, 0) = 0
   or coalesce(coaching_fee, 0) = 0
   or coalesce(admission_fee, 0) = 0;

update public.students
set
  fee_plan = coalesce(nullif(fee_plan, ''), 'monthly'),
  jersey_amount = case
    when coalesce(jersey_amount, 0) > 0 then jersey_amount
    else greatest(coalesce(jersey_pairs, 0), 0) * 750
  end,
  admission_fee = case
    when coalesce(admission_fee, 0) > 0 then admission_fee
    else 500
  end,
  coaching_fee = case
    when coalesce(coaching_fee, 0) > 0 then coaching_fee
    else greatest(coalesce(amount_paid, 0) - (greatest(coalesce(jersey_pairs, 0), 0) * 750) - 500, 0)
  end,
  total_fee_amount = case
    when coalesce(total_fee_amount, 0) > 0 then total_fee_amount
    else
      case
        when coalesce(coaching_fee, 0) > 0 then coaching_fee
        else greatest(coalesce(amount_paid, 0) - (greatest(coalesce(jersey_pairs, 0), 0) * 750) - 500, 0)
      end
      + case
          when coalesce(admission_fee, 0) > 0 then admission_fee
          else 500
        end
      + case
          when coalesce(jersey_amount, 0) > 0 then jersey_amount
          else greatest(coalesce(jersey_pairs, 0), 0) * 750
        end
  end
where coalesce(total_fee_amount, 0) = 0
   or coalesce(coaching_fee, 0) = 0
   or coalesce(admission_fee, 0) = 0;

do $$
declare
  v_function record;
begin
  for v_function in
    select oid::regprocedure as signature
    from pg_proc
    where pronamespace = 'public'::regnamespace
      and proname = 'submit_admission_form'
  loop
    execute format('drop function if exists %s', v_function.signature);
  end loop;
end;
$$;

create or replace function public.submit_admission_form(
  p_applicant_name text,
  p_nationality text,
  p_date_of_birth date,
  p_age integer,
  p_gender text,
  p_father_guardian_name text,
  p_alternate_contact_no text,
  p_parent_contact_no text,
  p_city text,
  p_address text,
  p_school_college text,
  p_grade text,
  p_parent_aadhaar_no text,
  p_time_slot text,
  p_join_date date,
  p_fees_paid boolean,
  p_amount_paid numeric,
  p_jersey_size text,
  p_jersey_pairs integer,
  p_batsman_style text,
  p_bowling_styles text[],
  p_ready_to_start boolean,
  p_consent_accepted boolean,
  p_terms_accepted boolean,
  p_payment_method text default 'UPI',
  p_payment_upi_id text default '',
  p_payment_reference text default '',
  p_comments text default '',
  p_filled_by text default 'Parent / Guardian',
  p_fee_plan text default 'monthly',
  p_coaching_fee numeric default 0,
  p_admission_fee numeric default 0,
  p_jersey_amount numeric default 0,
  p_total_fee_amount numeric default 0
)
returns table(id uuid, reg_no bigint)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_reg_no bigint;
begin
  if not p_consent_accepted or not p_terms_accepted then
    raise exception 'Please accept the required consent and terms.';
  end if;

  insert into public.registration_counters (counter_name, next_reg_no)
  values ('admissions', 1001)
  on conflict (counter_name) do nothing;

  update public.registration_counters
  set next_reg_no = next_reg_no + 1
  where counter_name = 'admissions'
  returning next_reg_no - 1 into v_reg_no;

  return query
  insert into public.admissions (
    reg_no,
    applicant_name,
    nationality,
    date_of_birth,
    age,
    gender,
    father_guardian_name,
    emergency_contact_no,
    parent_contact_no,
    city,
    address,
    school_college,
    grade,
    parent_aadhaar_no,
    time_slot,
    join_date,
    fees_paid,
    amount_paid,
    fee_plan,
    coaching_fee,
    admission_fee,
    jersey_amount,
    total_fee_amount,
    jersey_size,
    jersey_pairs,
    payment_method,
    payment_upi_id,
    payment_reference,
    filled_by,
    comments,
    batsman_style,
    bowling_styles,
    ready_to_start,
    consent_accepted,
    terms_accepted
  )
  values (
    v_reg_no,
    p_applicant_name,
    p_nationality,
    p_date_of_birth,
    p_age,
    p_gender,
    p_father_guardian_name,
    p_alternate_contact_no,
    p_parent_contact_no,
    p_city,
    p_address,
    p_school_college,
    coalesce(p_grade, ''),
    p_parent_aadhaar_no,
    p_time_slot,
    p_join_date,
    p_fees_paid,
    p_amount_paid,
    coalesce(nullif(p_fee_plan, ''), 'monthly'),
    greatest(coalesce(p_coaching_fee, 0), 0),
    greatest(coalesce(p_admission_fee, 0), 0),
    greatest(coalesce(p_jersey_amount, 0), 0),
    greatest(coalesce(p_total_fee_amount, 0), 0),
    coalesce(p_jersey_size, ''),
    greatest(coalesce(p_jersey_pairs, 0), 0),
    coalesce(p_payment_method, 'UPI'),
    coalesce(p_payment_upi_id, ''),
    coalesce(p_payment_reference, ''),
    coalesce(p_filled_by, 'Parent / Guardian'),
    coalesce(p_comments, ''),
    p_batsman_style,
    coalesce(p_bowling_styles, '{}'),
    p_ready_to_start,
    p_consent_accepted,
    p_terms_accepted
  )
  returning admissions.id, admissions.reg_no;
end;
$$;

grant execute on function public.submit_admission_form(
  text, text, date, integer, text, text, text, text, text, text, text, text,
  text, text, date, boolean, numeric, text, integer, text, text[],
  boolean, boolean, boolean, text, text, text, text, text, text, numeric,
  numeric, numeric, numeric
) to anon, authenticated;
