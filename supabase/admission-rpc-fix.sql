create table if not exists public.registration_counters (
  counter_name text primary key,
  next_reg_no bigint not null
);

insert into public.registration_counters (counter_name, next_reg_no)
values (
  'admissions',
  greatest(
    coalesce((select max(reg_no) + 1 from public.admissions), 1001),
    1001
  )
)
on conflict (counter_name) do update
set next_reg_no = greatest(
  public.registration_counters.next_reg_no,
  excluded.next_reg_no,
  coalesce((select max(reg_no) + 1 from public.admissions), 1001)
);

drop function if exists public.submit_admission_form(
  text, text, date, integer, text, text, text, text, text, text, text, text,
  text, date, boolean, numeric, text, text[], boolean, boolean, boolean
);

drop function if exists public.peek_next_admission_reg_no();

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
  p_parent_aadhaar_no text,
  p_time_slot text,
  p_join_date date,
  p_fees_paid boolean,
  p_amount_paid numeric,
  p_batsman_style text,
  p_bowling_styles text[],
  p_ready_to_start boolean,
  p_consent_accepted boolean,
  p_terms_accepted boolean
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

  update public.registration_counters
  set next_reg_no = next_reg_no + 1
  where counter_name = 'admissions'
  returning next_reg_no - 1 into v_reg_no;

  if v_reg_no is null then
    insert into public.registration_counters (counter_name, next_reg_no)
    values ('admissions', 1002)
    on conflict (counter_name) do nothing;
    v_reg_no := 1001;
  end if;

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
    parent_aadhaar_no,
    time_slot,
    join_date,
    fees_paid,
    amount_paid,
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
    p_parent_aadhaar_no,
    p_time_slot,
    p_join_date,
    p_fees_paid,
    p_amount_paid,
    p_batsman_style,
    coalesce(p_bowling_styles, '{}'),
    p_ready_to_start,
    p_consent_accepted,
    p_terms_accepted
  )
  returning admissions.id, admissions.reg_no;
end;
$$;

create or replace function public.peek_next_admission_reg_no()
returns table(next_reg_no bigint)
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.registration_counters (counter_name, next_reg_no)
  values (
    'admissions',
    greatest(
      coalesce((select max(reg_no) + 1 from public.admissions), 1001),
      1001
    )
  )
  on conflict (counter_name) do update
  set next_reg_no = greatest(
    public.registration_counters.next_reg_no,
    excluded.next_reg_no,
    coalesce((select max(reg_no) + 1 from public.admissions), 1001)
  );

  return query
  select rc.next_reg_no
  from public.registration_counters rc
  where rc.counter_name = 'admissions';
end;
$$;

grant execute on function public.submit_admission_form(
  text, text, date, integer, text, text, text, text, text, text, text, text,
  text, date, boolean, numeric, text, text[], boolean, boolean, boolean
) to anon, authenticated;

grant execute on function public.peek_next_admission_reg_no() to anon, authenticated;
