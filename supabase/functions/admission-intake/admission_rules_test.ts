import {
  normalizeAdmissionPlan,
  removeResolvedBlankFormPaymentConflicts,
  shouldUseMediaAsPaymentProof,
} from "./admission_rules.ts";

Deno.test("three-month admission instructions normalize to quarterly", () => {
  const draft = normalizeAdmissionPlan({ fee_plan: "pending", months_covered: 3 });
  if (draft.fee_plan !== "quarterly" || draft.months_covered !== 3) {
    throw new Error(`Unexpected normalized plan: ${JSON.stringify(draft)}`);
  }
});

Deno.test("staff payment date resolves a blank form-date conflict", () => {
  const conflicts = removeResolvedBlankFormPaymentConflicts([
    "Chat says paid 3 months / 10k, while form has no filled FEE Paid on date.",
    "A different unresolved conflict.",
  ], "2026-07-16");
  if (conflicts.length !== 1 || conflicts[0] !== "A different unresolved conflict.") {
    throw new Error(`Unexpected conflicts: ${JSON.stringify(conflicts)}`);
  }
});

Deno.test("admission form media is not payment proof", () => {
  if (shouldUseMediaAsPaymentProof("admission", "form_date_only")) {
    throw new Error("An admission form must not be promoted as payment proof.");
  }
  if (!shouldUseMediaAsPaymentProof("admission", "payment_screenshot")) {
    throw new Error("A classified payment screenshot should remain proof.");
  }
  if (!shouldUseMediaAsPaymentProof("renewal", "payment_screenshot")) {
    throw new Error("Renewal payment media should remain proof.");
  }
});
