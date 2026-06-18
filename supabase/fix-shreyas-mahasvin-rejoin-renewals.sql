-- Data correction for Shreyas K and Mahasvin K duplicate rejoin workaround rows.
-- Safe to run more than once: inserts renewal revenue only when the target row is missing,
-- then removes the temporary "(2nd)" student records and their cascaded finance rows.

begin;

insert into public.student_payments (
  student_id,
  payment_type,
  plan_type,
  cycle_start_date,
  months_covered,
  amount,
  paid_on,
  comment,
  recorded_by
)
select
  target.student_id,
  'renewal',
  'custom',
  date '2026-06-11',
  1,
  2500.00,
  date '2026-06-18',
  'Corrected duplicate rejoin record. One-month renewal from 2026-06-11; payment received 2026-06-18.',
  'codex-data-correction'
from (
  values
    ('2ae15939-e02b-4b43-ba02-d16471984789'::uuid),
    ('5b3d33b0-260f-4655-a879-4d6fdb771ee7'::uuid)
) as target(student_id)
where not exists (
  select 1
  from public.student_payments existing
  where existing.student_id = target.student_id
    and existing.payment_type = 'renewal'
    and existing.cycle_start_date = date '2026-06-11'
    and existing.paid_on = date '2026-06-18'
    and existing.amount = 2500.00
);

update public.students
set
  discontinued = false,
  discontinued_at = case
    when id = '2ae15939-e02b-4b43-ba02-d16471984789'::uuid then date '2026-04-28'
    when id = '5b3d33b0-260f-4655-a879-4d6fdb771ee7'::uuid then date '2026-04-30'
    else discontinued_at
  end,
  rejoined_at = date '2026-06-11',
  fee_pause_days = case
    when id = '2ae15939-e02b-4b43-ba02-d16471984789'::uuid then 44
    when id = '5b3d33b0-260f-4655-a879-4d6fdb771ee7'::uuid then 42
    else fee_pause_days
  end,
  renewals = (
    select array_agg(distinct renewal_date order by renewal_date)
    from unnest(coalesce(public.students.renewals, '{}'::date[]) || array[date '2026-06-11']) as renewal_date
  ),
  updated_by = 'codex-data-correction'
where id in (
  '2ae15939-e02b-4b43-ba02-d16471984789'::uuid,
  '5b3d33b0-260f-4655-a879-4d6fdb771ee7'::uuid
);

insert into public.student_timeline (
  student_id,
  event_type,
  event_date,
  title,
  details,
  changed_by
)
select
  target.student_id,
  'data_correction',
  date '2026-06-18',
  'Rejoin renewal corrected',
  'Rejoin date set to 2026-06-11 and Rs 2500 renewal saved from 2026-06-11 to 2026-07-11. Duplicate "(2nd)" record removed.',
  'codex-data-correction'
from (
  values
    ('2ae15939-e02b-4b43-ba02-d16471984789'::uuid),
    ('5b3d33b0-260f-4655-a879-4d6fdb771ee7'::uuid)
) as target(student_id)
where not exists (
  select 1
  from public.student_timeline existing
  where existing.student_id = target.student_id
    and existing.event_type = 'data_correction'
    and existing.title = 'Rejoin renewal corrected'
    and existing.details like '%Rs 2500 renewal saved from 2026-06-11%'
);

delete from public.student_payments
where student_id in (
  '83bae4c3-4b5f-48d4-9041-e344c9cc5467'::uuid,
  '6408bdeb-0588-4d03-9036-7a26eb8ab5a5'::uuid
);

delete from public.students
where id in (
  '83bae4c3-4b5f-48d4-9041-e344c9cc5467'::uuid,
  '6408bdeb-0588-4d03-9036-7a26eb8ab5a5'::uuid
);

commit;
