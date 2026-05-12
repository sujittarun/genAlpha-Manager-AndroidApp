-- Add last_nudge_at column to admissions table for reminder tracking.
alter table public.admissions add column if not exists last_nudge_at timestamptz;

-- Update the sync function to include the column if needed (optional for now).
