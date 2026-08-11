# GenAlpha — the app repo (web + Android)

Tenant `genalpha` on the Academy Manager platform since 2026-08-10.

This folder now sits under `Academy Manager Business/`, so the platform
`CLAUDE.md` one level up **is inherited automatically** — the house rule,
the migration ledger and the security facts all apply here without being
restated. What follows is only what is specific to GenAlpha.

## The rules that apply here

- **Anything that computes money lives in Postgres.** Fees come from
  `genalpha.quote_fee()`, payments through `record_fee_payment()`. This
  app used to hardcode 3500/9975/18900 while 52 of 81 students were on a
  different rate; do not put a price back in JavaScript or Kotlin.
- **Every request carries the schema.** GenAlpha's tables are views in a
  `genalpha` schema over the shared ones. Web uses `db: { schema }`,
  Android sends `Accept-Profile`/`Content-Profile` from `baseRequest()`.
  Without it PostgREST resolves against `public` and returns 404.
- **The anon key reads nothing.** `genalpha.students` grants to
  `authenticated` only. Two calls are anon by design —
  `submit_admission_form` and `peek_next_admission_reg_no`, the public
  parent-facing form.
- **Do not put a PIN or password in the client.** The coach PIN is the
  password of `coach@genalphaacademy.in`; the database decides what that
  role sees, which is roster and attendance with contacts nulled.

## Layout

```
android-app/    native Kotlin/Compose, WebView for pay.html only
supabase/       GenAlpha-era SQL. Historical — new shared work goes in
                AcademyManager/supabase/migrations/ at --scope shared
```
