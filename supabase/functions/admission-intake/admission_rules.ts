export function normalizeAdmissionPlan<T extends Record<string, any>>(draft: T): T {
  const normalized: Record<string, any> = draft || {};
  const aliases: Record<string, string> = {
    "1month": "monthly",
    "1months": "monthly",
    "one month": "monthly",
    "3month": "quarterly",
    "3months": "quarterly",
    "three months": "quarterly",
    "6month": "halfyearly",
    "6months": "halfyearly",
    "six months": "halfyearly",
  };
  const rawPlan = String(normalized.fee_plan || "").toLowerCase().trim();
  const compactPlan = rawPlan.replace(/\s+/g, "");
  normalized.fee_plan = aliases[rawPlan] || aliases[compactPlan] || rawPlan;
  const months = Number(normalized.months_covered || 0);
  const planByMonths: Record<number, string> = { 1: "monthly", 3: "quarterly", 6: "halfyearly" };
  if (
    planByMonths[months] &&
    !["monthly", "quarterly", "halfyearly", "special", "custom"].includes(normalized.fee_plan)
  ) {
    normalized.fee_plan = planByMonths[months];
  }
  if (normalized.fee_plan === "monthly") normalized.months_covered = 1;
  if (normalized.fee_plan === "quarterly") normalized.months_covered = 3;
  if (normalized.fee_plan === "halfyearly") normalized.months_covered = 6;
  return normalized as T;
}

export function removeResolvedBlankFormPaymentConflicts(
  conflicts: unknown[],
  paymentDate: unknown,
): string[] {
  const values = (conflicts || []).map((value) => String(value)).filter(Boolean);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(String(paymentDate || ""))) return values;
  return values.filter((conflict) =>
    !/(?:chat|staff).*(?:while|but).*(?:form).*(?:no|blank|empty|not filled).*(?:fee|paid|payment|date)|form.*(?:no|blank|empty|not filled).*(?:fee|paid|payment|date)/i.test(conflict)
  );
}

export function shouldUseMediaAsPaymentProof(
  intakeType: string,
  evidenceType: string,
): boolean {
  return intakeType === "renewal" || evidenceType === "payment_screenshot";
}
