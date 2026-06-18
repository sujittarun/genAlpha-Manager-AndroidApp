-- Data correction: Mahasvin K's original academy join date was overwritten
-- during the discontinued/rejoin cleanup. Keep the June 11 rejoin and renewal
-- cycle intact, but restore the original admission join date used by roster type
-- and training-duration displays.

begin;

update public.students
set
  join_date = date '2026-03-30',
  updated_by = 'codex-data-correction'
where id = '2ae15939-e02b-4b43-ba02-d16471984789'::uuid
  and join_date is distinct from date '2026-03-30';

insert into public.student_timeline (
  student_id,
  event_type,
  event_date,
  title,
  details,
  changed_by
)
select
  '2ae15939-e02b-4b43-ba02-d16471984789'::uuid,
  'data_correction',
  date '2026-06-18',
  'Original join date restored',
  'Join date restored to 2026-03-30. Rejoin date and renewal cycle remain 2026-06-11.',
  'codex-data-correction'
where not exists (
  select 1
  from public.student_timeline existing
  where existing.student_id = '2ae15939-e02b-4b43-ba02-d16471984789'::uuid
    and existing.event_type = 'data_correction'
    and existing.title = 'Original join date restored'
    and existing.details like '%2026-03-30%'
);

commit;
