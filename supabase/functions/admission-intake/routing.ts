const REVIEW_ACTION = /^(?:confirm|confirmed|approve|approved|save|proceed|cancel|discard|reject)(?:\s|$)/i;
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
