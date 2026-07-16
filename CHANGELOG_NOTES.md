# Gen Alpha Manager Changelog Notes

Last updated: 2026-07-16

This file records meaningful project changes and decisions so future Codex sessions can understand recent work without rereading the full chat. It is not a release changelog for users; it is a developer/manager memory log.

Use this file when:

- The user asks what changed recently.
- A new bug appears and we need to compare with recent edits.
- The user asks to revert a recent UI/logic change.
- A future agent needs to understand why a design or business rule exists.

For current source-of-truth rules, read `PROJECT_CONTEXT.md` first.

## 2026-07-16

### Shared WhatsApp Admission Intake Safety

- Added a mandatory normalized `ADMISSION_INTAKE_STAFF_PHONES` allowlist before inbound WhatsApp messages can reach the admission model.
- Shared-number intake now requires an exact Meta phone-number asset match and explicit `ADMISSION_INTAKE_SHARED_NUMBER=true` opt-in.
- Non-staff messages on the reminder number continue through the existing renewal and payment reply flow.
- Shared-number intake now inherits the existing reminder phone-number asset ID, avoiding a duplicate plaintext secret during activation.

### Conversational Renewal Intake

- Added admission-versus-renewal classification for messy staff conversations and payment screenshots.
- Added deterministic player matching, authoritative paid-through calculation, staff confirmation, and atomic renewal/payment recording.
- Added duplicate UTR/reference, intake-session, and renewal-cycle protection; failed or pending screenshots cannot be confirmed.
- Combined official Groups API messages by group ID while preserving the 1:1 forwarding and manager web fallbacks.
- Serialized live intake session creation so a text and screenshot delivered in parallel remain in one conversation.
- Added the missing one-minute idle-session processor and conservative near-name matching for unique player spellings such as Adil/Aadil.
- Added protected Meta token health diagnostics, detailed media-auth errors, and service-JWT compatibility for rotated Supabase project keys.
- Added deterministic renewal-plan inference when an exact academy price matches the uniquely matched player's existing standard plan.
- Accepted natural confirmation phrases such as `confirm 1 month renewal` when they agree with the reviewed draft, blocked confirmation while conflicts remain, and replaced dense intake summaries with compact WhatsApp-formatted review cards.
- Reused recently confirmed sessions for repeated confirmations so delayed or duplicate staff replies cannot open a new intake conversation.

### Conversational AI Admission Intake Foundation

- Added a provider-neutral admission intake state machine for informal WhatsApp conversations and manager web uploads.
- Added immutable message, extraction, correction, and payment-claim audit tables with private media storage.
- Added human-confirmed admission finalization mapped to the existing admissions review queue and fee rules.
- Added manager-only payment-claim verification that creates a joining payment ledger entry and updates roster/finance state atomically.
- Added an authenticated `AI Intake` web fallback for pasted conversations, admission forms, PDFs, and payment screenshots.
- Routed a configurable dedicated Meta Cloud API number into the new intake function without interfering with the current renewal reminder number.

## 2026-07-14

### Sample Reminder Flow Selector

- Updated `send_sample_reminder` so sample WhatsApp sends can use either Direct Pay V2 or the legacy plan-button flow.
- Added explicit sample actions: `send_sample_direct_pay_reminder` / `send_sample_v2_reminder` for the new Pay Now template, and `send_sample_legacy_reminder` for the old fallback template.
- `send_sample_reminder` now defaults to Direct Pay V2, but callers can pass `flow: "legacy"`/`version: "old"` to test the old path.

## 2026-07-11

### Direct Pay Reminder Flow

- Added Direct Pay reminder support in the `whatsapp-reminder` Edge Function: reminders can send a Pay Now URL button first, create an awaiting parent-choice UPI request, and fall back to the existing WhatsApp plan-button template while the new Meta template is unavailable or pending approval.
- Added public `payment_options` support for `pay.html?e=<reminder_event_id>` so the payment page loads the exact player, due type, and 1/3/6-month amounts before UPI handoff.
- Updated `payment_attempted` handling so the selected plan from the payment page is saved to `reminder_events`, `payment_link_requests`, and the WhatsApp audit stream before the follow-up message is sent.
- Added a protected `setup_direct_payment_template` action to submit the Direct Pay utility template to Meta from the deployed function.

### WhatsApp Payment Attempt Compatibility

- Updated the `whatsapp-reminder` Edge Function so `payment_attempted` accepts both `eventId` and legacy `event_id` payloads.
- This protects payment-attempt tracking when older cached payment pages or alternate browser copies call the function with snake-case event ids.

## 2026-07-04

### Special Training Multi-Month Payments

- Browser and Android admission forms now show `Special training months` only when the special plan is selected; default remains 1 month.
- Special training totals calculate as Rs 10,000 per month, auto-applying 5% off for 3-5 months and 10% off for 6+ months, with no admission fee, plus any extra jersey amount.
- Browser and Android renewal/joining-fee dialogs now support multi-month special training and save `months_covered` accordingly.
- Paid-through calculations in browser, Android, and the WhatsApp reminder function now treat `fee_plan`/`plan_type = special` with the special-training discount table before applying regular monthly/quarterly/half-yearly inference.
- Live correction applied for Aarav C: existing Rs 29,000 joining payment was kept, marked as special training for 3 months from 2026-07-01, and a data-correction timeline row was added.

## 2026-06-15

### Contextual Wrong-Number Management

- Removed all wrong-number controls from player action menus and normal Edit Player forms.
- Only flagged players see a compact correction message beside the phone field.
- Saving a different valid 10-digit number automatically reactivates future reminders and confirms that state to staff without retrying historical failures.

## 2026-06-14

### Separate Fee Follow-Up And Due Labels

- Android keeps `Manual follow-up` in the fee-status field for players whose automatic reminders have stopped.
- The next-fee-due field now continues to show the calendar status (`15 days overdue`, `Due today`, or days left) for both joining and renewal fees.
- This is presentation-only: the 15-day reminder cutoff, manual-follow-up flag, retry behavior, payment state, and due-date calculations are unchanged.

### Wrong WhatsApp Number Follow-Up

- Added player-level `whatsapp_contact_status` and reminder-level `manual_followup_reason` fields.
- Marking a player as `wrong_number` immediately stops automatic reminders, manual reminder sends, queued retries, and delayed retry callbacks until staff reactivate the contact.
- Web and Android keep the `Manual follow-up` fee status and show a visible reason such as `Wrong phone number`, `15+ days overdue`, `Retry limit reached`, or `WhatsApp delivery failed`.
- Added a manager edit control in both apps to mark or clear a wrong number; clearing it allows future reminders to resume under the normal schedule without retrying historical rows.
- Marked Leela Krishna C and Jeevan Reddy C as wrong-number contacts in the live database and confirmed both latest reminder rows have no next retry scheduled.
- Deployed the updated `whatsapp-reminder` Edge Function and applied `supabase/add-whatsapp-contact-status.sql`.

## 2026-06-04

### WhatsApp Utility Template Mapping

- Replaced fee heads-up template fallback with `utility_fee_headsup`.
- Replaced generic fee reminder template fallback with `utility_for_fee_reminder`.
- Added distinct renewal-day template fallback `utility_renewal_day` for `reminder_type = renewal_day`.
- Kept `manager_payment_alert` and `manager_payment_alert_with_proof` unchanged because those Meta templates are already utility.
- Removed discount wording from internal WhatsApp plan labels so reminders/payment notes use `1 Month`, `3 Months`, `6 Months`, and `Need Help`.

### Manual Follow-Up Cutoff

- Automatic joining-fee and renewal reminders now stop once a player reaches 15+ overdue days.
- The existing reminder row is marked `manual_followup_required` when available, and web/Android fee chips show `Manual follow-up`.
- Day 7 through day 14 still use the existing daily reminder branch.

### Android Dashboard Alerts

- Removed the regular blue alert card from the Android manager dashboard.
- Kept only the immediate follow-up card for players over the critical follow-up threshold.

### WhatsApp Timeline Message Bodies

- Template reminders now store the rendered parent-facing message body in `whatsapp_flow_events.message_body` instead of only storing the old preview text.
- Web and Android timelines now show the stored reminder body for `Fee reminder prepared` rows.
- Added a focused backfill for utility-template reminder rows from June 4, 2026 onward.

### Early Renewal Payments

- Android renewal payment action is now available for any active player whose joining fee is already paid, even before the due date.
- Renewal payments still use the next cycle date, so early collection extends from the current paid-through date instead of today.

### Reminder Pause Control

- Live Supabase reminders were paused by setting `system_settings.whatsapp_reminders_enabled = false` and unscheduling the `daily-whatsapp-reminder` and `whatsapp-retry-reminders` cron jobs.
- Updated the WhatsApp reminder Edge Function so the `retry_due_reminders` action also respects the global pause flag before sending retry templates.
- Added `supabase/resume-whatsapp-reminders.sql` to restore the global flag and both schedules using a Vault-backed cron secret instead of embedding credentials in source or cron definitions.

### Joining Fee Due-Day Reminder

- Fixed the automatic due-day branch so unpaid joining fees use `reminder_type = joining_fee` and the joining-fee template instead of being incorrectly classified as `renewal_day`.
- Joining-fee reminders remain scheduled on the join date, overdue day 5, and daily from overdue day 7 through day 14; day 15+ remains manual follow-up.
- Non-retryable Meta delivery failures such as `131026 Message undeliverable` now clear retry scheduling and explicitly set `manual_followup_required = true`; only temporary `131049` failures remain eligible for automatic retry.
- Corrected the live heads-up template mapping to `utlity_fee_headsup`, matching the exact Meta template name; the previous `utility_fee_headsup` mapping caused Meta error `132001`.
- Successful reminder retries now clear stale `meta_error` and `failed_at` values from the current reminder row so web and Android do not continue showing a recovered reminder as failed.

### Pending Admission Roster Isolation

- Added `supabase/fix-pending-admission-roster-leak.sql` to remove the legacy `admissions_create_student` insert trigger so pending admissions stay out of the roster until `approve_admission()` is used.
- The migration removes only dependency-free student rows linked to still-pending admissions while preserving the admission record for manager review or rejection.

## 2026-05-25

### Student Life Timeline and Attendance Calendar

- Added `supabase/student-life-timeline-audit.sql` to log meaningful student life events into `student_timeline`: created, discontinued/rejoined, renewal array changes, fee status changes, jersey changes, profile/contact/fee split changes, and payment insert/update/delete.
- Attendance is intentionally excluded from timeline and remains in the `attendance` table.
- Updated browser Player Profile V2 to show filtered history chips and a six-month attendance calendar similar to the Android attendance history dialog.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `node --check web-app-repo/player-profile-v2/player-profile-v2.js`
  - `git diff --check`

### Reminder Status and Joining Fee Simplification

- Updated web and Android fee-status labels so failed WhatsApp/Meta reminders show `Reminder failed` instead of being flattened into `Reminder sent`.
- Added failed reminder reason handling from `reminder_events.meta_error`/`failed_at` and profile timeline fallback entries, plus `supabase/reminder-status-timeline-failures.sql` to log provider failure reasons into `student_timeline`.
- Compacted player timelines so a reminder attempt shows one `WhatsApp reminder prepared` item plus one final `Reminder failed`/`Reminder delivered` item, hiding accepted/legacy duplicate rows and long phone/template message bodies.
- Simplified web and Android `Record joining fee`: removed the joining-mode `Amount paid` field, added jersey size/pairs, calculated jersey amount from pair count at Rs 750 each, and saved jersey size/pairs to joining payment rows.
- Fixed the web admission banner spacing so it no longer overlaps the admission heading.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `git diff --check` in both repos.
  - `./gradlew assembleDebug`
  - Browser DOM check on a fresh local static server confirmed the edited joining-fee fields, readonly jersey amount, and positive admission heading/banner gap.

### Joining Fee Split Payment Wiring

- Added `supabase/add-payment-fee-breakdown-fields.sql` to store coaching/admission/jersey/total split fields on `student_payments`.
- Updated web and Android `Record joining fee` flows to show and save the fee split, update the player fee split fields, and keep `amount_paid` as the actual received amount.
- Added schema-cache fallbacks so joining payments can still be saved if the live DB has not yet received the new payment split columns.
- Supabase CLI local status could not inspect the project because Docker is not running; the migration file is saved for application to the linked DB.

### Simplified Admission Amount Paid Wiring

- Removed the visible manual "Amount paid" override from admission and player create/edit forms in both web and Android.
- Kept the database/RPC `amount_paid` wiring stable: paid/pending admissions now submit the calculated total, unpaid submissions submit Rs 0, and existing paid player edits preserve the stored paid amount.
- Custom or partial collections should use the manager joining-fee or renewal payment action, where the payment amount remains editable.
- No destructive DB migration is required because existing columns and RPC parameters are still populated.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `git diff --check` in both repos.
  - `./gradlew assembleDebug`
  - Small-screen browser check at 390 px confirmed admission has no `Amount paid now`, manager edit form has no manual amount field, and edit-card player names are visible without overlapping status.

## 2026-05-24

### Cross-App Implementation Rule

- Strengthened the project instruction that this workspace has separate Android and web app projects.
- Future shared logic, business-rule, validation, payment/reminder, schema/RPC, and bug-fix work must be checked and implemented in both apps unless the user says one app only.
- Added explicit guidance to fit changes into each app's own architecture, design, navigation, and flow instead of copying UI/code blindly.

### Project Verification and Push Rule

- Added a standing project instruction to audit code/UI changes before finishing, including edge cases and rough rendered UI states.
- Added a standing project instruction to commit and push completed code changes to GitHub after verification unless the user says to keep them local or pause before publishing.

### Web Mobile Edit Card Player Names

- Fixed phone-sized browser roster edit cards so the player name spans the mobile card front instead of being squeezed beside status.
- Added the player name to the flipped mobile action face so staff can still see which player they are editing/reminding/deleting.
- Bumped web cache assets to `v59`.

### Web Fee Split and Action Menu Audit

- Converted web admission and manager player-entry fee breakdown tiles into editable amount fields for coaching fee, admission fee, jersey amount, and total.
- Added safe web payload mapping for fee split fields and a Supabase migration file `supabase/add-fee-breakdown-fields.sql`.
- Cleaned roster action-menu logic so dropdowns close when scrolling, only one action menu stays open, and stale menus do not stick to the page.
- Wired Android admission/player edit models and Supabase payloads to carry the same fee split fields, with schema-cache fallbacks for older DB states.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`

### Admission Jersey Payment Layout and Lithvik Correction

- Moved the web admission `Pay now` button below jersey size and jersey pairs.
- Removed the optional label from admission and manager jersey pairs and made pair count active only when a jersey size is selected.
- Corrected Lithvik tiru lohan G in Supabase: joining payment Rs 3,500, paid on 2026-05-22, cycle start kept at join date 2026-04-25, next fee due 2026-05-25.

### WhatsApp Heads-Up Template

- Changed automated `heads_up` WhatsApp reminders from free-text messages to the Meta template `gen_alpha_fee_heads_up`.
- Reason: Meta error `131047` blocks free-text re-engagement messages outside the 24-hour parent reply window.
- Heads-up reminders now use template quick replies for 1 month, 3 months, 6 months, and Need Help, matching the renewal flow.

## 2026-05-23

### Player Edit Fee Split and Blank Values

- Wired the manager/player edit flows to show the same coaching fee, admission fee, jersey amount, and total guidance used by admissions.
- Blank amount fields are now accepted and saved as Rs 0 in both web and Android player edit flows.
- Kept renewal pricing unchanged: monthly Rs 3,500, 3 months Rs 9,975, 6 months Rs 18,900.
- Strengthened jersey pair parsing so blank or invalid pair counts safely become 0, while every selected pair still adds Rs 750.
- Fixed the small-screen web edit card name rendering so the player name stays visible in edit mode.
- Verification done:
  - `node --check web-app-repo/script.js`
  - `./gradlew assembleDebug`

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
- AI/OCR intake is being reintroduced behind human confirmation, using the existing admission review and payment-verification lifecycle.
- Current UPI ID is `9059962499@ybl`.
- Current WhatsApp reminder template uses two variables and language `en`.

## How To Add Future Entries

Add the newest entry at the top under a date heading. Keep each entry short but include:

- What changed.
- Why it changed.
- Files or features affected.
- Verification done.
- Commit hash if pushed.
