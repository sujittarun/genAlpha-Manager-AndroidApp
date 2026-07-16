import { selectWaitingReviewCandidate, shouldTargetWaitingReview } from "./routing.ts";

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
  assertRoute(true, "text", "Yes");
  assertRoute(true, "text", "okay");
  assertRoute(true, "text", "looks good");
  assertRoute(true, "text", "cancel this admission");
  assertRoute(true, "text", "ignore paid on date, mark payment pending");
  assertRoute(true, "text", "check for student name with Adil");
  assertRoute(true, "text", "School is Sloka\nMark today as payment date and register for 3 months plan");
  assertRoute(true, "text", "Set batch to 5:30 PM and address is Jubilee Hills");
});

Deno.test("standalone media starts a case unless it replies to a review", () => {
  assertRoute(false, "image", "Paid using PhonePe UPI");
  assertRoute(true, "image", "Paid using PhonePe UPI", "wamid.review-message");
});

Deno.test("a natural confirmation selects a clearly active review", () => {
  const active = {
    id: "active",
    intake_type: "renewal",
    confirmation_message_id: "wamid.active",
    updated_at: "2026-07-16T10:16:00.000Z",
  };
  const stale = {
    id: "stale",
    intake_type: "renewal",
    confirmation_message_id: "wamid.stale",
    updated_at: "2026-07-16T10:03:00.000Z",
  };
  const orphan = {
    id: "orphan",
    intake_type: "unknown",
    confirmation_message_id: "wamid.orphan",
    updated_at: "2026-07-16T10:16:30.000Z",
  };
  const selected = selectWaitingReviewCandidate([orphan, stale, active]);
  if (selected === null || selected === "ambiguous" || selected.id !== "active") {
    throw new Error("Expected the fresh, valid renewal review to be selected.");
  }
});

Deno.test("similar open reviews remain ambiguous", () => {
  const base = {
    intake_type: "admission",
    confirmation_message_id: "wamid.review",
  };
  const close = selectWaitingReviewCandidate([
    { ...base, updated_at: "2026-07-16T10:16:00.000Z" },
    { ...base, updated_at: "2026-07-16T10:15:10.000Z" },
  ]);
  if (close !== "ambiguous") throw new Error("Close reviews must require a threaded reply.");
});
