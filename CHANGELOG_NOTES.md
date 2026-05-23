# Gen Alpha Manager Changelog Notes

Last updated: 2026-05-23

This file records meaningful project changes and decisions so future Codex sessions can understand recent work without rereading the full chat. It is not a release changelog for users; it is a developer/manager memory log.

Use this file when:

- The user asks what changed recently.
- A new bug appears and we need to compare with recent edits.
- The user asks to revert a recent UI/logic change.
- A future agent needs to understand why a design or business rule exists.

For current source-of-truth rules, read `PROJECT_CONTEXT.md` first.

## 2026-05-23

### Joining Fee Payment Action

- Added a manager action for joining-fee-pending players to record the first payment separately from renewals.
- The payment popup/dialog now supports plan, amount, comment, and payment date; payment date is used for finance reporting while the fee cycle stays anchored to the player join date.
- In joining-fee mode, the suggested amount includes selected plan amount, admission fee, and jersey amount, but remains editable.
- Saving a joining payment inserts a `student_payments` row with `payment_type = joining`, marks the student paid, and updates due-date logic via the selected plan/months.
- Applied in both web and Android.

### Admission Fee Breakdown and Jersey Charging

- Updated web and Android admission forms to show separate coaching fee, admission fee, jersey amount, and total boxes.
- Removed the first-pair-free jersey rule; every selected jersey pair now adds Rs 750.
- Added/kept optional amount-paid override so admissions can still be submitted when parents pay a custom or partial amount.
- Updated jersey pair edit revenue logic so moving from 0 to 1 pair records Rs 750 revenue.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`
  - Local web preview confirmed monthly + one jersey pair shows Rs 4,750 total.

### Special Training Admission Logic

- Fixed special-training admission logic in web and Android so Rs 10,000 is treated as 1 month, not inferred as a 3-month payment.
- Special training no longer adds the one-time admission fee in the admission amount summary.
- Added special-training roster detection for explicit `plan_type = special` payment rows and legacy Rs 10,000 first payments.
- Corrected Parvez Ali in Supabase by adding a `student_payments` joining row with `plan_type = special`, `months_covered = 1`, and amount Rs 10,000.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`

## 2026-05-22

### Jersey First Pair Included Rule

- Made jersey size and jersey pair count optional in both web and Android admission/player-edit flows.
- Changed jersey revenue logic so joining includes the first pair; only pair 2+ records Rs 750 per extra pair.
- Superseded on 2026-05-23: every selected jersey pair is now chargeable at Rs 750.
- Removing pairs now only records a refund row when an already charged extra pair is removed.
- Adjusted first-payment duration inference so extra jersey charges do not break 3-month or 6-month paid-through calculations.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`

## 2026-05-21

### Blank Jersey Size Save Fix

- Fixed web and Android save payloads so blank jersey size is stored as an empty string instead of `NULL`.
- Reason: `students.jersey_size` is `NOT NULL`, and editing a player without a jersey size was failing with a constraint error.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`

### Jersey Pair Counter and Revenue Ledger

- Added a lightweight jersey-pair counter for staff in Android roster cards.
- Updating the count immediately saves `students.jersey_pairs` and records a `student_payments` ledger row.
- Added `jersey` revenue rows for added pairs and `jersey_refund` rows for removed pairs so finance totals can add/subtract Rs 750 without violating the DB non-negative payment amount check.
- Renewal due-date logic now ignores jersey ledger rows so jersey edits do not extend player subscription periods.
- Existing full player edit saves also record jersey count revenue adjustments when the pair count changes.
- Verification done:
  - `./gradlew assembleDebug`

## 2026-05-15

### WhatsApp Renewal Flow Audit Trail

- Added `supabase/whatsapp-flow-audit.sql` to create `whatsapp_flow_events`, an append-only ledger for reminder/payment events with explicit SQL columns for message id, direction, status, sent/delivered/read/failed times, payment plan/amount/months/date range, proof storage path, and provider payload.
- Extended `reminder_events` and `payment_link_requests` with timestamps for payment link sent, Pay Now clicked, pending verification, payment confirmed, and confirmation message delivery/read tracking.
- Updated the `whatsapp-reminder` Edge Function to log manual reminders, auto reminders, parent plan button taps, payment link sends, Pay Now clicks, parent “Paid”/screenshot replies, verification replies, help replies, manager payment confirmations, Meta delivery/read/failure callbacks, and admission payment reminders.
- Player timelines now receive mirrored WhatsApp flow events via DB trigger, and web/native timeline coloring treats failed/error events distinctly from payment/reminder/admission events.
- Verification done:
  - `deno check supabase/functions/whatsapp-reminder/index.ts`
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`

### Android Native Roster UI Upgrade

- Reworked Android roster cards to feel more native and compact than the mobile browser version.
- Player cards now show a clean front summary with status/fee chips, direct Renew Payment CTA when renewal is due, and a Manage chip for staff.
- Staff Manage opens a Compose-native quick action face with Profile, Edit details, Renew payment, Send reminder, and Discontinue/Mark active actions; Delete remains inside the player profile to avoid accidental destructive taps.
- Student movement cards now use the same color meaning as the browser chart: Joined blue, Continuing green, Left red, with improved active count and bar styling.
- Player profile timeline was redesigned from a plain log list into colored event cards with a vertical rail, metadata chips, details text, and payment proof thumbnail support.
- Android finance expense rows were compacted into modern finance cards with clearer type/comment/date/paid-by/amount/delete layout.
- Verification done:
  - `./gradlew assembleDebug`
  - Installed the debug APK on `emulator-5554` and exercised Admission, Attendance, Staff login, Roster, Player Profile, and Finance in large-font mode.
  - Confirmed relaunch returns to the public Admission view instead of silently keeping manager access unlocked.

## 2026-05-12

### Automated Reminders Robustness & Joining Fees
- Fixed aggressive reminder logic in `whatsapp-reminder` Edge Function.
- Strict schedule enforced: Day -2 (Heads-up), Day 0 (Renewal), Day 5 (Follow-up), Day 7 (Follow-up), and every day after Day 7.
- **Improved Admission Nudges**: Moved from brittle "Day 4 only" logic to milestone windows (Day 2-5, 5-8, 8+) with a 48-hour deduplication check.
- **Joining Fee Alignment**: Joining Fee reminders (for unpaid roster students) now follow the same Day 0, 5, 7+ cadence as renewals.
- **Visibility**: Automated admission nudges are now logged in the admission's **Comments** field for better manager visibility.
- Cleared backlog of stale `queued` reminder events and cancelled old `awaiting_parent_choice` payment link requests.
- Deployed updated function to production.

### Admission Form Grade Schema Fix
- Added missing `grade` column to `public.admissions` table.
- Updated `submit_admission_form` RPC to accept `p_grade` parameter.
- Updated `sync_student_from_admission` trigger to correctly sync the grade field to the roster on approval.
- Updated Android app (`SupabaseRepository`, `Models`, `AdmissionDraft`) to support the new field.

### Roster UI Cleanup
- Removed "Fees Paid" metric tile from both Android and Web roster dashboards.
- Layout in Android `StatsSection` adjusted to a clean 3-card row (Joined, Active, Returning).
- Web `script.js` updated to remove unused `paidCount` DOM update logic.

## 2026-05-09

### Payment Verification UI

- Roster/profile fee status now uses latest WhatsApp reminder/payment-link state to show `Reminder sent`, `Pending verification`, or `Paid`.
- Player profile now shows `Confirm payment received` when a parent has replied `Paid` or sent a screenshot and the payment is awaiting manager verification.
- Confirming payment records the renewal payment with the selected WhatsApp plan amount/months, updates renewal dates, and triggers the renewal confirmation WhatsApp.
- Timeline payment-proof entries can show stored screenshot thumbnails and open them in a viewer when the proof exists in private Supabase Storage.
- Android listens for reminder/payment-link realtime changes so the status can update without manual refresh.

### Renewal Verification WhatsApp and Proof Logging

- Changed parent payment-proof reply to: `Once the academy confirms the payment, we’ll update your renewal. Thank You!`
- When staff records a renewal payment, the WhatsApp function now sends a renewal confirmation with player name, selected plan, and renewal date range.
- Parent `Paid` text or screenshot/document replies are stored in reminder debug metadata; raw webhook payloads remain in `whatsapp_webhook_events`.
- Screenshot/document payment proof media is downloaded from Meta and stored in private Supabase Storage bucket `payment-proofs` when possible.
- Added explicit timeline/debug events for parent payment proof received and renewal confirmation sent.

### Renewal Payment WhatsApp Follow-Up

- Added renewal Pay Now follow-up flow.
- Payment page links now include the reminder event id generated by the WhatsApp reminder function.
- When parent taps `Pay Now` on `pay.html`, the page calls the Supabase WhatsApp function with `payment_attempted`.
- The function updates reminder/payment-link status and sends: `After payment, just reply here with "Paid" or send the payment screenshot.`
- If parent replies `Paid` or sends an image/document screenshot on WhatsApp, the function marks the payment flow as `payment_pending_verification` and replies: `Once the academy confirms the payment, we’ll update your renewal. Thank You!`
- This does not auto-renew, count revenue, or send receipt. Manager verification is still required.
- Supabase function deployed: `whatsapp-reminder`.
- Verification done:
  - `deno check supabase/functions/whatsapp-reminder/index.ts`
  - `node --check web-app-repo/script.js`
  - safe deployed endpoint smoke test returned `eventId is required`.

### Payment Pending Verification Status

- Added `Payment pending verification` behavior for parent UPI payments in both web and Android.
- Parent/UPI payment claims no longer auto-mark `fees_paid=true`.
- Submitted UPI amount/reference is kept for manager review, while finance/paid stats/receipts stay unconfirmed until manager verifies payment.
- Web admission copy now says payment is submitted for academy verification.
- Android admission copy now says UPI stays pending until manager verifies.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`

### Project Context and Changelog Files

- Added and prepared `PROJECT_CONTEXT.md` and `CHANGELOG_NOTES.md` for both Android/root and web repos.
- Decision: keep the Android/root context as the fuller cross-app source of truth; keep the web repo context web-focused but still include shared business rules.
- Sanitized the staff PIN wording in context docs so future agents verify the current value from code/config instead of exposing it directly in documentation.
- Reason: future Codex sessions need reliable context, but GitHub docs should avoid unnecessary operational secrets.

### Android: Staff Login Dialog Visibility

- Changed the staff email/password login from a bottom-aligned sheet to a centered compact dialog.
- Reason: on emulator/large-font devices, the login button was pushed to the bottom gesture/navigation area and became partly invisible.
- Verified with emulator screenshot and UI tree:
  - Before: login button bounds touched bottom of app content around y=2400.
  - After: dialog surface centered; login button bounds ended around y=1863.
- Build passed with `./gradlew assembleDebug`.
- Installed debug APK into emulator and visually confirmed.
- Commit pushed to Android repo:
  - `6a3742b Center staff login dialog`
- Main file changed:
  - `/Users/jiths/Documents/New project/android-app/app/src/main/java/com/genalpha/cricketacademy/ui/AcademyApp.kt`

### Project Memory Files

- Added `PROJECT_CONTEXT.md` as the stable source-of-truth briefing for future Codex sessions.
- Added this `CHANGELOG_NOTES.md` to track important changes and decisions over time.
- These files are local unless intentionally committed/pushed.

## Recent Stable Decisions To Preserve

- Web public landing page should show only the admission form before manager login.
- Android and web should both be updated for shared product features unless user says Android-only or web-only.
- Finance appears only after staff login.
- WhatsApp reminders are manual only for now, not automated.
- Razorpay is paused; UPI payment link/page is used.
- AI/OCR upload/import for handwritten forms is removed/paused.
- Current UPI ID is `9059962499@ybl`.
- Current WhatsApp reminder template uses two variables and language `en`.

## How To Add Future Entries

Add the newest entry at the top under a date heading. Keep each entry short but include:

- What changed.
- Why it changed.
- Files or features affected.
- Verification done.
- Commit hash if pushed.
