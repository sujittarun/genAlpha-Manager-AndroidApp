# Supabase Setup

1. Create a new Supabase project.
2. Open the SQL editor and run [`supabase/schema.sql`](./supabase/schema.sql).
3. In Supabase, open `Authentication` -> `Users` and create the manager users who should be allowed to edit the app.
4. In the Supabase dashboard, copy:
   - `Project URL`
   - `anon` or `publishable` key
5. Paste those values into [`supabase-config.js`](./supabase-config.js).
6. Redeploy the app to Netlify.

If your `students` table already exists from an older version:
- run [`supabase/add-audit-columns.sql`](./supabase/add-audit-columns.sql) in the SQL editor too
- run [`supabase/add-discontinued-column.sql`](./supabase/add-discontinued-column.sql) in the SQL editor too
- run [`supabase/add-time-slot-column.sql`](./supabase/add-time-slot-column.sql) in the SQL editor too

The app will allow:
- public viewing for everyone
- add/edit/delete/renew for authenticated manager users

Notes:
- Do not put the `service_role` key in the browser.
- The `anon` key is the correct browser key for this app.
