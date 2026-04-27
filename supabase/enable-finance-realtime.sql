do $$
begin
    if not exists (
        select 1
        from pg_publication_tables
        where pubname = 'supabase_realtime'
          and schemaname = 'public'
          and tablename = 'academy_expenses'
    ) then
        alter publication supabase_realtime add table public.academy_expenses;
    end if;

    if not exists (
        select 1
        from pg_publication_tables
        where pubname = 'supabase_realtime'
          and schemaname = 'public'
          and tablename = 'student_payments'
    ) then
        alter publication supabase_realtime add table public.student_payments;
    end if;
end
$$;
