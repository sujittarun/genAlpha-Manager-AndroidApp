-- Display timestamps in India time for Supabase/PostgREST sessions.
-- This does not rewrite stored timestamp data; timestamptz values remain timezone-safe.

alter database postgres set timezone to 'Asia/Kolkata';
alter role authenticator set timezone to 'Asia/Kolkata';
alter role anon set timezone to 'Asia/Kolkata';
alter role authenticated set timezone to 'Asia/Kolkata';
alter role service_role set timezone to 'Asia/Kolkata';
