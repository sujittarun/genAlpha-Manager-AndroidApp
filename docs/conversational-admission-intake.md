# Conversational admission intake

## Production channel decision

Use a dedicated 1:1 Meta WhatsApp Cloud API number for the first production rollout. Staff can forward or send the same informal conversation, handwritten form image, and payment screenshot to that number. No `NEW ADMISSION` or `DONE` command is required.

Do not add the proposed academy number to the existing admission group merely to enable automation. A normal group membership does not create an API webhook. Meta's Groups API is a separate, restricted surface for eligible Cloud API businesses and API-managed small groups. Confirm eligibility in WhatsApp Manager before attempting any group rollout.

Keep the existing group unchanged until all of these are true:

1. The academy business portfolio has the required Official Business Account eligibility for Groups API.
2. The target number is a Cloud API number eligible for Groups API, not only a WhatsApp Business App/coexistence number.
3. The staff workflow fits Meta's current group participant and message-type limits.
4. A test API-created group receives inbound message and media webhooks successfully.

If those checks fail, use the dedicated 1:1 intake or `/intake.html`. Do not use WhatsApp Web DOM automation, unofficial multi-device libraries, or QR-session scraping for production.

## State and persistence

The migration `supabase/conversational-admission-intake.sql` adds:

- `admission_intake_sessions`: one temporary draft and state machine per candidate admission.
- `admission_intake_messages`: idempotent raw message/media ledger keyed by provider message ID.
- `admission_ai_extractions`: immutable, versioned model output.
- `admission_intake_corrections`: before/patch/after audit history.
- `admission_payment_claims`: unverified payment evidence kept separate from finance.
- `finalize_admission_intake(...)`: validates and creates a pending `admissions` row only after confirmation.
- `verify_admission_payment_claim(...)`: creates the joining `student_payments` row and marks fees paid only after manager verification.

Confirmed intake follows the existing app lifecycle:

```text
conversation + files
  -> temporary AI draft
  -> staff confirmation
  -> admissions.review_status = pending
  -> manager approval
  -> students row
  -> manager payment verification
  -> student_payments row + finance revenue
```

The existing web and Android joining-payment actions remain compatible. Database reconciliation triggers link those payments back to the intake claim.

## Edge Functions

Deploy the migration before the functions:

```bash
supabase db push
supabase functions deploy admission-intake
supabase functions deploy whatsapp-reminder
```

Required secrets:

```text
OPENAI_API_KEY
OPENAI_ADMISSION_MODEL=gpt-5.4-mini
META_WHATSAPP_TOKEN
META_WHATSAPP_PHONE_NUMBER_ID
META_ADMISSION_PHONE_NUMBER_ID
ADMISSION_INTAKE_ENABLED=true
ADMISSION_INTAKE_SHARED_NUMBER=false
ADMISSION_INTAKE_WEBHOOK_SECRET=<random-long-secret>
```

`META_ADMISSION_PHONE_NUMBER_ID` is Meta's phone-number asset ID, not the human-readable telephone number.

Keep `ADMISSION_INTAKE_SHARED_NUMBER=false` for rollout. This prevents renewal/payment replies on the current reminder number from being mistaken for new admissions. A shared-number classifier can be added only after an evaluation set proves reliable routing.

## Inactivity processing

The receiver stores messages immediately. A scheduler calls `process_due` after the conversation has been idle for at least 60 seconds, allowing images and text to arrive out of order.

n8n workflow:

```text
Schedule Trigger (every minute)
  -> HTTP Request POST /functions/v1/admission-intake
     headers: Authorization Bearer <service-role>, apikey, Content-Type
     body: {"action":"process_due"}
  -> IF results[].error exists
  -> manager alert / error workflow
```

Use n8n credentials for secrets; never put the service-role key in a workflow export committed to Git.

## Web fallback

Managers who are logged into the existing dashboard see **AI Intake**. The page:

1. accepts a free-form pasted conversation;
2. uploads images or PDFs to the private `admission-intake` bucket;
3. calls the same extraction backend used by WhatsApp;
4. shows the confirmation summary;
5. accepts normal-language corrections;
6. creates the pending admission only after the manager confirms.

The web fallback is also the recommended manual recovery path for unreadable media, provider outages, and messages accidentally sent to the old group.

## Rollout test

1. Apply the migration in a staging Supabase project.
2. Deploy both functions with `ADMISSION_INTAKE_ENABLED=false`.
3. Add the OpenAI key and run `/intake.html` with fake data.
4. Verify that extraction creates only an intake session.
5. Confirm and verify that one pending admission is created.
6. Approve it and verify the payment claim; confirm exactly one player and one `student_payments` row exist.
7. Enable the dedicated Meta test number.
8. Test duplicate webhooks, two staff senders, out-of-order images, corrections, failed screenshots, and missing required fields.
9. Enable the production intake number only after the above passes.
10. Set `window.GEN_ALPHA_FEATURES.aiIntakeEnabled` to `true` in the web app config and deploy the web repo.
