# Conversational admission intake

## Production channel decision

The academy's existing reminder number can receive 1:1 admission or renewal messages from explicitly allowlisted staff. Staff can forward or send the same informal conversation, handwritten form image, and payment screenshot to that number. No fixed command is required.

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
supabase db query --linked --file supabase/conversational-renewal-intake.sql
supabase functions deploy admission-intake
supabase functions deploy whatsapp-reminder
```

Keep JWT verification enabled for `admission-intake`. The function accepts authenticated manager sessions and the service-role key used internally by the WhatsApp router.

Required secrets:

```text
OPENAI_API_KEY
OPENAI_ADMISSION_MODEL=gpt-5.4-mini
META_WHATSAPP_TOKEN
META_WHATSAPP_PHONE_NUMBER_ID
META_ADMISSION_PHONE_NUMBER_ID=<optional dedicated intake asset ID>
ADMISSION_INTAKE_ENABLED=true
ADMISSION_INTAKE_SHARED_NUMBER=true
ADMISSION_INTAKE_STAFF_PHONES=919876543210,919123456789
ADMISSION_INTAKE_WEBHOOK_SECRET=<random-long-secret>
```

`META_ADMISSION_PHONE_NUMBER_ID` is Meta's phone-number asset ID, not the human-readable telephone number.

For the academy's current setup, leave `META_ADMISSION_PHONE_NUMBER_ID` unset so it safely inherits `META_WHATSAPP_PHONE_NUMBER_ID`, and opt in with `ADMISSION_INTAKE_SHARED_NUMBER=true`. Set `META_ADMISSION_PHONE_NUMBER_ID` only when a different dedicated Cloud API number is introduced. `ADMISSION_INTAKE_STAFF_PHONES` is mandatory and comma-separated; values may include `+91` and spaces. The function normalizes them and fails closed when the list is empty. Messages from parents and other non-allowlisted senders continue to the existing reminder/payment reply handler and are never sent to the admission model.

Adding the number to an ordinary WhatsApp group is still not an ingestion method. Keep it in the group only for normal human communication unless Meta has enabled the separate Groups API for this exact Cloud API phone-number asset and API-created group.

## Existing-player renewals

The same model classifies each conversation as `admission`, `renewal`, or `unknown`. For renewals it extracts player identifiers, plan/months, amount, payment date, UTR/reference, and screenshot status. The application—not the model—then:

1. matches the player deterministically using registration number, parent phone, exact player name, and guardian name;
2. calculates the next cycle from the existing joining/renewal ledger and legacy renewal dates;
3. posts a renewal draft back for staff review;
4. records the `student_payments` row and advances `students.renewals` only after explicit confirmation;
5. blocks duplicate intake sessions, UTRs/payment references, and duplicate renewal cycles.

A failed, pending, or processing screenshot cannot be confirmed. A screenshot or transaction reference is evidence; the allowlisted staff member's explicit confirmation is the payment-verification action.

Messages from multiple allowlisted staff are combined by group ID when the official Groups API supplies that ID. With an ordinary WhatsApp Business App group, Meta does not send those messages to the current webhook. Staff must forward the relevant conversation/screenshots 1:1 to the academy number or use the manager web intake until Groups API eligibility is verified.

## Inactivity processing

The receiver stores messages immediately. A scheduler calls `process_due` after the conversation has been idle for at least 60 seconds, allowing images and text to arrive out of order.

The scheduler is installed as the `admission-intake-process-due` pg_cron job and authenticates with the existing `whatsapp_cron_secret` in Supabase Vault. Collecting-session creation uses a database advisory lock so simultaneous Meta webhook deliveries cannot split one conversation into separate sessions.

Use a non-expiring Meta system-user token with `whatsapp_business_management` and `whatsapp_business_messaging` for `META_WHATSAPP_TOKEN`; temporary developer tokens will eventually stop media downloads and confirmation messages. The protected `meta_token_health` action reports only permission state and sanitized Meta error codes, never the token.

For renewals, the extractor never invents a plan. After a unique player match, deterministic app logic may fill monthly, quarterly, or half-yearly only when the screenshot amount exactly matches the academy price and is consistent with that player's existing standard plan. The confirmation message labels this inference before staff approves it.

Confirmation accepts concise natural phrases such as `confirm`, `confirm renewal`, and `confirm 1 month renewal`. If a stated plan differs from the current draft, the message is treated as a correction instead of approval. Missing fields or unresolved conflicts always block finalization. WhatsApp reviews use short bold sections and blank lines for phone readability.

A repeated confirmation within 30 minutes is attached to the recently confirmed session and returns the same idempotent success result instead of opening a new intake conversation.

## AgentAlpha group protocol

Direct staff chats keep their natural conversational behavior. In a WhatsApp group, intake is deliberately opt-in so unrelated academy conversation never reaches the model:

- Put `AgentAlpha` in the caption of each new admission form or payment screenshot, for example `AgentAlpha renew Rohan`.
- Each new AgentAlpha-tagged group item opens its own isolated session, so simultaneous screenshots cannot merge.
- Reply to that item's AgentAlpha review when correcting or confirming it. Replies are routed by the WhatsApp message ID to the exact session.
- Untagged group messages that are not replies inside an existing AgentAlpha thread are ignored.
- A bare screenshot followed by an unrelated text message is intentionally not paired. Caption the screenshot or resend it with the AgentAlpha trigger.

This routing protects application context only after Meta delivers group webhooks. WhatsApp Business Platform group availability and coexistence eligibility must still be verified with a live group test for the academy number.

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
7. Add only test staff numbers to `ADMISSION_INTAKE_STAFF_PHONES`, then enable shared-number mode.
8. Test duplicate webhooks, two staff senders, out-of-order images, corrections, failed screenshots, and missing required fields.
9. Enable the production intake number only after the above passes.
10. Set `window.GEN_ALPHA_FEATURES.aiIntakeEnabled` to `true` in the web app config and deploy the web repo.
