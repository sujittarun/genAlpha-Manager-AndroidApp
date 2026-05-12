-- Enable RLS on storage.objects if not already enabled
alter table storage.objects enable row level security;

-- Drop policy if it already exists to avoid errors on rerun
drop policy if exists "Allow authenticated users to read payment proofs" on storage.objects;

-- Create policy to allow authenticated managers to read payment proof screenshots
create policy "Allow authenticated users to read payment proofs"
on storage.objects for select
to authenticated
using ( bucket_id = 'payment-proofs' );
