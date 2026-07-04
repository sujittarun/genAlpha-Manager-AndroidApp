-- Data correction for Aarav C special-training admission.
-- Safe to run more than once: updates the existing student/payment rows only.

begin;

update public.students
set
  fee_plan = 'special',
  fees_paid = true,
  payment_status = 'paid',
  amount_paid = 29000.00,
  coaching_fee = 29000.00,
  admission_fee = 0.00,
  jersey_amount = 0.00,
  total_fee_amount = 29000.00,
  updated_by = 'codex-data-correction'
where id = '339908b7-3003-4dcd-a768-edad15ffe8e1'::uuid;

update public.student_payments
set
  payment_type = 'joining',
  plan_type = 'special',
  cycle_start_date = date '2026-07-01',
  months_covered = 3,
  amount = 29000.00,
  paid_on = date '2026-07-03',
  comment = 'Special training paid for 3 months with 5% discount.',
  recorded_by = coalesce(recorded_by, 'codex-data-correction')
where id = 'dd3ce877-6f3c-4af3-b8a3-52522f7f4bef'::uuid
  and student_id = '339908b7-3003-4dcd-a768-edad15ffe8e1'::uuid;

insert into public.student_timeline (
  student_id,
  event_type,
  event_date,
  title,
  details,
  changed_by
)
select
  '339908b7-3003-4dcd-a768-edad15ffe8e1'::uuid,
  'data_correction',
  date '2026-07-03',
  'Special training months corrected',
  'Aarav C marked as special training for 3 months from 2026-07-01. Existing Rs 29000 joining payment kept as revenue; months covered changed from 1 to 3.',
  'codex-data-correction'
where not exists (
  select 1
  from public.student_timeline existing
  where existing.student_id = '339908b7-3003-4dcd-a768-edad15ffe8e1'::uuid
    and existing.event_type = 'data_correction'
    and existing.title = 'Special training months corrected'
);

commit;
