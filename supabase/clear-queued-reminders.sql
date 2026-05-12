-- Clear all queued reminder events to reset the system after the aggressive logic
-- This prevents old generated reminders from being processed
delete from public.reminder_events where status = 'queued';

-- Optional: Cancel pending payment link requests associated with these reminders
update public.payment_link_requests 
set status = 'cancelled' 
where status = 'awaiting_parent_choice' 
and created_at < now();
