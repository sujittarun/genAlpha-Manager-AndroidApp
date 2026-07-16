import {
  monthsForPlan,
  normalizeSelectedPlan,
  paymentAmountForReminderType,
} from "./payment_plans.ts";

Deno.test("special training uses the admission discount schedule", () => {
  const expected = new Map([[1, 10000], [2, 20000], [3, 28500], [6, 54000]]);
  for (const [months, amount] of expected) {
    const actual = paymentAmountForReminderType("renewal", "special", months);
    if (actual !== amount) throw new Error(`${months} months: expected ${amount}, got ${actual}`);
  }
});

Deno.test("special training does not add the joining fee", () => {
  const actual = paymentAmountForReminderType("joining_fee", "special", 1);
  if (actual !== 10000) throw new Error(`Expected 10000, got ${actual}`);
});

Deno.test("special training aliases and month bounds normalize safely", () => {
  if (normalizeSelectedPlan("Special Training") !== "special") {
    throw new Error("Special Training label did not normalize.");
  }
  if (monthsForPlan("special", 0) !== 1 || monthsForPlan("special", 99) !== 36) {
    throw new Error("Special Training month bounds are incorrect.");
  }
});
