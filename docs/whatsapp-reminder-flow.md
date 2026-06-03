# WhatsApp Reminder Flow Map

This is the canonical map for Gen Alpha fee reminders. Use this before changing reminder, retry, payment-link, or timeline behavior.

## Sources Of Truth

- `reminder_events`: one current-state row per reminder run for a player.
- `whatsapp_flow_events`: append-only audit stream for reminder, Meta status, parent reply, payment link, proof, and manager alert steps.
- `whatsapp_webhook_events`: raw inbound webhook diagnostics.
- `student_timeline`: player history. WhatsApp rows should come from `whatsapp_flow_events`; legacy direct reminder rows are disabled by `supabase/whatsapp-reminder-flow-cleanup.sql`.

## Flow Tree

```mermaid
flowchart TD
  A["Daily cron: 3:00 PM IST"] --> B["For each active student"]
  B --> C{"Fee state"}
  C -- "Joining fee unpaid" --> J0{"Join date due?"}
  C -- "Renewal" --> R0{"Renewal date state"}

  J0 -- "Due day: rawDaysSince = 0" --> M2["MESSAGE: gen_alpha_fee_reminder<br/>Joining fee due today<br/>Body: Player + Admission + 1st Month"]
  J0 -- "Overdue day 5" --> M3["MESSAGE: gen_alpha_fee_reminder<br/>5-day joining overdue nudge"]
  J0 -- "Overdue day 7+" --> M4["MESSAGE: gen_alpha_fee_reminder<br/>Daily joining overdue nudge"]

  R0 -- "2 days before: rawDaysSince = -2" --> M1["MESSAGE: gen_alpha_fee_heads_up<br/>Soft heads-up before renewal"]
  R0 -- "Renewal day: rawDaysSince = 0" --> M2R["MESSAGE: gen_alpha_fee_reminder<br/>Renewal due today"]
  R0 -- "Overdue day 5" --> M3R["MESSAGE: gen_alpha_fee_reminder<br/>5-day overdue nudge"]
  R0 -- "Overdue day 7+" --> M4R["MESSAGE: gen_alpha_fee_reminder<br/>Daily overdue nudge"]

  M1 --> S0["Insert reminder_events + whatsapp_flow_events: reminder_created"]
  M2 --> S0
  M2R --> S0
  M3 --> S0
  M3R --> S0
  M4 --> S0
  M4R --> S0

  S0 --> S1{"Dry run or WhatsApp disabled?"}
  S1 -- "Yes" --> S2["Log dry_run and stop"]
  S1 -- "No" --> S3{"Parent phone valid?"}
  S3 -- "No" --> F1["Failure: manual follow-up"]
  S3 -- "Yes" --> S4["Send selected template to parent"]
  S4 -- "Meta accepts" --> S5["accepted/sent + message_id"]
  S4 -- "API failure 131049" --> RT1["Retry schedule: 5m, 30m, 60m"]
  S4 -- "Other API failure" --> F1

  S5 --> W0["Meta webhook"]
  W0 -- "delivered" --> TL1["Timeline: Reminder delivered"]
  W0 -- "read" --> TL2["Timeline: Reminder read"]
  W0 -- "failed 131049" --> RT1
  W0 -- "failed 131026/other" --> F1

  RT1 --> RTC["Retry cron: every 5 minutes"]
  RTC -- "due retry_scheduled" --> S4
  RTC -- "same IST-day missed 131049 failure" --> S4
  RTC -- "historical failure" --> STOP["Do not auto-retry"]

  TL2 --> P0["Parent taps quick reply plan"]
  P0 --> P1["Create UPI payment link request"]
  P1 --> M5["MESSAGE: payment link text<br/>Amount + UPI/payment page link"]
  M5 --> P2["Parent submits payment/proof"]
  P2 --> P3["payment_pending_verification"]
  P3 --> M6["MESSAGE: manager_payment_alert<br/>After 5 min delay to 9985822772"]
  P3 --> M7["MESSAGE: manager_payment_alert_with_proof<br/>After 5 min delay + proof media/header when available"]
  M6 --> A1["Manager confirms in app"]
  M7 --> A1
  A1 --> M8["MESSAGE: payment confirmation text to parent<br/>Only after manager confirms payment"]

  ADG{"ENABLE_AUTO_ADMISSION_NUDGES = true?"} --> AD0["Pending admission form<br/>fees_paid = false"]
  ADG -- "No" --> ADS["Do not send admission auto-nudges"]
  AD0 --> AD1{"Days since admission form"}
  AD1 -- "Day 2-4, if not nudged in last 48h" --> AD2["MESSAGE: Registration Reminder text<br/>initial_nudge + payment page link"]
  AD1 -- "Day 5-7, if not nudged in last 48h" --> AD3["MESSAGE: Follow up text<br/>followup_nudge + payment page link"]
  AD1 -- "Day 8+, if not nudged in last 48h" --> AD4["MESSAGE: Final Reminder text<br/>final_nudge + payment page link"]

  classDef message fill:#e8f7ff,stroke:#0877a8,stroke-width:2px,color:#06283d;
  classDef retry fill:#fff4d6,stroke:#b7791f,stroke-width:2px,color:#3d2600;
  classDef fail fill:#fff0f0,stroke:#c53030,stroke-width:2px,color:#4a0505;
  classDef stop fill:#f3f4f6,stroke:#6b7280,color:#111827;
  class M1,M2,M2R,M3,M3R,M4,M4R,M5,M6,M7,M8,AD2,AD3,AD4 message;
  class RT1,RTC retry;
  class F1 fail;
  class STOP stop;
```

## Message Schedule

### Fee Reminders

- `gen_alpha_fee_heads_up`: sent 2 days before a renewal date. Not sent for joining-fee dues.
- `gen_alpha_fee_reminder`: sent on due day for renewal and joining fee.
- `gen_alpha_fee_reminder`: sent again on overdue day 5.
- `gen_alpha_fee_reminder`: sent daily from overdue day 7 onward.
- Same player is skipped if any reminder was already sent today.
- Joining-fee reminders are skipped if the joining payment already has an amount/reference pending verification.

### Payment Flow Messages

- Payment link text: sent after parent selects `1 Month`, `3 Months`, or `6 Months`.
- Help reply text: sent when parent selects `Need Help`.
- Manager payment alert: sent 5 minutes after payment/proof is pending verification.
- Manager payment alert with proof: sent 5 minutes after pending verification when proof media is available.
- Payment confirmation text: sent to parent only after the manager confirms the payment in the app.

### Admission Nudges

- Initial registration reminder text: day 2-4 after admission form submission.
- Follow-up registration text: day 5-7 after admission form submission.
- Final registration reminder text: day 8+ after admission form submission.
- Admission nudges are skipped if another nudge was sent in the last 48 hours.
- Automated admission nudges are disabled unless `ENABLE_AUTO_ADMISSION_NUDGES=true` is set on the Edge Function.

## Retry Rules

- Retry only `131049` / healthy ecosystem errors automatically.
- Do not auto-retry `131026 Message undeliverable`; it needs manual follow-up.
- Scheduled retry rows use `status = retry_scheduled` and `next_retry_at`.
- Fallback recovery can pick up unscheduled `131049` failed rows only from the current IST day.
- Historical failures must stay historical and should not be revived by the retry worker.
- Worker batch size is capped at 20 with a small delay between sends.

## Timeline Rules

- Main user-visible WhatsApp events:
  - `WhatsApp reminder prepared`
  - `Reminder failed`
  - `Reminder delivered`
  - `Reminder read`
  - `Parent selected renewal plan`
  - `Payment link sent`
  - `Parent payment proof received`
  - `Payment confirmed by academy`
- Operational noise to suppress in UI:
  - `accepted`
  - `sent`
  - retry scheduled rows
  - legacy `Renewal reminder prepared` / `Joining fee reminder prepared`
- Show time as IST `HH:mm:ss` wherever player timeline timestamps are displayed.

## June 2, 2026 Audit Result

Five reminder records were created in the 15:00 IST run:

- Lithvik tiru lohan G: delivered/read.
- Syed Musbah: accepted/sent only, no failed status in the tracked data.
- Gowshik Ch: delivered/read.
- Shaswat p: failed once with `131049`, retried, delivered/read, then parent selected a plan.
- Leela Krishna C: failed with `131026 Message undeliverable`; no auto-retry should run.

So the correct interpretations are:

- `5` reminder records in the June 2 run.
- `2` failed delivery attempts.
- `1` retry-eligible failed attempt.
- `1` currently failed reminder row.
