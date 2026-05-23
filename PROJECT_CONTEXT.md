# Gen Alpha Manager Project Context

Last updated: 2026-05-09

This file is the source-of-truth briefing for Codex/future agents. Read this first before changing code, then verify the relevant implementation files. Do not rely only on old chat history because this project has many superseded decisions.

## Project Identity

- Academy name: Gen Alpha Cricket Academy.
- Location/theme text: Manikonda appears in logo artwork, but do not add unnecessary "Manikonda" text in UI unless the user asks.
- Primary public domain: `https://genalphaacademy.in`.
- Main product: academy manager system with public admission form, attendance, staff roster, finance, WhatsApp reminders, UPI payments, and Android native app.
- Tone/design goal: clean, modern, compact, parent-friendly on admission surfaces, manager-efficient on staff/finance surfaces.

## Repository Map

- Android/native app repo root: `/Users/jiths/Documents/New project`
- Android remote: `git@github.com:sujittarun/genAlpha-Manager-AndroidApp.git`
- Android branch: `main`
- Android project folder: `/Users/jiths/Documents/New project/android-app`
- Web repo root: `/Users/jiths/Documents/New project/web-app-repo`
- Web remote: `git@github.com:sujittarun/cricket-academy-manager.git`
- Web branch: `main`
- Supabase SQL/function files live under Android repo root: `/Users/jiths/Documents/New project/supabase`

Important: keep Android-only code in the Android repo and web-only code in `web-app-repo`. Do not accidentally push web changes to the Android repo or Android changes to the web repo.

## Expected Working Rule

- Unless the user explicitly says "Android only" or "web only", changes that apply to product behavior should be implemented in both Android and browser apps.
- If a new field is added, check all layers: Supabase schema/RPC, web form/render/edit flows, Android models/repository/UI, receipts/finance/timeline if relevant.
- Do not make big design changes silently. For risky layout choices, give a concise option/recommendation first or keep existing design language.
- The user expects thorough verification before saying "fixed", especially for Android emulator/UI bugs and Supabase/RPC issues.
- Keep `CHANGELOG_NOTES.md` updated automatically for meaningful feature work, bug fixes, database changes, UI redesigns, payment/reminder changes, and anything pushed to GitHub. Do not log tiny experiments, simple explanations, failed attempts, or temporary checks unless the user asks.
- Update this `PROJECT_CONTEXT.md` only when the stable source-of-truth changes, such as new business rules, repo/deployment changes, major feature decisions, credentials/config locations, or important "do not do this again" lessons.

## Main Product Views

### Public/Parent Admission

- Landing page for web must be admission form only. Parents should not see roster, dashboard stats, finance, or manager details before login.
- Android bottom navigation has parent admission, attendance, and staff. Finance only appears after staff login.
- Admission form is public. It creates an admission record and, after review approval, creates/updates player record.
- Admission form fields include:
  - Applicant name and initial for jersey naming.
  - Filled by: `Parent / Guardian`, `Coach`, `Manager`.
  - DOB dropdowns, age, gender, nationality.
  - Father/guardian, parent contact, alternate contact.
  - City, address, school/college, grade where supported.
  - Parent Aadhaar is optional. Do not use "NIDA" wording.
  - Time slot.
  - Join date defaults to today but can be picked.
  - Fee paid yes/no, payment plan, amount/custom amount.
  - Jersey size and jersey pairs.
  - Comments/special requests, optional.
  - Skills and playing style. "Kick start the journey now" disables detailed style choices.
  - Consent and terms are mandatory.
- Mobile number validation: parent and alternate contact should be 10 digits when provided/required.
- Payment is not mandatory. If fees paid is "No", no receipt should be generated at admission submit time.
- Parent UPI payments are not automatically verified. If a parent marks payment made or enters UTR/reference, store it as `Payment pending verification`: keep `fees_paid=false`, keep the submitted amount/reference for manager review, do not count it in finance, and do not generate receipt until manager verifies/marks paid.
- Roster/profile fee state should distinguish `Reminder sent`, `Pending verification`, and `Paid` when WhatsApp reminder/payment-link data exists.

### Admission Review Queue

- Parent submissions should wait for manager review.
- Manager can approve admission to roster or reject it.
- Approval should create the student/player record.
- If approval fails with missing columns like `filled_by`, add incremental migration, do not rerun destructive schema.

### Staff/Manager View

- Staff entry has a 6-digit PIN gate. Verify the current value from Android code/config instead of repeating it in public docs.
- PIN should be asked every time the user goes into Staff from other tabs.
- After PIN, email/password manager login unlocks editing.
- Android should not persist the logged-in manager session across app relaunch. If app is removed from background/launched again, it should not silently stay logged in.
- Once logged in, staff can still navigate Admission, Attendance, Staff, and Finance. Do not trap staff in Finance only.
- After logout, web should return to public admission landing only.

### Roster/Player Management

- Roster should show registered players clearly and compactly.
- Web roster should remain a table on desktop, centered within the page; avoid horizontal scroll if possible but do not collapse desktop table into a mobile-card design unless requested.
- Mobile can use cards/list design.
- Player names in alerts should be clickable and navigate/open player details.
- Clicking alert player in web should scroll down to player list/details.
- Edit player should include:
  - name, age, slot, join date, fees paid/amount where appropriate.
  - jersey size and jersey pair count.
  - father name, mobile number, alternate number, school, grade, address.
  - status active/discontinued and discontinued date.
- Payment method, UTR/reference, and comments from admission/payment should generally not be editable in the basic edit player panel unless explicitly requested.
- "Last Updated" column should show just username/value, not repeated label text.
- Action column in view-only mode should not show useless "Login to edit" text.
- Discontinued players should not count as active or paid for active-player fee stats.
- Returning students calculations should use active students only.

### Player Profile

- Player profile should show:
  - Parent/father name and phone number. Android phone number should be directly callable.
  - Join date, renewals, discontinued date if applicable.
  - Number of training days present.
  - Attendance calendar popup when days present is clicked.
  - Number of months/training duration in academy. Do not count discontinued gaps as active training time.
  - Total amount paid and detail of months/plans paid.
  - Timeline/history in small, informational style; do not make timeline visually heavy.
  - If a parent uploads a payment screenshot/image through WhatsApp and it is stored in `payment-proofs`, timeline should show a small thumbnail that can open in a viewer.
  - If latest WhatsApp payment status is pending verification, manager profile should show `Confirm payment received`; confirming records renewal payment, updates due dates, and sends the renewal confirmation WhatsApp.

### Attendance

- Attendance can be marked by young players, so the UX must be simple and forgiving.
- Give option to undo/revert present because kids may tap wrong player.
- Attendance page should have search and batch/time-slot filters.
- Attendance must sync quickly between Android and web without requiring manual refresh.
- Supabase realtime for `attendance` should be enabled.
- Android attendance updates previously needed a similar live-sync fix as player edits. Do not regress this.

### Finance

- Finance appears only after staff login.
- Finance tracks revenue via `student_payments` and expenses via `academy_expenses`.
- Finance has:
  - range filters: current code has no "1 week"; "Last month" means previous calendar month. Also supports date range fields.
  - metrics for selected range, not only current month.
  - revenue vs expense/monthly net visualization.
  - month detail popup/list with revenue and expenses side by side.
  - revenue detail should include joining and renewal totals as submetrics, not as large independent pills.
  - expenses table/list with filter/sort and delete option.
  - add expense popup/dialog, not a large inline block.
- Expense fields:
  - Type dropdown: Coach Fees, Purchased accessories, Transport, Maid expense, etc.
  - Amount.
  - Comment.
  - Paid by dropdown: Sandeep, Srinivas, Sujit.
- Android finance page should feel like a professional compact finance app, not bulky tiles.
- Android finance uses pull-to-refresh carefully; avoid refresh triggering during normal scroll. Prefer a deliberate/deep pull if implemented.
- Expense data should sync/reload quickly between web and Android.

## Fees, Renewals, Due Logic

Current fee constants in code:

- Monthly base: Rs 3,500.
- One-time admission fee: Rs 500.
- 3 months base before discount: Rs 10,500.
- 3 months payable after 5% discount: Rs 9,975.
- 6 months base before discount: Rs 21,000.
- 6 months payable after 10% discount: Rs 18,900.
- Special training: Rs 10,000 per month.
- Admission first payment shows three separate values: coaching fee, one-time admission fee, and jersey amount.
- Jersey pairs are charged at Rs 750 per pair; there is no free first-pair rule.
- Custom/partial payment is allowed; admission submission must not be blocked when the amount paid differs from the calculated total.
- Current calculated admission totals before custom payment:
  - monthly admission: Rs 4,000 plus Rs 750 per jersey pair (example: 1 pair = Rs 4,750).
  - 3 months admission: Rs 10,475 plus Rs 750 per jersey pair.
  - 6 months admission: Rs 19,400 plus Rs 750 per jersey pair.
  - special admission: Rs 10,000 plus Rs 750 per jersey pair.
- Current renewal totals:
  - monthly: Rs 3,500.
  - 3 months: Rs 9,975.
  - 6 months: Rs 18,900.
  - special: Rs 10,000.
- In future if the user says "3 months is 10500 and 6 months is 21000", interpret that as pre-discount base unless they explicitly cancel discount.
- For due-date/month calculation, use the selected plan value/month count, not the manually typed amount. Sometimes academy may take a lower amount from a player.
- Joining-fee-pending players can be marked paid from manager actions. Save a `student_payments` row with `payment_type = joining`; use the selected payment date for finance (`paid_on`) and keep cycle start on the player's join date for next-fee-due logic.

Renewal logic:

- Next due should be based on the student's joining-day/cycle-day, not the date money was paid late.
- Example: joined March 3, paid renewal on April 10; next cycle starts April 3, not April 10.
- Renewal day should remain the same day-of-month as join/last cycle day where possible.
- 3-month and 6-month payments must extend due date by 3 or 6 months. Do not show only 30 days left for a 3-month or 6-month payment.
- If player is discontinued without paying renewal, track discontinued date and status; do not count future active duration.
- If discontinued and returns later, timeline/payment/training duration should reflect the gap.
- Alerts:
  - Blue/normal alert is "Alert" or regular fee/renewal follow-up.
  - Immediate follow-up/red alert is separate and only appears when there are players over the defined threshold.
  - Red alert should disappear when no immediate attention players exist.

## WhatsApp Reminders and UPI Payment

Provider selected: Meta WhatsApp Cloud API direct, not Twilio/WATI/360Dialog.

Current reminder template:

- Template name: `gen_alpha_fee_reminder`.
- Language: `en`.
- Body has exactly 2 variables:
  - `{{1}}` parent/player display name.
  - `{{2}}` due date only, e.g. `5th May`. Do not send text like "joining fee due from 2026-05-05".
- Template text:
  - `Hi {{1}}, your Gen Alpha Cricket Academy fee is due from {{2}}.`
  - Then it asks parent to choose renewal plan: 1 Month, 3 Months, 6 Months.
  - Need Help button is supported.
- Buttons/options:
  - 1 Month, 3 Months, 6 Months, Need Help.
  - If Need Help: log it and provide manager/help flow.
- Automatic scheduled reminders are NOT enabled yet. The user wants manual send only for now.
- Reminder policy planned for future automation:
  - reminders at overdue day 3, day 5, then daily until day 10.
  - after day 10, highlight in apps with immediate follow-up banner.
- Do not reintroduce dry-run UI/buttons unless requested. Current defaults in function are live/manual.
- Always log reminder activity to DB/timeline when applicable.

UPI/payment details:

- UPI ID: `9059962499@ybl`.
- Payment phone: `9059962499`.
- Account name: `Srinivas`.
- Bank: `Kotak Mahindra Bank`.
- Academy/UPI payee name: `Gen Alpha Cricket Academy`.
- Payment page: `https://genalphaacademy.in/pay.html`.
- Manager phone setting/default currently seen in reminder function: `8143960950`.
- Razorpay is not being used. It was paused because onboarding required PAN details. Do not add Razorpay back unless the user asks.

Payment UX:

- Web admission payment panel should be popup style and clean.
- Mobile browser should be able to open UPI apps.
- Desktop browser should show QR/payment path and not show unusable mobile-app tiles.
- Payment should not be mandatory to submit admission.
- Parent should not need to enter preferred app, parent UPI ID, or UTR unless explicitly needed. UPI does not automatically return reliable payment confirmation without a payment gateway.
- Current non-gateway UPI flow must not auto-mark paid. Treat parent payment claims as pending verification until a manager confirms in roster/edit/payment flow.
- Renewal WhatsApp payment flow:
  - Parent selects a renewal plan in WhatsApp.
  - Function sends payment page link with reminder event id.
  - When parent taps `Pay Now` on `pay.html`, call the WhatsApp function with `payment_attempted`, update reminder/payment-link status, and send: `After payment, just reply here with "Paid" or send the payment screenshot.`
  - If parent replies `Paid` or sends a screenshot/image/document in WhatsApp, mark reminder/payment-link as `payment_pending_verification` and reply: `Once the academy confirms the payment, we’ll update your renewal. Thank You!`
  - Store parent payment proof details in `reminder_events.meta_response.payment_confirmation`; raw inbound webhook payloads go to `whatsapp_webhook_events`; screenshot/document media should be stored in private Supabase Storage bucket `payment-proofs` when Meta media download succeeds.
  - Still do not mark renewal paid, extend due date, count finance revenue, or send receipt until manager verifies payment.
  - When manager records/verifies a renewal payment in roster, send a WhatsApp confirmation with the player name and renewal period from cycle start date to selected plan end date.
  - Manager verification should update reminder/payment-link status to `payment_confirmed` and add timeline/debug metadata where possible.

Receipts:

- Joining receipt and renewal receipt have different templates.
- Joining receipt includes jersey details.
- Renewal receipt is simpler.
- If parent marks fees paid as no, no receipt should be generated.
- Later, when marked paid, receipt should be generated/shareable/sendable.
- Receipt design should be modern, logo/theme-based, not old/plain.

## Supabase and Database

Database is Supabase Postgres.

Important tables/functions/migrations in repo:

- `students`: core player records.
- `admissions`: parent admission submissions.
- `registration_counters`: admission registration number sequencing.
- `attendance`: attendance marks, unique by student/date.
- `student_timeline`: player history/timeline.
- `student_payments`: structured joining/renewal payments.
- `academy_expenses`: finance expenses.
- `system_settings`: reminder/payment flags/settings.
- `reminder_events`: WhatsApp reminder events.
- `payment_link_requests`: payment-link/plan selection tracking.
- `whatsapp_webhook_events`: webhook/debug event log.
- `submit_admission_form(...)`: public admission RPC.
- `peek_next_admission_reg_no()`: preview registration number without incrementing.
- `approve_admission(...)`, `reject_admission(...)`: review queue workflow.
- `mark_player_attendance(...)`, `unmark_player_attendance(...)`: attendance RPCs.
- Supabase Edge Function: `supabase/functions/whatsapp-reminder/index.ts`.

Migration discipline:

- Do not rerun the full `schema.sql` on an existing database unless the user confirms a fresh/reset DB.
- Prefer incremental SQL migrations for new columns/features.
- Avoid destructive SQL unless explicitly requested.
- If Supabase SQL editor complains about "cannot change name of input parameter", drop the old function signature first or create a proper incremental function replacement.
- If SQL editor asks "Run and enable RLS" and appends extra SQL into the middle of a function, avoid that path; run clean SQL/CLI or split the file so dollar-quoted function bodies are complete.
- RLS is enabled on key tables; public admission and authenticated manager flows depend on policies/RPC grants.
- Realtime should include `students`, `attendance`, `student_payments`, and `academy_expenses` as needed. If instant sync breaks, check Supabase publication and app subscription handling.
- Timestamps should display in IST for user-facing DB/app records where relevant.

## Web App Implementation Notes

Key files:

- `/Users/jiths/Documents/New project/web-app-repo/index.html`
- `/Users/jiths/Documents/New project/web-app-repo/script.js`
- `/Users/jiths/Documents/New project/web-app-repo/styles.css`
- `/Users/jiths/Documents/New project/web-app-repo/supabase-config.js`
- `/Users/jiths/Documents/New project/web-app-repo/pay.html`
- `/Users/jiths/Documents/New project/web-app-repo/manifest.webmanifest`
- `/Users/jiths/Documents/New project/web-app-repo/assets/...`

Web expectations:

- Landing page before login: admission form only.
- After manager login: dashboard/roster/attendance/finance as appropriate.
- After logout: back to admission landing page only.
- Browser tab icon/favicon uses academy favicon/logo asset from web assets.
- PWA install icon should use the updated favicon/logo, not old stretched image.
- Cache/versioning: browser should not keep stale old version after updates. Check manifest/service worker/cache busting if old UI persists.
- Academy Moments/reels are currently intended mainly for browser. Instagram embedded playback is constrained by Instagram; if in-app direct playback is unreliable, keep a lightweight browser-only section with local/hosted feature video plus simple recent-post cards/embeds. Do not let this slow Android scrolling.

Web layout traps:

- Desktop roster should not be turned into mobile cards unexpectedly.
- Registered players and New Gen Alpha Entry panels had width issues on 13-inch MacBook Air; keep them centered/contained without ugly horizontal scroll when possible.
- Finance range panel must be compact and clean.
- Alert box in browser should keep Fees to collect and Renewal follow-up side by side with smaller text names.

## Android App Implementation Notes

Key files:

- `/Users/jiths/Documents/New project/android-app/app/src/main/java/com/genalpha/cricketacademy/ui/AcademyApp.kt`
- `/Users/jiths/Documents/New project/android-app/app/src/main/java/com/genalpha/cricketacademy/ui/AcademyViewModel.kt`
- `/Users/jiths/Documents/New project/android-app/app/src/main/java/com/genalpha/cricketacademy/data/SupabaseRepository.kt`
- `/Users/jiths/Documents/New project/android-app/app/src/main/java/com/genalpha/cricketacademy/data/Models.kt`
- `/Users/jiths/Documents/New project/android-app/app/src/main/AndroidManifest.xml`
- APK output: `/Users/jiths/Documents/New project/android-app/app/build/outputs/apk/debug/genAlpha-manager.apk`

Android package:

- `com.genalpha.cricketacademy`

Android UX rules:

- Must handle large system font and different phone sizes gracefully. Do not assume Samsung/default font scale only.
- Test with emulator screenshots/UI tree when possible for layout bugs.
- Avoid fixed vertical layouts that push buttons below screen.
- Staff login email/password dialog was changed from bottom sheet to centered compact dialog because bottom sheet put login button under gesture area on large font devices.
- PIN dialog should show 6 digit boxes and auto-unlock on 6th digit.
- Login password should be hidden.
- Dark mode must be pleasant:
  - badges/capsules should not be too bright.
  - popup backgrounds/text must remain readable.
  - finance pill metrics text must be visible.
- App should not use background resources while minimized. On return to foreground, refresh/resubscribe as needed.
- Android Instagram/reel previews made app slow; avoid heavy embedded web/media reels in Android unless carefully optimized.

Android build/test commands:

- Build debug APK:
  - `cd /Users/jiths/Documents/New project/android-app`
  - `./gradlew assembleDebug`
- Install into emulator:
  - `/Users/jiths/Library/Android/sdk/platform-tools/adb install -r /Users/jiths/Documents/New project/android-app/app/build/outputs/apk/debug/genAlpha-manager.apk`
- Launch:
  - `/Users/jiths/Library/Android/sdk/platform-tools/adb shell monkey -p com.genalpha.cricketacademy 1`
- Screenshot:
  - `/Users/jiths/Library/Android/sdk/platform-tools/adb exec-out screencap -p > /private/tmp/screen.png`
- UI dump:
  - `/Users/jiths/Library/Android/sdk/platform-tools/adb shell uiautomator dump /sdcard/window.xml`

## Removed or Paused Features

- AI/OCR upload/import for old handwritten admission forms is removed/paused. Do not re-add "AI Assist Upload old admission form" unless the user asks.
- Scan/upload document can be kept only as plain attachment if requested, but OCR/AI extraction was not good enough without paid/strong AI.
- Razorpay is paused/not integrated live.
- Automatic WhatsApp reminders are paused; manual send only.
- Do not bring back raw OCR text UI.
- Do not show parent-facing manager/internal helper text like "Manager tools stay locked" on public admission page.

## Assets and Branding

- Use clean academy logo/badge without stretching.
- App icon should not stretch the logo. Prefer square/non-distorted academy favicon/logo asset.
- Browser favicon/PWA icon should use the current clean favicon asset.
- Header should use logo once, not repeated.
- Keep header simple and tidy; no redundant Manikonda text unless part of image.
- Academy colors are blue/navy and gold/yellow from the logo, with red only for immediate attention.

## Data Safety and Git Rules

- There may be untracked or unrelated files in the worktree. Do not delete/revert unrelated changes unless the user explicitly asks.
- Use `git status` before commits.
- Commit/push to the correct repo:
  - Android: `/Users/jiths/Documents/New project`, `origin main`.
  - Web: `/Users/jiths/Documents/New project/web-app-repo`, `origin main`.
- If the user says "push both", push Android repo and web repo separately.
- If user says "git pull", pull the relevant repo(s). If context says both apps are involved, pull both.
- Do not use `git reset --hard` or destructive commands unless explicitly approved.

## Verification Checklist for Future Changes

Before final response, as applicable:

- For Android UI: run `./gradlew assembleDebug`; install/test emulator for layout-sensitive changes.
- For web UI: inspect relevant browser layout if a dev server/browser test is available.
- For Supabase/RPC changes: ensure function signatures match both web and Android payloads.
- For DB fields: confirm `students`, `admissions`, Android models, web mapping, and Supabase functions all agree.
- For realtime issues: test or inspect subscriptions/publication for affected table.
- For fees: test monthly/3-month/6-month/custom/special and due date calculations.
- For public admission: test fees paid yes/no, optional payment, phone validation, and review queue.
- For reminders: do not send real WhatsApp messages unless user explicitly asks and recipient is clear.

## Current Known Untracked Files

At time of writing, these were untracked in Android repo and should not be accidentally committed unless intentionally needed:

- `sample-receipts/`
- `supabase/.temp/`
