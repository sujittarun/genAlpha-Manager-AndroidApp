# Gen Alpha Manager Changelog Notes

Last updated: 2026-05-25

This file records meaningful project changes and decisions so future Codex sessions can understand recent work without rereading the full chat. It is not a release changelog for users; it is a developer/manager memory log.

Use this file when:

- The user asks what changed recently.
- A new bug appears and we need to compare with recent edits.
- The user asks to revert a recent UI/logic change.
- A future agent needs to understand why a design or business rule exists.

For current source-of-truth rules, read `PROJECT_CONTEXT.md` first.

## 2026-05-25

### Reminder Status and Joining Fee Simplification

- Updated web and Android fee-status labels so failed WhatsApp/Meta reminders show `Reminder failed` instead of being flattened into `Reminder sent`.
- Added failed reminder reason handling from `reminder_events.meta_error`/`failed_at` and profile timeline fallback entries, plus `supabase/reminder-status-timeline-failures.sql` to log provider failure reasons into `student_timeline`.
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
