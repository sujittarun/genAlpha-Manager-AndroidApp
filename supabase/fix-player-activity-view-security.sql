-- Make the consolidated activity helper respect the caller's permissions/RLS.
-- This clears the Supabase security-definer-view advisor warning.

alter view public.player_activity_consolidated
set (security_invoker = true);

revoke all on public.player_activity_consolidated from public;
revoke all on public.player_activity_consolidated from anon;
revoke all on public.player_activity_consolidated from authenticated;

grant select on public.player_activity_consolidated to authenticated;
grant select on public.player_activity_consolidated to service_role;

comment on view public.player_activity_consolidated is
  'Authenticated-only helper view for player timeline/reminder activity. Uses security_invoker so underlying table RLS applies.';
