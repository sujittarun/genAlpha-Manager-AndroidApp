-- View to see consolidated player activity including timeline logs and WhatsApp reminders.
-- This helps verify exactly what was sent and when.

CREATE OR REPLACE VIEW public.player_activity_consolidated
WITH (security_invoker = true) AS
SELECT 
    s.name AS student_name,
    st.created_at AS event_time,
    st.event_type AS activity_type,
    st.title AS activity_title,
    st.details AS activity_details,
    st.changed_by AS performed_by,
    NULL AS status_info
FROM public.students s
JOIN public.student_timeline st ON s.id = st.student_id

UNION ALL

SELECT 
    s.name AS student_name,
    re.created_at AS event_time,
    re.reminder_type AS activity_type,
    'WhatsApp Reminder' AS activity_title,
    re.message_preview AS activity_details,
    re.created_by AS performed_by,
    re.status AS status_info
FROM public.students s
JOIN public.reminder_events re ON s.id = re.student_id;

REVOKE ALL ON public.player_activity_consolidated FROM PUBLIC;
REVOKE ALL ON public.player_activity_consolidated FROM anon;
REVOKE ALL ON public.player_activity_consolidated FROM authenticated;
GRANT SELECT ON public.player_activity_consolidated TO authenticated;
GRANT SELECT ON public.player_activity_consolidated TO service_role;

-- Helpful query to see today's reminders specifically:
-- SELECT * FROM public.player_activity_consolidated 
-- WHERE event_time >= CURRENT_DATE 
-- AND activity_title = 'WhatsApp Reminder'
-- ORDER BY event_time DESC;
