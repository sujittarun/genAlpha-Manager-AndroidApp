const REVIEW_ACTION = /^(?:confirm(?:ed)?|approve(?:d)?|save(?: it)?|proceed|cancel|discard|reject|yes|yep|yeah|ok(?:ay)?|correct|all correct|looks good|all good|go ahead|do it|sure|done)(?:\s|$)/i;
const CORRECTION_CONTEXT = /\b(?:change|check|correct|correction|instead|actually|should be|update|edit|remove|ignore|mark (?:it )?as|pending|not paid|unpaid|not do(?:ne)?|wrong)\b/i;

export function shouldTargetWaitingReview(
  messageType: string,
  text: string,
  replyToProviderMessageId = "",
): boolean {
  if (replyToProviderMessageId.trim()) return true;
  if (messageType !== "text") return false;
  const normalized = text.trim().toLowerCase().replace(/[^a-z0-9 ]+/g, " ").replace(/\s+/g, " ");
  if (!normalized) return false;
  return REVIEW_ACTION.test(normalized) || CORRECTION_CONTEXT.test(normalized);
}

type WaitingReview = {
  intake_type?: string;
  confirmation_message_id?: string;
  updated_at?: string;
};

/**
 * Natural, unthreaded replies such as "yes" belong to the latest bot review
 * when that review is clearly newer than every other open review.
 * Otherwise the caller must ask staff to use WhatsApp Reply.
 */
export function selectWaitingReviewCandidate<T extends WaitingReview>(
  rows: T[],
): T | "ambiguous" | null {
  const candidates = rows
    .filter((row) =>
      (row.intake_type === "admission" || row.intake_type === "renewal") &&
      Boolean(row.confirmation_message_id?.trim()) &&
      Number.isFinite(Date.parse(String(row.updated_at || "")))
    )
    .sort((a, b) => Date.parse(String(b.updated_at)) - Date.parse(String(a.updated_at)));

  if (!candidates.length) return null;
  if (candidates.length === 1) return candidates[0];

  const latestAt = Date.parse(String(candidates[0].updated_at));
  const previousAt = Date.parse(String(candidates[1].updated_at));
  const isClearlyLatest = latestAt - previousAt >= 90_000;
  return isClearlyLatest ? candidates[0] : "ambiguous";
}
