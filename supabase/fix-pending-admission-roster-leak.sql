-- Pending admissions must remain in the review queue until approve_admission()
-- creates the roster student. The legacy insert trigger bypassed that rule.

drop trigger if exists admissions_create_student on public.admissions;

-- Remove only dependency-free roster rows that leaked from still-pending
-- admissions. Keep the admission itself intact for manager review/rejection.
delete from public.students s
using public.admissions a
where s.admission_id = a.id
  and a.review_status = 'pending'
  and not exists (
    select 1
    from public.student_payments p
    where p.student_id = s.id
  )
  and not exists (
    select 1
    from public.attendance att
    where att.student_id = s.id
  )
  and not exists (
    select 1
    from public.reminder_events r
    where r.student_id = s.id
  )
  and not exists (
    select 1
    from public.student_timeline t
    where t.student_id = s.id
  );
