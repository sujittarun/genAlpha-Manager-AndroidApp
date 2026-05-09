# Gen Alpha Manager Changelog Notes

Last updated: 2026-05-09

This file records meaningful project changes and decisions so future Codex sessions can understand recent work without rereading the full chat. It is not a release changelog for users; it is a developer/manager memory log.

Use this file when:

- The user asks what changed recently.
- A new bug appears and we need to compare with recent edits.
- The user asks to revert a recent UI/logic change.
- A future agent needs to understand why a design or business rule exists.

For current source-of-truth rules, read `PROJECT_CONTEXT.md` first.

## 2026-05-09

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
