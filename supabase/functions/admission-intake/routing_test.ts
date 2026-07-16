import { shouldTargetWaitingReview } from "./routing.ts";

function assertRoute(
  expected: boolean,
  messageType: string,
  text: string,
  replyId = "",
) {
  const actual = shouldTargetWaitingReview(messageType, text, replyId);
  if (actual !== expected) {
    throw new Error(`Expected ${expected} for ${messageType}: ${text}; received ${actual}.`);
  }
}

Deno.test("new renewal conversation stays out of old waiting reviews", () => {
  assertRoute(false, "text", "Adil's payment");
  assertRoute(false, "text", "Renewal");
  assertRoute(false, "text", "Adil renewal for 1 month");
});

Deno.test("explicit review actions target the waiting review", () => {
  assertRoute(true, "text", "confirm");
  assertRoute(true, "text", "cancel this admission");
  assertRoute(true, "text", "ignore paid on date, mark payment pending");
  assertRoute(true, "text", "check for student name with Adil");
});

Deno.test("standalone media starts a case unless it replies to a review", () => {
  assertRoute(false, "image", "Paid using PhonePe UPI");
  assertRoute(true, "image", "Paid using PhonePe UPI", "wamid.review-message");
});
