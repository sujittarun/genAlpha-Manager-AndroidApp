-- Narrow live-data correction for Sharvan's 3 Aug 2026 one-month rejoin renewal.
-- Safe to rerun: every write is restricted to the known student/payment IDs.
begin;

update public.student_payments
set
  cycle_start_date = date '2026-08-03',
  comment = concat_ws(
    ' ',
    nullif(trim(coalesce(comment, '')), ''),
    'Corrected rejoin renewal cycle: one month from 2026-08-03.'
  )
where id = '25cb38d2-a089-4e4b-8136-a2aea1f7c84a'::uuid
  and student_id = '2fc84484-1388-4f07-9aa4-cd2211bc41b0'::uuid
  and cycle_start_date = date '2026-07-29'
  and months_covered = 1;

update public.students
set
  renewals = (
    select array_agg(distinct renewal_date order by renewal_date)
    from unnest(
      array_remove(coalesce(public.students.renewals, '{}'::date[]), date '2026-07-29') ||
      array[date '2026-08-03']
    ) as renewal_date
  ),
  updated_by = 'codex-rejoin-cycle-correction'
where id = '2fc84484-1388-4f07-9aa4-cd2211bc41b0'::uuid
  and rejoined_at = date '2026-08-03';

insert into public.student_timeline (
  student_id,
  event_type,
  event_date,
  title,
  details,
  changed_by
)
select
  '2fc84484-1388-4f07-9aa4-cd2211bc41b0'::uuid,
  'data_correction',
  date '2026-08-03',
  'Rejoin renewal cycle corrected',
  'One-month renewal corrected to run from 03 Aug 2026 through 03 Sep 2026.',
  'Codex'
where not exists (
  select 1
  from public.student_timeline existing
  where existing.student_id = '2fc84484-1388-4f07-9aa4-cd2211bc41b0'::uuid
    and existing.event_type = 'data_correction'
    and existing.title = 'Rejoin renewal cycle corrected'
);

commit;
