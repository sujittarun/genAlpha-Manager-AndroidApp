const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

type ReminderSettings = {
  whatsappRemindersEnabled: boolean;
  paymentLinksEnabled: boolean;
  dryRunMode: boolean;
  managerPhone: string;
};

const DEFAULT_SETTINGS: ReminderSettings = {
  whatsappRemindersEnabled: true,
  paymentLinksEnabled: true,
  dryRunMode: false,
  managerPhone: "8143960950",
};
const ACADEMY_UPI_ID = "9059962499@ybl";
const ACADEMY_PAYEE_NAME = "Gen Alpha Cricket Academy";
const ACADEMY_PAYMENT_PHONE = "9059962499";
const ACADEMY_PAYMENT_ACCOUNT_NAME = "Srinivas";
const ACADEMY_PAYMENT_BANK = "Kotak Mahindra Bank";
const PAYMENT_PAGE_URL = "https://genalphaacademy.in/pay.html";
const MANAGER_PAYMENT_ALERT_PHONE = "9985822772";
const MANAGER_PAYMENT_ALERT_DELAY_MINUTES = 5;

const PLAN_OPTIONS = ["monthly", "quarterly", "halfyearly", "need_help"];
const PLAN_LABELS: Record<string, string> = {
  monthly: "1 Month",
  quarterly: "3 Months",
  halfyearly: "6 Months",
  need_help: "Need Help",
};
const PLAN_AMOUNTS: Record<string, number> = {
  monthly: 3500,
  quarterly: 9975,
  halfyearly: 18900,
};
const PLAN_MONTHS: Record<string, number> = {
  monthly: 1,
  quarterly: 3,
  halfyearly: 6,
};
const PAID_PLAN_OPTIONS = PLAN_OPTIONS.filter((plan) => plan !== "need_help");
const DEFAULT_DIRECT_PAY_TEMPLATE_NAME = "gen_alpha_fee_direct_pay";
const HEALTHY_ECOSYSTEM_ERROR_CODE = "131049";
const HEALTHY_ECOSYSTEM_RETRY_MINUTES = [5, 30, 60];
const DEFAULT_REMINDER_MAX_RETRIES = HEALTHY_ECOSYSTEM_RETRY_MINUTES.length;
const RETRY_WORKER_LIMIT = 20;
const RETRY_RECOVERY_MINUTES = 10;
const MANUAL_FOLLOWUP_OVERDUE_DAYS = 15;

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}

function env(name: string): string {
  return Deno.env.get(name) || "";
}

function serviceHeaders() {
  const serviceRoleKey = env("SUPABASE_SERVICE_ROLE_KEY");
  return {
    apikey: serviceRoleKey,
    Authorization: `Bearer ${serviceRoleKey}`,
    "Content-Type": "application/json",
    Accept: "application/json",
  };
}

function normalizePhone(value: string): string {
  const digits = value.replace(/\D/g, "").slice(-10);
  return digits ? `91${digits}` : "";
}

function admissionIntakeStaffPhones(): Set<string> {
  return new Set(
    env("ADMISSION_INTAKE_STAFF_PHONES")
      .split(",")
      .map((value) => normalizePhone(value))
      .filter(Boolean),
  );
}

function isWhatsappContactBlocked(student: any): boolean {
  return ["wrong_number", "opted_out"].includes(
    String(student?.whatsapp_contact_status || "active").toLowerCase(),
  );
}

function whatsappContactBlockedMessage(student: any): string {
  return String(student?.whatsapp_contact_status || "") === "opted_out"
    ? "Parent has opted out of WhatsApp reminders."
    : "Saved WhatsApp number is marked wrong.";
}

function localIsoDate(date = new Date()): string {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Kolkata',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date);
}

function whatsappTimestampToIso(value: unknown): string {
  const seconds = Number(value || 0);
  return seconds > 0
    ? new Date(seconds * 1000).toISOString()
    : new Date().toISOString();
}


function addMonthsIso(dateValue: string, months: number): string {
  const date = new Date(`${dateValue}T00:00:00`);
  const originalDay = date.getDate();
  date.setDate(1);
  date.setMonth(date.getMonth() + months);
  const daysInTargetMonth = new Date(
    date.getFullYear(),
    date.getMonth() + 1,
    0,
  ).getDate();
  date.setDate(Math.min(originalDay, daysInTargetMonth));
  return localIsoDate(date);
}

function getDaysSinceDate(dateValue: string): number {
  if (!dateValue) return 0;
  const targetDate = new Date(`${dateValue}T00:00:00`);
  const msPerDay = 1000 * 60 * 60 * 24;
  const diff = new Date().getTime() - targetDate.getTime();
  return Math.floor(diff / msPerDay);
}

function getGenderTerms(studentName: string) {
  const name = studentName || "the player";
  return { 
    him: name, 
    his: `${name}'s`, 
    he: name, 
    child: "child" 
  };
}

function maxIsoDate(d1: string, d2: string): string {
  if (!d1) return d2;
  if (!d2) return d1;
  return d1 > d2 ? d1 : d2;
}

const ADMISSION_ONE_TIME_FEE = 2500;
const SPECIAL_TRAINING_MONTHLY_FEE = 10000;

function getSpecialTrainingDiscountRate(months: number): number {
  const safeMonths = Math.max(Math.floor(Number(months || 1)), 1);
  if (safeMonths >= 6) return 0.1;
  if (safeMonths >= 3) return 0.05;
  return 0;
}

function getSpecialTrainingAmountForMonths(months: number): number {
  const safeMonths = Math.max(Math.floor(Number(months || 1)), 1);
  return Math.round(
    SPECIAL_TRAINING_MONTHLY_FEE * safeMonths *
      (1 - getSpecialTrainingDiscountRate(safeMonths)),
  );
}

function inferSpecialTrainingMonthsFromAmount(amount: number): number {
  const roundedAmount = Math.round(Number(amount || 0));
  if (roundedAmount <= 0) return 1;
  for (let months = 1; months <= 36; months += 1) {
    if (getSpecialTrainingAmountForMonths(months) === roundedAmount) return months;
  }
  if (roundedAmount >= getSpecialTrainingAmountForMonths(6)) {
    return Math.max(
      Math.round(roundedAmount / (SPECIAL_TRAINING_MONTHLY_FEE * 0.9)),
      1,
    );
  }
  if (roundedAmount >= getSpecialTrainingAmountForMonths(3)) {
    return Math.max(
      Math.round(roundedAmount / (SPECIAL_TRAINING_MONTHLY_FEE * 0.95)),
      1,
    );
  }
  return Math.max(Math.round(roundedAmount / SPECIAL_TRAINING_MONTHLY_FEE), 1);
}

function getPaymentMonthsCovered(payment: any): number {
  const plan = payment.plan_type || payment.planType;
  const explicitMonths = Number(payment.months_covered || payment.monthsCovered || 0);
  const amount = Math.round(Number(payment.amount || 0));
  if (String(plan || "").toLowerCase() === "special") {
    return Math.max(
      explicitMonths > 0 ? explicitMonths : 0,
      inferSpecialTrainingMonthsFromAmount(amount),
      1,
    );
  }
  if (explicitMonths > 0) return explicitMonths;
  if (plan === "monthly") return 1;
  if (plan === "quarterly") return 3;
  if (plan === "halfyearly") return 6;

  if (amount >= 18900) return 6;
  if (amount >= 9975) return 3;
  if (amount >= 3500) return 1;
  return 0;
}

function toCsv(headers: string[], rows: any[]): string {
  const headerRow = headers.join(",");
  const bodyRows = rows.map((row) =>
    headers
      .map((header) => {
        const val = row[header] ?? "";
        const escaped = String(val).replace(/"/g, '""');
        return `"${escaped}"`;
      })
      .join(",")
  );
  return [headerRow, ...bodyRows].join("\n");
}

async function sendResendEmail(
  to: string | string[],
  subject: string,
  html: string,
  attachments: { filename: string; content: string }[],
) {
  const apiKey = env("RESEND_API_KEY");
  if (!apiKey) throw new Error("RESEND_API_KEY not configured.");

  const response = await fetch("https://api.resend.com/emails", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify({
      from: "Gen Alpha Backup <onboarding@resend.dev>",
      to,
      subject,
      html,
      attachments: attachments.map((a) => ({
        filename: a.filename,
        content: btoa(a.content),
      })),
    }),
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(`Resend API error: ${error}`);
  }

  return await response.json();
}

async function handleAutoBackup(payload: any) {
  const targetEmail = payload?.email || ["tarun.sujit@gmail.com", "genalphacricketacademy@gmail.com"];
  
  // 1. Fetch data
  const [students, payments, expenses, attendance] = await Promise.all([
    rest("students?order=name.asc"),
    rest("student_payments?order=paid_on.desc"),
    rest("academy_expenses?order=expense_date.desc"),
    rest("attendance?order=attendance_date.desc&limit=2000"),
  ]);

  // 2. Convert to CSV
  const studentCsv = students.length > 0 ? toCsv(Object.keys(students[0]), students) : "No data";
  const paymentsCsv = payments.length > 0 ? toCsv(Object.keys(payments[0]), payments) : "No data";
  const expensesCsv = expenses.length > 0 ? toCsv(Object.keys(expenses[0]), expenses) : "No data";
  const attendanceCsv = attendance.length > 0 ? toCsv(Object.keys(attendance[0]), attendance) : "No data";

  const today = localIsoDate();

  // 3. Send Email
  await sendResendEmail(
    targetEmail,
    `Gen Alpha Academy Backup - ${today}`,
    `
      <h2>Gen Alpha Cricket Academy Data Backup</h2>
      <p>Automated backup performed on <strong>${today}</strong>.</p>
      <p>Please find the attached CSV files for:</p>
      <ul>
        <li>Registered Students</li>
        <li>Payment Ledger</li>
        <li>Expense Records</li>
        <li>Recent Attendance</li>
      </ul>
      <p><em>This is an automated system backup.</em></p>
    `,
    [
      { filename: `students_${today}.csv`, content: studentCsv },
      { filename: `payments_${today}.csv`, content: paymentsCsv },
      { filename: `expenses_${today}.csv`, content: expensesCsv },
      { filename: `attendance_${today}.csv`, content: attendanceCsv },
    ],
  );

  return jsonResponse({ success: true, message: `Backup sent to ${targetEmail}` });
}

function getInitialCoverageMonths(student: any): number {
  const isPaid = student.fees_paid === true || 
                 String(student.fees_paid).toLowerCase() === "true" || 
                 String(student.fees_paid).toLowerCase() === "yes";
                 
  if (!isPaid || Number(student.amount_paid || 0) <= 0) return 0;
  const amount = Number(student.amount_paid || 0);
  if (String(student.fee_plan || student.feePlan || "").toLowerCase() === "special") {
    return inferSpecialTrainingMonthsFromAmount(amount);
  }
  const withoutAdmissionFee = Math.max(amount - ADMISSION_ONE_TIME_FEE, 0);
  const roundedAmount = Math.round(amount);

  if (
    withoutAdmissionFee >= 18900 ||
    [18900, 19400, 20000, 20500, 21000].includes(roundedAmount)
  ) return 6;
  if (
    [9000, 9500, 9975, 10475, 10500, 11000].includes(roundedAmount) ||
    withoutAdmissionFee >= 9975
  ) return 3;
  if (
    [3500, 4000, 6000, 6500].includes(roundedAmount) ||
    withoutAdmissionFee >= 3500
  ) return 1;
  return 1; // Default to 1 month for any joining payment
}

function getPaidThroughDate(student: any, payments: any[]): string {
  const isPaid = student.fees_paid === true || student.fees_paid === "yes";
  let paidThrough = isPaid
    ? addMonthsIso(student.join_date, getInitialCoverageMonths(student))
    : student.join_date;

  if (!paidThrough) return localIsoDate();

  const studentPayments = payments.filter((p) => p.student_id === student.id);

  studentPayments.forEach((payment) => {
    const cycleStart = payment.cycle_start_date ||
      payment.cycleStartDate ||
      payment.paid_on ||
      payment.paidOn;
    const monthsCovered = getPaymentMonthsCovered(payment);
    if (cycleStart) {
      paidThrough = maxIsoDate(
        paidThrough,
        addMonthsIso(cycleStart, monthsCovered),
      );
    }
  });

  return paidThrough;
}

function encodeUpiValue(value: string): string {
  return encodeURIComponent(value).replace(
    /[!'()*]/g,
    (character) => `%${character.charCodeAt(0).toString(16).toUpperCase()}`,
  );
}

function encodeUpiPayeeAddress(value: string): string {
  return value.trim();
}

function buildUpiLink(student: any, plan: string, amount: number): string {
  const note = `Gen Alpha ${PLAN_LABELS[plan]} fee - ${
    student.name || "Player"
  }`;
  const params = [
    ["pa", encodeUpiPayeeAddress(ACADEMY_UPI_ID)],
    ["pn", ACADEMY_PAYEE_NAME],
    ["tn", note],
    ["am", amount.toFixed(2)],
    ["cu", "INR"],
  ];
  const query = params
    .filter(([, value]) => String(value || "").trim())
    .map(([key, value]) =>
      key === "pa"
        ? `${key}=${String(value)}`
        : `${key}=${encodeUpiValue(String(value))}`
    )
    .join("&");
  return `upi://pay?${query}`;
}

function paymentAmountForReminderType(reminderType: string, plan: string): number {
  const baseAmount = PLAN_AMOUNTS[plan] || 0;
  return reminderType === "joining_fee" ? (baseAmount + 500) : baseAmount;
}

function buildPaymentPageUrl(
  student: any,
  plan: string,
  amount: number,
  eventId = "",
): string {
  const params = new URLSearchParams({
    a: amount.toFixed(2),
    p: PLAN_LABELS[plan] || "fees",
    name: String(student.name || "Player"),
  });
  if (eventId) {
    params.set("e", eventId);
  }
  return `${PAYMENT_PAGE_URL}?${params.toString()}`;
}

function buildDirectPaymentPageUrl(eventId: string): string {
  const params = new URLSearchParams({ e: eventId });
  return `${PAYMENT_PAGE_URL}?${params.toString()}`;
}

function buildPaymentOptions(reminderEvent: any) {
  const reminderType = String(reminderEvent?.reminder_type || "renewal");
  return PAID_PLAN_OPTIONS.map((plan) => ({
    id: plan,
    label: PLAN_LABELS[plan],
    months: PLAN_MONTHS[plan],
    amount: paymentAmountForReminderType(reminderType, plan),
  }));
}

function paymentContactDetails(): string {
  return [
    `UPI ID: ${ACADEMY_UPI_ID}`,
    `Phone: ${ACADEMY_PAYMENT_PHONE}`,
    `Name: ${ACADEMY_PAYMENT_ACCOUNT_NAME}`,
    ACADEMY_PAYMENT_BANK,
  ].join("\n");
}

const AFTER_PAY_NOW_FOLLOWUP =
  "✅ Once payment is complete, please *send the screenshot* here. This helps our manager verify and update your kid's status immediately.";

const PAYMENT_CONFIRMATION_REPLY =
  "Once the academy confirms the payment, we’ll update your renewal. Thank You!";

function normalizeChoiceText(value: unknown): string {
  return String(value || "").toLowerCase().replace(/[^a-z0-9]/g, "");
}

function normalizeSelectedPlan(value: unknown): string {
  const normalized = normalizeChoiceText(value);
  if (
    ["monthly", "month", "1month", "one1month", "onemonth", "1"].includes(
      normalized,
    )
  ) {
    return "monthly";
  }
  if (
    ["quarterly", "3month", "3months", "threemonths", "3"].includes(normalized)
  ) {
    return "quarterly";
  }
  if (
    ["halfyearly", "6month", "6months", "sixmonths", "6"].includes(normalized)
  ) {
    return "halfyearly";
  }
  if (["needhelp", "help", "support"].includes(normalized)) {
    return "need_help";
  }
  return "";
}

function normalizeSampleReminderFlow(value: unknown, action: unknown): string {
  const requested = normalizeChoiceText(value);
  if (
    ["legacy", "old", "v1", "classic", "buttons", "planbuttons"].includes(
      requested,
    )
  ) {
    return "legacy";
  }
  if (
    ["directpay", "directpayment", "paynow", "new", "v2"].includes(requested)
  ) {
    return "direct_pay";
  }

  const actionName = normalizeChoiceText(action);
  if (actionName.includes("legacy") || actionName.includes("old")) {
    return "legacy";
  }
  if (
    actionName.includes("direct") ||
    actionName.includes("paynow") ||
    actionName.includes("v2") ||
    actionName.includes("new")
  ) {
    return "direct_pay";
  }

  return "direct_pay";
}


function parseBoolean(value: unknown, fallback: boolean): boolean {
  if (typeof value === "boolean") return value;
  if (typeof value === "string") return value.toLowerCase() === "true";
  return fallback;
}


async function rest(path: string, init: RequestInit = {}) {
  const baseUrl = env("SUPABASE_URL").replace(/\/+$/, "");
  const response = await fetch(`${baseUrl}/rest/v1/${path}`, {
    ...init,
    headers: {
      ...serviceHeaders(),
      ...(init.headers || {}),
    },
  });
  const text = await response.text();
  const body = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(body?.message || body?.error || response.statusText);
  }
  return body;
}

async function forwardToAdmissionIntake(value: any, message: any) {
  if (!parseBoolean(env("ADMISSION_INTAKE_ENABLED"), false)) return null;
  const configuredPhoneNumberId = env("META_ADMISSION_PHONE_NUMBER_ID");
  const incomingPhoneNumberId = String(value?.metadata?.phone_number_id || "");
  const reminderPhoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  const sharedNumber = parseBoolean(env("ADMISSION_INTAKE_SHARED_NUMBER"), false);
  if (!configuredPhoneNumberId || incomingPhoneNumberId !== configuredPhoneNumberId) {
    return null;
  }
  if (configuredPhoneNumberId === reminderPhoneNumberId && !sharedNumber) {
    return null;
  }

  // The reminder number also receives replies from parents. Only explicitly
  // allowlisted staff may start or correct an admission intake, so enabling the
  // shared number can never redirect all inbound conversations to the model.
  const senderPhone = normalizePhone(String(message?.from || ""));
  const allowedStaffPhones = admissionIntakeStaffPhones();
  if (!senderPhone || !allowedStaffPhones.has(senderPhone)) {
    return null;
  }

  const groupId = String(message?.group_id || message?.context?.group_id || "");
  const contact = (value?.contacts || []).find((item: any) =>
    String(item?.wa_id || "") === String(message?.from || "")
  ) || value?.contacts?.[0];
  const messageType = String(message?.type || "text");
  const response = await fetch(
    `${env("SUPABASE_URL").replace(/\/+$/, "")}/functions/v1/admission-intake`,
    {
      method: "POST",
      headers: serviceHeaders(),
      body: JSON.stringify({
        action: "ingest",
        channel: groupId ? "whatsapp_group" : "whatsapp",
        message: {
          provider_message_id: String(message?.id || ""),
          source_chat_id: groupId || String(message?.from || ""),
          source_sender_id: String(message?.from || ""),
          source_sender_name: String(contact?.profile?.name || ""),
          reply_to_provider_message_id: String(message?.context?.id || ""),
          message_type: messageType,
          text_body: String(
            message?.text?.body || message?.[messageType]?.caption ||
              message?.interactive?.button_reply?.title || "",
          ),
          media_id: String(message?.[messageType]?.id || ""),
          media_mime_type: String(message?.[messageType]?.mime_type || ""),
          media_filename: String(message?.document?.filename || ""),
          timestamp: message?.timestamp,
          raw_payload: message,
        },
      }),
    },
  );
  const body = await response.json().catch(() => ({}));
  if (!response.ok || body?.success === false) {
    throw new Error(body?.error || `Admission intake returned ${response.status}.`);
  }
  return body;
}

async function assertAuthenticated(request: Request) {
  const authHeader = request.headers.get("authorization") || "";
  const token = authHeader.replace(/^Bearer\s+/i, "");
  if (!token) throw new Error("Manager login is required.");

  const authApiKey = request.headers.get("apikey") ||
    env("SUPABASE_ANON_KEY") ||
    env("SUPABASE_SERVICE_ROLE_KEY");
  if (!authApiKey) throw new Error("Supabase auth secret is missing.");

  const response = await fetch(
    `${env("SUPABASE_URL").replace(/\/+$/, "")}/auth/v1/user`,
    {
      headers: {
        apikey: authApiKey,
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (!response.ok) {
    const body = await response.text();
    throw new Error(
      `Manager session is not valid (${response.status}). ${
        body || response.statusText
      }`,
    );
  }
  const user = await response.json();
  return user?.email || "manager";
}

async function assertAuthenticatedOrServiceRole(request: Request) {
  const cronSecret = env("WHATSAPP_CRON_SECRET");
  const providedCronSecret = request.headers.get("x-cron-secret") || "";
  if (cronSecret && providedCronSecret && providedCronSecret === cronSecret) {
    return "system_cron_secret";
  }

  const authHeader = request.headers.get("authorization") || "";
  const token = authHeader.replace(/^Bearer\s+/i, "");
  if (token && token === env("SUPABASE_SERVICE_ROLE_KEY")) {
    return "system_cron";
  }
  if (token) {
    const payload = decodeJwtPayload(token);
    const role = String(payload?.role || "").toLowerCase();
    if (role === "service_role") {
      return "system_cron_service_role";
    }
  }
  return await assertAuthenticated(request);
}

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  const payload = token.split(".")[1] || "";
  if (!payload) return null;
  try {
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(
      normalized.length + ((4 - normalized.length % 4) % 4),
      "=",
    );
    return JSON.parse(atob(padded));
  } catch (_error) {
    return null;
  }
}

async function loadSettings(): Promise<ReminderSettings> {
  const rows = await rest(
    "system_settings?select=setting_key,setting_value&setting_key=in.(whatsapp_reminders_enabled,payment_links_enabled,dry_run_mode)",
  );
  const byKey = Object.fromEntries(
    (rows || []).map((row: any) => [row.setting_key, row.setting_value]),
  );
  return {
    whatsappRemindersEnabled: parseBoolean(
      byKey.whatsapp_reminders_enabled,
      DEFAULT_SETTINGS.whatsappRemindersEnabled,
    ),
    paymentLinksEnabled: parseBoolean(
      byKey.payment_links_enabled,
      DEFAULT_SETTINGS.paymentLinksEnabled,
    ),
    dryRunMode: parseBoolean(byKey.dry_run_mode, DEFAULT_SETTINGS.dryRunMode),
    managerPhone: DEFAULT_SETTINGS.managerPhone,
  };
}

async function fetchStudent(studentId: string) {
  const rows = await rest(
    `students?select=*&id=eq.${encodeURIComponent(studentId)}&limit=1`,
  );
  const student = rows?.[0];
  if (!student) throw new Error("Student not found.");
  return student;
}

async function insertReminderEvent(payload: Record<string, unknown>) {
  const rows = await rest("reminder_events?select=*", {
    method: "POST",
    headers: { Prefer: "return=representation" },
    body: JSON.stringify(payload),
  });
  return rows?.[0];
}

async function insertPaymentLinkRequest(payload: Record<string, unknown>) {
  const rows = await rest("payment_link_requests?select=*", {
    method: "POST",
    headers: { Prefer: "return=representation" },
    body: JSON.stringify(payload),
  });
  return rows?.[0];
}

async function insertWebhookEvent(payload: Record<string, unknown>) {
  try {
    const rows = await rest("whatsapp_webhook_events?select=*", {
      method: "POST",
      headers: { Prefer: "return=representation" },
      body: JSON.stringify(payload),
    });
    return rows?.[0] || null;
  } catch (_error) {
    return null;
  }
}

async function insertStudentTimelineEvent(payload: Record<string, unknown>) {
  try {
    await rest("student_timeline", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  } catch (_error) {
    // Timeline logging must never block the parent or manager payment flow.
  }
}

async function insertWhatsappFlowEvent(payload: Record<string, unknown>) {
  try {
    const rows = await rest("whatsapp_flow_events?select=*", {
      method: "POST",
      headers: { Prefer: "return=representation" },
      body: JSON.stringify(payload),
    });
    return rows?.[0] || null;
  } catch (_error) {
    // Audit logging must never block parent-facing WhatsApp/payment flows.
    return null;
  }
}

async function findWhatsappFlowEventByMessageId(messageId: string) {
  if (!messageId) return null;
  try {
    const rows = await rest(
      `whatsapp_flow_events?select=*&message_id=eq.${
        encodeURIComponent(messageId)
      }&order=created_at.desc&limit=1`,
    );
    return rows?.[0] || null;
  } catch (_error) {
    return null;
  }
}

async function updateWhatsappFlowEvent(
  id: string,
  payload: Record<string, unknown>,
) {
  try {
    await rest(`whatsapp_flow_events?id=eq.${encodeURIComponent(id)}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    });
  } catch (_error) {
    // Audit status updates must never block Meta webhook processing.
  }
}

async function updateReminderEvent(
  id: string,
  payload: Record<string, unknown>,
) {
  await rest(`reminder_events?id=eq.${encodeURIComponent(id)}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

async function markReminderContactBlocked(event: any, student: any) {
  if (!event?.id) return;
  const optedOut = String(student?.whatsapp_contact_status || "") === "opted_out";
  await updateReminderEvent(event.id, {
    status: "manual_followup",
    manual_followup_required: true,
    manual_followup_reason: optedOut
      ? "whatsapp_opted_out"
      : "wrong_phone_number",
    next_retry_at: null,
    retry_reason: optedOut
      ? "Parent opted out. Automatic WhatsApp reminders and retries are paused."
      : "Wrong phone number. Automatic WhatsApp reminders and retries are paused.",
  });
}

async function findReminderByAnyWhatsappMessageId(messageId: string) {
  if (!messageId) return null;
  const encoded = encodeURIComponent(messageId);
  const rows = await rest(
    `reminder_events?select=*&or=(whatsapp_message_id.eq.${encoded},confirmation_message_id.eq.${encoded})&limit=1`,
  );
  return rows?.[0] || null;
}

async function findReminderEvent(eventId: string) {
  const rows = await rest(
    `reminder_events?select=*&id=eq.${encodeURIComponent(eventId)}&limit=1`,
  );
  return rows?.[0] || null;
}

async function updateLatestPaymentLinkRequest(
  reminderEventId: string,
  payload: Record<string, unknown>,
) {
  const rows = await rest(
    `payment_link_requests?select=id&reminder_event_id=eq.${
      encodeURIComponent(reminderEventId)
    }&order=created_at.desc&limit=1`,
  );
  const latest = rows?.[0];
  if (!latest?.id) return;
  await rest(`payment_link_requests?id=eq.${encodeURIComponent(latest.id)}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

async function updateReminderEventByWhatsappMessageId(
  messageId: string,
  payload: Record<string, unknown>,
) {
  await rest(
    `reminder_events?whatsapp_message_id=eq.${encodeURIComponent(messageId)}`,
    {
      method: "PATCH",
      body: JSON.stringify(payload),
    },
  );
}

async function findLatestReminderByPhone(phone: string) {
  const parentPhone = phone.slice(-10);
  const rows = await rest(
    `reminder_events?select=*&parent_phone=eq.${
      encodeURIComponent(parentPhone)
    }&order=created_at.desc&limit=1`,
  );
  return rows?.[0] || null;
}

function buildReminderPreview(
  student: any,
  dueDate: string,
  settings: ReminderSettings,
  reminderType: string,
) {
  return buildReminderMessageBody(student, dueDate, reminderType, settings.managerPhone);
}

function ordinalDay(day: number): string {
  if (day >= 11 && day <= 13) return `${day}th`;
  const lastDigit = day % 10;
  if (lastDigit === 1) return `${day}st`;
  if (lastDigit === 2) return `${day}nd`;
  if (lastDigit === 3) return `${day}rd`;
  return `${day}th`;
}

function buildReminderDueText(reminderType: string, dueDate: string) {
  const [year, month, day] = String(dueDate || "").split("-").map(Number);
  if (!year || !month || !day) return String(dueDate || "");

  const monthName = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ][month - 1];

  const dateFormatted = monthName ? `${ordinalDay(day)} ${monthName}` : String(dueDate || "");
  
  if (reminderType === "joining_fee") {
    return `${dateFormatted} (Admission + 1st Month)`;
  }
  
  if (reminderType === "renewal_day") {
    return dateFormatted;
  }

  return dateFormatted;
}

function renderTemplateBody(pattern: string, values: Record<string, string>) {
  return String(pattern || "").replace(/\{\{\s*(\d+)\s*\}\}/g, (_match, key) => {
    return values[String(key)] ?? "";
  });
}

function reminderTemplateBodyPattern(reminderType: string) {
  if (reminderType === "heads_up") {
    return env("META_WHATSAPP_HEADS_UP_TEMPLATE_BODY") ||
      "Academy fee renewal notice for {{1}}.\n\nDue date: {{2}}\n\nPlease select a payment option below to complete the fee payment. For billing help, tap Need Help.";
  }
  if (reminderType === "renewal_day") {
    return env("META_WHATSAPP_RENEWAL_DAY_TEMPLATE_BODY") ||
      "Today is {{1}}'s academy fee renewal date for {{2}}.\n\nPlease select a payment option below to complete the fee renewal. For billing help, tap Need Help.";
  }
  return env("META_WHATSAPP_TEMPLATE_BODY") ||
    "Hi {{1}}, this is an academy fee payment reminder from Gen Alpha Cricket Academy.\n\nFee due from: {{2}}\n\nPlease select a payment option below to complete the fee renewal. If payment is already completed or you need billing help, tap Need Help.";
}

function buildReminderMessageBody(
  student: any,
  dueDate: string,
  reminderType: string,
  _managerPhone = "",
) {
  return renderTemplateBody(reminderTemplateBodyPattern(reminderType), {
    "1": String(student?.name || "Player"),
    "2": buildReminderDueText(reminderType, dueDate),
  });
}

function resolveTemplateName(
  envName: string,
  utilityTemplateName: string,
  legacyTemplateNames: string[] = [],
) {
  const configured = env(envName);
  if (!configured || legacyTemplateNames.includes(configured)) {
    return utilityTemplateName;
  }
  return configured;
}

function reminderTemplateName(reminderType: string) {
  if (reminderType === "heads_up") {
    return resolveTemplateName(
      "META_WHATSAPP_HEADS_UP_TEMPLATE_NAME",
      "utlity_fee_headsup",
      ["gen_alpha_fee_heads_up", "utility_fee_headsup"],
    );
  }
  if (reminderType === "renewal_day") {
    return resolveTemplateName(
      "META_WHATSAPP_RENEWAL_DAY_TEMPLATE_NAME",
      "utility_renewal_day",
      ["gen_alpha_renewal_day"],
    );
  }
  return resolveTemplateName(
    "META_WHATSAPP_TEMPLATE_NAME",
    "utility_for_fee_reminder",
    ["gen_alpha_fee_reminder"],
  );
}

function directPaymentTemplateName() {
  return env("META_WHATSAPP_DIRECT_PAY_TEMPLATE_NAME") ||
    DEFAULT_DIRECT_PAY_TEMPLATE_NAME;
}

function isDirectPaymentTemplateEnabled(settings: ReminderSettings): boolean {
  return settings.paymentLinksEnabled &&
    parseBoolean(env("WHATSAPP_DIRECT_PAY_ENABLED"), true);
}

function displayDate(value: string): string {
  const [year, month, day] = String(value || "").split("-").map(Number);
  if (!year || !month || !day) return String(value || "");

  const monthName = [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
  ][month - 1];

  return monthName ? `${ordinalDay(day)} ${monthName} ${year}` : String(value || "");
}

function buildDirectPaymentMessageBody(
  student: any,
  dueDate: string,
  reminderType: string,
) {
  return renderTemplateBody(
    env("META_WHATSAPP_DIRECT_PAY_TEMPLATE_BODY") ||
      "Hi {{1}}, your Gen Alpha Cricket Academy fee is due from {{2}}.\n\nTap Pay Now to choose 1/3/6 months and complete UPI payment. After payment, reply here with Paid or send the payment screenshot.",
    {
      "1": String(student?.name || "Player"),
      "2": buildReminderDueText(reminderType, dueDate),
    },
  );
}

async function sendTemplateMessage(
  to: string,
  eventId: string,
  student: any,
  dueDate: string,
  reminderType: string,
) {
  const token = env("META_WHATSAPP_TOKEN");
  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  const isHeadsUp = reminderType === "heads_up";
  const templateName = reminderTemplateName(reminderType);
  const languageCode = env("META_WHATSAPP_TEMPLATE_LANGUAGE") || "en";
  const renderedMessageBody = buildReminderMessageBody(student, dueDate, reminderType);
  if (!token || !phoneNumberId) {
    throw new Error("Meta WhatsApp secrets are missing.");
  }

  const components = [
    ...(!isHeadsUp
      ? [
          {
            type: "header",
            parameters: [
              {
                type: "image",
                image: {
                  link: "https://genalphaacademy.in/assets/og-image.jpg",
                },
              },
            ],
          },
        ]
      : []),
    {
      type: "body",
      parameters: [
        { type: "text", text: student.name || "Player" },
        {
          type: "text",
          text: buildReminderDueText(reminderType, dueDate),
        },
      ],
    },
    {
      type: "button",
      sub_type: "quick_reply",
      index: "0",
      parameters: [
        { type: "payload", payload: `renewal:${eventId}:monthly` },
      ],
    },
    {
      type: "button",
      sub_type: "quick_reply",
      index: "1",
      parameters: [
        { type: "payload", payload: `renewal:${eventId}:quarterly` },
      ],
    },
    {
      type: "button",
      sub_type: "quick_reply",
      index: "2",
      parameters: [
        { type: "payload", payload: `renewal:${eventId}:halfyearly` },
      ],
    },
    {
      type: "button",
      sub_type: "quick_reply",
      index: "3",
      parameters: [
        { type: "payload", payload: `renewal:${eventId}:need_help` },
      ],
    },
  ];

  const response = await fetch(
    `https://graph.facebook.com/v20.0/${phoneNumberId}/messages`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messaging_product: "whatsapp",
        to,
        type: "template",
        template: {
          name: templateName,
          language: { code: languageCode },
          components,
        },
      }),
    },
  );

  const body = await response.json();
  if (!response.ok) {
    throw new Error(JSON.stringify(body?.error || body));
  }
  return {
    ...body,
    local_template_name: templateName,
    local_template_language: languageCode,
    local_message_body: renderedMessageBody,
  };
}

async function sendDirectPaymentTemplateMessage(
  to: string,
  eventId: string,
  student: any,
  dueDate: string,
  reminderType: string,
) {
  const token = env("META_WHATSAPP_TOKEN");
  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  const templateName = directPaymentTemplateName();
  const languageCode = env("META_WHATSAPP_TEMPLATE_LANGUAGE") || "en";
  const renderedMessageBody = buildDirectPaymentMessageBody(
    student,
    dueDate,
    reminderType,
  );
  if (!token || !phoneNumberId) {
    throw new Error("Meta WhatsApp secrets are missing.");
  }

  const response = await fetch(
    `https://graph.facebook.com/v20.0/${phoneNumberId}/messages`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messaging_product: "whatsapp",
        to,
        type: "template",
        template: {
          name: templateName,
          language: { code: languageCode },
          components: [
            {
              type: "body",
              parameters: [
                { type: "text", text: student.name || "Player" },
                {
                  type: "text",
                  text: buildReminderDueText(reminderType, dueDate),
                },
              ],
            },
            {
              type: "button",
              sub_type: "url",
              index: "0",
              parameters: [
                { type: "text", text: eventId },
              ],
            },
            {
              type: "button",
              sub_type: "quick_reply",
              index: "1",
              parameters: [
                { type: "payload", payload: `renewal:${eventId}:need_help` },
              ],
            },
          ],
        },
      }),
    },
  );

  const body = await response.json();
  if (!response.ok) {
    throw new Error(JSON.stringify(body?.error || body));
  }
  return {
    ...body,
    local_template_name: templateName,
    local_template_language: languageCode,
    local_message_body: renderedMessageBody,
    local_payment_page_url: buildDirectPaymentPageUrl(eventId),
  };
}

async function sendTemplatePayload(
  to: string,
  templateName: string,
  components: Record<string, unknown>[] = [],
) {
  const token = env("META_WHATSAPP_TOKEN");
  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  const languageCode = env("META_WHATSAPP_TEMPLATE_LANGUAGE") || "en";
  if (!token || !phoneNumberId) {
    throw new Error("Meta WhatsApp secrets are missing.");
  }

  const response = await fetch(
    `https://graph.facebook.com/v20.0/${phoneNumberId}/messages`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messaging_product: "whatsapp",
        to,
        type: "template",
        template: {
          name: templateName,
          language: { code: languageCode },
          components,
        },
      }),
    },
  );

  const body = await response.json();
  if (!response.ok) {
    throw new Error(JSON.stringify(body?.error || body));
  }
  return body;
}

async function graphRequest(path: string, init: RequestInit = {}) {
  const token = env("META_WHATSAPP_TOKEN");
  if (!token) throw new Error("META_WHATSAPP_TOKEN is missing.");
  const cleanPath = path.replace(/^\/+/, "");
  const headers = new Headers(init.headers);
  headers.set("Authorization", `Bearer ${token}`);
  headers.set("Content-Type", "application/json");
  const response = await fetch(`https://graph.facebook.com/v20.0/${cleanPath}`, {
    ...init,
    headers,
  });
  const body = await response.json();
  if (!response.ok) {
    throw new Error(JSON.stringify(body?.error || body));
  }
  return body;
}

async function tryGraphRequest(path: string) {
  try {
    return { body: await graphRequest(path) };
  } catch (error) {
    return { error: parseProviderError(error) };
  }
}

function graphDataList(value: any): any[] {
  if (Array.isArray(value)) return value;
  if (Array.isArray(value?.data)) return value.data;
  return [];
}

function extractWabaIdFromGraphPayload(payload: any, phoneNumberId: string) {
  const foundWabaIds = new Set<string>();
  const businesses = [
    ...graphDataList(payload),
    ...graphDataList(payload?.businesses),
  ];
  const directWabas = graphDataList(payload?.owned_whatsapp_business_accounts);

  for (const business of businesses) {
    const wabas = graphDataList(business?.owned_whatsapp_business_accounts);
    for (const waba of wabas) {
      const wabaId = String(waba?.id || "");
      if (wabaId) foundWabaIds.add(wabaId);
      const phoneNumbers = graphDataList(waba?.phone_numbers);
      if (
        phoneNumbers.some((phone) => String(phone?.id || "") === phoneNumberId)
      ) {
        return wabaId;
      }
    }
  }

  for (const waba of directWabas) {
    const wabaId = String(waba?.id || "");
    if (wabaId) foundWabaIds.add(wabaId);
    const phoneNumbers = graphDataList(waba?.phone_numbers);
    if (
      phoneNumbers.some((phone) => String(phone?.id || "") === phoneNumberId)
    ) {
      return wabaId;
    }
  }

  return foundWabaIds.size === 1 ? [...foundWabaIds][0] : "";
}

async function resolveWhatsappBusinessAccountId() {
  const configured = env("META_WHATSAPP_BUSINESS_ACCOUNT_ID") ||
    env("META_WABA_ID") ||
    env("WHATSAPP_BUSINESS_ACCOUNT_ID");
  if (configured) return configured;

  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  if (!phoneNumberId) {
    throw new Error("META_WHATSAPP_PHONE_NUMBER_ID is missing.");
  }
  const phoneInfo = await tryGraphRequest(
    `${encodeURIComponent(phoneNumberId)}?fields=whatsapp_business_account`,
  );
  const directWabaId = String(
    phoneInfo.body?.whatsapp_business_account?.id || "",
  );
  if (directWabaId) return directWabaId;

  const lookupPaths = [
    "me/businesses?fields=id,name,owned_whatsapp_business_accounts{id,name,phone_numbers{id,display_phone_number}}",
    "me?fields=id,name,businesses{id,name,owned_whatsapp_business_accounts{id,name,phone_numbers{id,display_phone_number}}},owned_whatsapp_business_accounts{id,name,phone_numbers{id,display_phone_number}}",
  ];
  const lookupErrors: Record<string, any>[] = [];
  for (const path of lookupPaths) {
    const result = await tryGraphRequest(path);
    if (result.error) {
      lookupErrors.push(result.error);
      continue;
    }
    const wabaId = extractWabaIdFromGraphPayload(result.body, phoneNumberId);
    if (wabaId) return wabaId;
  }

  throw new Error(JSON.stringify({
    message:
      "Unable to resolve WhatsApp Business Account ID. Set META_WHATSAPP_BUSINESS_ACCOUNT_ID.",
    phoneNumberLookupError: phoneInfo.error || null,
    businessLookupErrors: lookupErrors,
  }));
}

async function handleSetupDirectPaymentTemplate(request: Request) {
  await assertAuthenticatedOrServiceRole(request);

  const wabaId = await resolveWhatsappBusinessAccountId();
  const templateName = directPaymentTemplateName();
  const languageCode = env("META_WHATSAPP_TEMPLATE_LANGUAGE") || "en";
  const bodyText = env("META_WHATSAPP_DIRECT_PAY_TEMPLATE_BODY") ||
    "Hi {{1}}, your Gen Alpha Cricket Academy fee is due from {{2}}.\n\nTap Pay Now to choose 1/3/6 months and complete UPI payment. After payment, reply here with Paid or send the payment screenshot.";
  const templatePayload = {
    name: templateName,
    language: languageCode,
    category: "UTILITY",
    components: [
      {
        type: "BODY",
        text: bodyText,
        example: {
          body_text: [["Aarav", "11th July"]],
        },
      },
      {
        type: "BUTTONS",
        buttons: [
          {
            type: "URL",
            text: "Pay Now",
            url: `${PAYMENT_PAGE_URL}?e={{1}}`,
            example: [`${PAYMENT_PAGE_URL}?e=00000000-0000-0000-0000-000000000000`],
          },
          {
            type: "QUICK_REPLY",
            text: "Need Help",
          },
        ],
      },
    ],
  };

  try {
    const body = await graphRequest(`${encodeURIComponent(wabaId)}/message_templates`, {
      method: "POST",
      body: JSON.stringify(templatePayload),
    });
    return jsonResponse({
      success: true,
      templateName,
      languageCode,
      wabaId,
      metaResponse: body,
    });
  } catch (error) {
    const errorPayload = parseProviderError(error);
    const message = providerErrorMessage(errorPayload);
    if (/already exists|duplicate/i.test(JSON.stringify(errorPayload))) {
      return jsonResponse({
        success: true,
        templateName,
        languageCode,
        wabaId,
        alreadyExists: true,
        metaResponse: errorPayload,
      });
    }
    return jsonResponse({
      success: false,
      templateName,
      languageCode,
      wabaId,
      error: message,
      metaResponse: errorPayload,
    }, 502);
  }
}

function parseProviderError(input: unknown): Record<string, any> {
  if (!input) return {};
  if (input instanceof Error) {
    const message = input.message || "";
    try {
      const parsed = JSON.parse(message);
      return typeof parsed === "object" && parsed ? parsed : { message };
    } catch (_error) {
      return { message };
    }
  }
  if (typeof input === "string") {
    try {
      const parsed = JSON.parse(input);
      return typeof parsed === "object" && parsed ? parsed : { message: input };
    } catch (_error) {
      return { message: input };
    }
  }
  if (typeof input === "object") return input as Record<string, any>;
  return { message: String(input) };
}

function providerErrorCode(errorPayload: Record<string, any>): string {
  const candidates = [
    errorPayload?.code,
    errorPayload?.error?.code,
    errorPayload?.errors?.[0]?.code,
    errorPayload?.statuses?.[0]?.errors?.[0]?.code,
  ];
  const code = candidates.map((value) => String(value || "")).find(Boolean);
  if (code) return code;
  const text = JSON.stringify(errorPayload || {});
  return text.includes(HEALTHY_ECOSYSTEM_ERROR_CODE)
    ? HEALTHY_ECOSYSTEM_ERROR_CODE
    : "";
}

function providerErrorMessage(errorPayload: Record<string, any>): string {
  const candidates = [
    errorPayload?.message,
    errorPayload?.title,
    errorPayload?.error?.message,
    errorPayload?.error?.error_data?.details,
    errorPayload?.errors?.[0]?.message,
    errorPayload?.errors?.[0]?.title,
  ];
  return candidates.map((value) => String(value || "").trim()).find(Boolean) ||
    "Meta WhatsApp send failed.";
}

function isTemplateFallbackError(errorPayload: Record<string, any>): boolean {
  const code = providerErrorCode(errorPayload);
  const text = JSON.stringify(errorPayload || {}).toLowerCase();
  return code === "132001" ||
    (text.includes("template") &&
      (text.includes("does not exist") ||
        text.includes("not found") ||
        text.includes("paused") ||
        text.includes("disabled") ||
        text.includes("rejected") ||
        text.includes("not approved")));
}

function isHealthyEcosystemError(errorPayload: Record<string, any>): boolean {
  const code = providerErrorCode(errorPayload);
  const text = JSON.stringify(errorPayload || {}).toLowerCase();
  return code === HEALTHY_ECOSYSTEM_ERROR_CODE ||
    text.includes("healthy ecosystem engagement");
}

function retryDelayMinutes(retryCount: number): number {
  const index = Math.min(
    Math.max(0, retryCount),
    HEALTHY_ECOSYSTEM_RETRY_MINUTES.length - 1,
  );
  return HEALTHY_ECOSYSTEM_RETRY_MINUTES[index];
}

async function markReminderSendFailed(
  event: any,
  errorPayload: Record<string, any>,
  failedAt: string,
  createdBy: string,
  finalReason = "",
  manualFollowupReason = "delivery_failure",
) {
  const reason = finalReason || providerErrorMessage(errorPayload);
  await updateReminderEvent(event.id, {
    status: "send_failed",
    dry_run: false,
    meta_error: errorPayload,
    failed_at: failedAt,
    retry_count: Number(event?.retry_count || 0),
    next_retry_at: null,
    retry_reason: null,
    manual_followup_required: true,
    manual_followup_reason: manualFollowupReason,
  });
  await insertWhatsappFlowEvent({
    student_id: event.student_id,
    reminder_event_id: event.id,
    event_type: "reminder_send_failed",
    direction: "outbound",
    parent_phone: String(event.parent_phone || ""),
    message_kind: "template",
    message_body: event.message_preview || "",
    status: "send_failed",
    failed_at: failedAt,
    error_code: providerErrorCode(errorPayload),
    error_message: reason,
    provider_payload: errorPayload,
    created_by: createdBy,
  });
}

async function scheduleHealthyEcosystemRetry(
  event: any,
  errorPayload: Record<string, any>,
  failedAt: string,
  createdBy: string,
) {
  const student = await fetchStudent(String(event?.student_id || ""));
  if (isWhatsappContactBlocked(student)) {
    await markReminderContactBlocked(event, student);
    return {
      scheduled: false,
      manualFollowupRequired: true,
      reason: whatsappContactBlockedMessage(student),
    };
  }

  const retryCount = Number(event?.retry_count || 0);
  const maxRetryCount = Math.max(
    1,
    Number(event?.max_retry_count || DEFAULT_REMINDER_MAX_RETRIES),
  );

  if (retryCount >= maxRetryCount) {
    await markReminderSendFailed(
      event,
      errorPayload,
      failedAt,
      createdBy,
      "Meta kept this message blocked for healthy ecosystem engagement. Manual follow-up is required.",
      "retry_exhausted",
    );
    return {
      scheduled: false,
      manualFollowupRequired: true,
      reason: "Retry limit reached; manual follow-up required.",
    };
  }

  const delayMinutes = retryDelayMinutes(retryCount);
  const nextRetryAt = new Date(
    new Date(failedAt).getTime() + delayMinutes * 60 * 1000,
  ).toISOString();
  const retryReason =
    `Meta healthy ecosystem engagement limit. Retry ${retryCount + 1} of ${maxRetryCount} scheduled after ${delayMinutes} minutes.`;

  await updateReminderEvent(event.id, {
    status: "retry_scheduled",
    dry_run: false,
    meta_error: errorPayload,
    failed_at: failedAt,
    next_retry_at: nextRetryAt,
    retry_reason: retryReason,
    max_retry_count: maxRetryCount,
    manual_followup_required: false,
    manual_followup_reason: "",
  });
  await insertWhatsappFlowEvent({
    student_id: event.student_id,
    reminder_event_id: event.id,
    event_type: "reminder_retry_scheduled",
    direction: "system",
    parent_phone: String(event.parent_phone || ""),
    message_kind: "template",
    message_body: event.message_preview || "",
    status: "retry_scheduled",
    status_at: failedAt,
    failed_at: failedAt,
    error_code: providerErrorCode(errorPayload),
    error_message: retryReason,
    provider_payload: errorPayload,
    created_by: createdBy,
  });

  return {
    scheduled: true,
    nextRetryAt,
    reason: retryReason,
  };
}

async function recordReminderAccepted(
  event: any,
  metaResponse: any,
  createdBy: string,
  eventType = "reminder_message_status",
) {
  const whatsappMessageId = String(metaResponse?.messages?.[0]?.id || "");
  const acceptedAt = new Date().toISOString();
  await updateReminderEvent(event.id, {
    status: whatsappMessageId ? "accepted" : "sent",
    whatsapp_message_id: whatsappMessageId,
    meta_response: metaResponse,
    meta_error: {},
    failed_at: null,
    accepted_at: acceptedAt,
    next_retry_at: null,
    retry_reason: null,
    manual_followup_required: false,
    manual_followup_reason: "",
  });
  await insertWhatsappFlowEvent({
    student_id: event.student_id,
    reminder_event_id: event.id,
    event_type: eventType,
    direction: "outbound",
    parent_phone: String(event.parent_phone || ""),
    message_kind: "template",
    message_body: metaResponse?.local_message_body || event.message_preview || "",
    message_id: whatsappMessageId,
    status: whatsappMessageId ? "accepted" : "sent",
    status_at: acceptedAt,
    accepted_at: acceptedAt,
    provider_payload: metaResponse,
    created_by: createdBy,
  });
  return whatsappMessageId;
}

async function createAwaitingPaymentLinkRequest(
  student: any,
  reminderEvent: any,
  createdBy: string,
  dryRun = false,
) {
  return await insertPaymentLinkRequest({
    reminder_event_id: reminderEvent.id,
    student_id: reminderEvent.student_id,
    payment_type: reminderEvent.reminder_type === "joining_fee"
      ? "joining"
      : "renewal",
    plan_type: "awaiting_parent_choice",
    months_covered: 0,
    amount: 0,
    cycle_start_date: reminderEvent.due_date,
    provider: "upi",
    status: dryRun ? "dry_run" : "link_sent",
    dry_run: dryRun,
    payment_link_url: buildDirectPaymentPageUrl(reminderEvent.id),
    payment_link_id: "",
    created_by: createdBy,
  });
}

async function recordDirectPaymentLinkSent(
  event: any,
  student: any,
  linkRequest: any,
  createdBy: string,
  dryRun = false,
) {
  const sentAt = new Date().toISOString();
  const paymentPageUrl = buildDirectPaymentPageUrl(event.id);
  await updateReminderEvent(event.id, {
    payment_link_url: paymentPageUrl,
    payment_link_sent_at: sentAt,
  });
  await insertWhatsappFlowEvent({
    student_id: event.student_id,
    reminder_event_id: event.id,
    payment_link_request_id: linkRequest?.id || null,
    event_type: "payment_link_sent",
    direction: "outbound",
    parent_phone: String(event.parent_phone || ""),
    message_kind: "direct_payment_link",
    message_body: paymentPageUrl,
    status: dryRun ? "dry_run" : "sent",
    status_at: sentAt,
    sent_at: dryRun ? null : sentAt,
    payment_from_date: event.due_date || null,
    provider_payload: {
      player_name: student?.name || "",
      direct_payment_page_url: paymentPageUrl,
    },
    created_by: createdBy,
  });
}

async function sendReminderTemplate(
  to: string,
  event: any,
  student: any,
  dueDate: string,
  reminderType: string,
  settings: ReminderSettings,
  createdBy: string,
  eventType = "reminder_message_status",
) {
  if (isDirectPaymentTemplateEnabled(settings)) {
    try {
      const directResponse = await sendDirectPaymentTemplateMessage(
        to,
        event.id,
        student,
        dueDate,
        reminderType,
      );
      await recordReminderAccepted(
        event,
        directResponse,
        createdBy,
        eventType === "reminder_retry_sent"
          ? "direct_payment_reminder_retry_sent"
          : "direct_payment_reminder_sent",
      );
      const linkRequest = await createAwaitingPaymentLinkRequest(
        student,
        event,
        createdBy,
        false,
      );
      await recordDirectPaymentLinkSent(
        event,
        student,
        linkRequest,
        createdBy,
        false,
      );
      return {
        metaResponse: directResponse,
        directPayment: true,
        fallbackUsed: false,
      };
    } catch (error) {
      const errorPayload = parseProviderError(error);
      if (!isTemplateFallbackError(errorPayload)) {
        throw error;
      }
      await insertWhatsappFlowEvent({
        student_id: event.student_id,
        reminder_event_id: event.id,
        event_type: "direct_payment_template_fallback",
        direction: "system",
        parent_phone: String(event.parent_phone || ""),
        message_kind: "template",
        message_body: buildDirectPaymentMessageBody(
          student,
          dueDate,
          reminderType,
        ),
        status: "fallback_to_plan_buttons",
        status_at: new Date().toISOString(),
        error_code: providerErrorCode(errorPayload),
        error_message: providerErrorMessage(errorPayload),
        provider_payload: errorPayload,
        created_by: createdBy,
      });
    }
  }

  const metaResponse = await sendTemplateMessage(
    to,
    event.id,
    student,
    dueDate,
    reminderType,
  );
  await recordReminderAccepted(event, metaResponse, createdBy, eventType);
  return {
    metaResponse,
    directPayment: false,
    fallbackUsed: isDirectPaymentTemplateEnabled(settings),
  };
}

function dedupeById(rows: any[]): any[] {
  const seen = new Set<string>();
  return rows.filter((row) => {
    const id = String(row?.id || "");
    if (!id || seen.has(id)) return false;
    seen.add(id);
    return true;
  });
}

async function processDueReminderRetries(
  limit = RETRY_WORKER_LIMIT,
  settings?: ReminderSettings,
) {
  const reminderSettings = settings || await loadSettings();
  const nowIso = new Date().toISOString();
  const recoveryCutoffIso = new Date(
    Date.now() - RETRY_RECOVERY_MINUTES * 60 * 1000,
  ).toISOString();
  const failedRecoveryCutoffIso = new Date(
    `${localIsoDate()}T00:00:00+05:30`,
  ).toISOString();
  let events: any[] = [];
  try {
    const dueScheduled = await rest(
      `reminder_events?select=*&status=eq.retry_scheduled&next_retry_at=lte.${
        encodeURIComponent(nowIso)
      }&order=next_retry_at.asc&limit=${limit}`,
    );
    const scheduledWithoutDate = await rest(
      `reminder_events?select=*&status=eq.retry_scheduled&next_retry_at=is.null&order=created_at.asc&limit=${limit}`,
    );
    const interruptedQueuedRetries = await rest(
      `reminder_events?select=*&status=eq.queued&retry_count=gt.0&last_retry_at=lte.${
        encodeURIComponent(recoveryCutoffIso)
      }&order=last_retry_at.asc&limit=${limit}`,
    );
    const recentRetryableFailures = await rest(
      `reminder_events?select=*&status=in.(failed,send_failed,delivery_failed,undelivered)&failed_at=gte.${
        encodeURIComponent(failedRecoveryCutoffIso)
      }&delivered_at=is.null&read_at=is.null&order=failed_at.asc&limit=${limit}`,
    );
    const recoverableFailures = (recentRetryableFailures || []).filter((event: any) => {
      const retryCount = Number(event.retry_count || 0);
      const maxRetryCount = Math.max(
        1,
        Number(event.max_retry_count || DEFAULT_REMINDER_MAX_RETRIES),
      );
      if (retryCount >= maxRetryCount) return false;
      const errorPayload = parseProviderError(
        event.meta_error || { message: event.retry_reason || "" },
      );
      return isHealthyEcosystemError(errorPayload);
    });
    events = dedupeById([
      ...(dueScheduled || []),
      ...(scheduledWithoutDate || []),
      ...(interruptedQueuedRetries || []),
      ...recoverableFailures,
    ]).slice(0, limit);
  } catch (_error) {
    return [];
  }

  const results = [];
  for (const event of events || []) {
    const retryStartedAt = new Date().toISOString();
    const retryCount = Number(event.retry_count || 0) + 1;
    try {
      const student = await fetchStudent(String(event.student_id || ""));
      if (isWhatsappContactBlocked(student)) {
        await markReminderContactBlocked(event, student);
        results.push({
          student: student.name,
          status: "manual_followup_wrong_phone",
        });
        continue;
      }
      const parentPhone = String(
        event.parent_phone || student.parent_contact_no || "",
      ).replace(/\D/g, "").slice(-10);
      const to = normalizePhone(parentPhone);
      if (!to) {
        await markReminderSendFailed(
          { ...event, parent_phone: parentPhone, retry_count: retryCount },
          { message: "Parent phone number is missing." },
          retryStartedAt,
          "system_retry",
          "",
          "missing_phone",
        );
        results.push({ student: student.name, status: "failed_missing_phone" });
        continue;
      }

      await updateReminderEvent(event.id, {
        status: "queued",
        retry_count: retryCount,
        last_retry_at: retryStartedAt,
        next_retry_at: null,
        retry_reason: `Retry attempt ${retryCount} of ${
          Math.max(1, Number(event.max_retry_count || DEFAULT_REMINDER_MAX_RETRIES))
        } started.`,
      });
      const refreshedEvent = {
        ...event,
        parent_phone: parentPhone,
        retry_count: retryCount,
      };
      const sent = await sendReminderTemplate(
        to,
        refreshedEvent,
        student,
        String(event.due_date || localIsoDate()),
        String(event.reminder_type || "renewal"),
        reminderSettings,
        "system_retry",
        "reminder_retry_sent",
      );
      results.push({
        student: student.name,
        status: sent.directPayment ? "direct_payment_retry_sent" : "retry_sent",
        retryCount,
      });
    } catch (error) {
      const errorPayload = parseProviderError(error);
      const retryEvent = { ...event, retry_count: retryCount };
      if (isHealthyEcosystemError(errorPayload)) {
        const scheduled = await scheduleHealthyEcosystemRetry(
          retryEvent,
          errorPayload,
          retryStartedAt,
          "system_retry",
        );
        results.push({
          studentId: event.student_id,
          status: scheduled.scheduled ? "retry_scheduled" : "send_failed",
          retryCount,
          nextRetryAt: scheduled.nextRetryAt || null,
          reason: scheduled.reason || null,
        });
      } else {
        await markReminderSendFailed(
          retryEvent,
          errorPayload,
          retryStartedAt,
          "system_retry",
        );
        results.push({
          studentId: event.student_id,
          status: "send_failed",
          error: providerErrorMessage(errorPayload),
        });
      }
    }
    await new Promise((resolve) => setTimeout(resolve, 350));
  }
  return results;
}

async function sendTextMessage(to: string, text: string) {
  const token = env("META_WHATSAPP_TOKEN");
  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  if (!token || !phoneNumberId) return null;

  const response = await fetch(
    `https://graph.facebook.com/v20.0/${phoneNumberId}/messages`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messaging_product: "whatsapp",
        to,
        type: "text",
        text: { preview_url: true, body: text },
      }),
    },
  );
  return await response.json();
}

async function sendMediaMessage(
  to: string,
  mediaKind: "image" | "document",
  link: string,
  caption: string,
  filename = "payment-proof",
) {
  const token = env("META_WHATSAPP_TOKEN");
  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  if (!token || !phoneNumberId || !link) return null;

  const mediaPayload = mediaKind === "document"
    ? { link, caption, filename }
    : { link, caption };
  const response = await fetch(
    `https://graph.facebook.com/v20.0/${phoneNumberId}/messages`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messaging_product: "whatsapp",
        to,
        type: mediaKind,
        [mediaKind]: mediaPayload,
      }),
    },
  );
  return await response.json();
}

function mediaIdFromMessage(message: any): string {
  return String(message?.image?.id || message?.document?.id || "");
}

function extensionForMime(mimeType: string): string {
  if (mimeType.includes("png")) return "png";
  if (mimeType.includes("webp")) return "webp";
  if (mimeType.includes("pdf")) return "pdf";
  return "jpg";
}

async function ensurePaymentProofBucket(bucket: string) {
  const baseUrl = env("SUPABASE_URL").replace(/\/+$/, "");
  const response = await fetch(`${baseUrl}/storage/v1/bucket`, {
    method: "POST",
    headers: serviceHeaders(),
    body: JSON.stringify({
      id: bucket,
      name: bucket,
      public: false,
      file_size_limit: 10485760,
    }),
  });
  if (response.ok || response.status === 409) return;

  const text = await response.text();
  if (/already exists|duplicate/i.test(text)) return;
  throw new Error(text || "Unable to create payment proof storage bucket.");
}

async function storePaymentProofMedia(message: any, reminderEvent: any) {
  const mediaId = mediaIdFromMessage(message);
  if (!mediaId) return null;

  const token = env("META_WHATSAPP_TOKEN");
  if (!token) {
    return {
      media_id: mediaId,
      storage_error: "META_WHATSAPP_TOKEN is missing.",
    };
  }

  try {
    const metadataResponse = await fetch(
      `https://graph.facebook.com/v20.0/${encodeURIComponent(mediaId)}`,
      { headers: { Authorization: `Bearer ${token}` } },
    );
    const metadata = await metadataResponse.json();
    if (!metadataResponse.ok) {
      throw new Error(JSON.stringify(metadata?.error || metadata));
    }

    const mediaResponse = await fetch(metadata.url, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!mediaResponse.ok) {
      throw new Error(await mediaResponse.text() || "Unable to download WhatsApp media.");
    }

    const mimeType = String(metadata.mime_type || "image/jpeg");
    const bucket = "payment-proofs";
    const safeMessageId = String(message.id || mediaId).replace(/[^a-zA-Z0-9_-]/g, "");
    const safeReminderId = String(reminderEvent.id || "unknown").replace(/[^a-zA-Z0-9_-]/g, "");
    const objectPath = `${safeReminderId}/${safeMessageId}.${extensionForMime(mimeType)}`;
    await ensurePaymentProofBucket(bucket);

    const uploadResponse = await fetch(
      `${env("SUPABASE_URL").replace(/\/+$/, "")}/storage/v1/object/${bucket}/${objectPath}`,
      {
        method: "POST",
        headers: {
          ...serviceHeaders(),
          "Content-Type": mimeType,
          "x-upsert": "true",
        },
        body: await mediaResponse.arrayBuffer(),
      },
    );
    if (!uploadResponse.ok) {
      throw new Error(await uploadResponse.text() || "Unable to store payment proof.");
    }

    return {
      media_id: mediaId,
      mime_type: mimeType,
      sha256: metadata.sha256 || "",
      file_size: metadata.file_size || null,
      storage_bucket: bucket,
      storage_path: objectPath,
      stored_at: new Date().toISOString(),
    };
  } catch (error) {
    return {
      media_id: mediaId,
      storage_error: error instanceof Error ? error.message : String(error),
    };
  }
}

async function createPaymentProofSignedUrl(bucket: string, path: string) {
  if (!bucket || !path) return "";
  const baseUrl = env("SUPABASE_URL").replace(/\/+$/, "");
  try {
    const response = await fetch(
      `${baseUrl}/storage/v1/object/sign/${encodeURIComponent(bucket)}/${path}`,
      {
        method: "POST",
        headers: serviceHeaders(),
        body: JSON.stringify({ expiresIn: 60 * 60 }),
      },
    );
    const body = await response.json();
    const signedUrl = body?.signedURL || body?.signedUrl || "";
    if (!response.ok || !signedUrl) return "";
    return String(signedUrl).startsWith("http")
      ? String(signedUrl)
      : `${baseUrl}/storage/v1${signedUrl}`;
  } catch (_error) {
    return "";
  }
}

async function createUpiPaymentLink(
  student: any,
  plan: string,
  reminderEvent: any,
  settings: ReminderSettings,
) {
  const dryRun = settings.dryRunMode || !settings.paymentLinksEnabled;
  const isJoining = reminderEvent.reminder_type === "joining_fee";
  const amount = paymentAmountForReminderType(
    String(reminderEvent.reminder_type || "renewal"),
    plan,
  );
  const months = PLAN_MONTHS[plan] || 0;

  if (dryRun) {
    return await insertPaymentLinkRequest({
      reminder_event_id: reminderEvent.id,
      student_id: reminderEvent.student_id,
      payment_type: reminderEvent.reminder_type === "joining_fee"
        ? "joining"
        : "renewal",
      plan_type: plan,
      months_covered: months,
      amount,
      cycle_start_date: reminderEvent.due_date,
      provider: "upi",
      status: "dry_run",
      dry_run: true,
      created_by: "whatsapp-webhook",
    });
  }

  const upiLink = buildUpiLink(student, plan, amount);
  const paymentPageUrl = buildPaymentPageUrl(
    student,
    plan,
    amount,
    reminderEvent.id,
  );

  return await insertPaymentLinkRequest({
    reminder_event_id: reminderEvent.id,
    student_id: reminderEvent.student_id,
    payment_type: reminderEvent.reminder_type === "joining_fee"
      ? "joining"
      : "renewal",
    plan_type: plan,
    months_covered: months,
    amount,
    cycle_start_date: reminderEvent.due_date,
    provider: "upi",
    status: "created",
    dry_run: false,
    payment_link_url: paymentPageUrl,
    payment_link_id: upiLink,
    created_by: "whatsapp-webhook",
  });
}

async function sendManagerPaymentAlert(
  reminderEvent: any,
  options: { proofMedia?: any; forceWithProof?: boolean; forceWithoutProof?: boolean; source?: string } = {},
) {
  if (!reminderEvent?.id) return { sent: false, reason: "missing reminder event" };
  const currentAlertStatus = String(reminderEvent.manager_payment_alert_status || "");
  const proofMedia = options.proofMedia || reminderEvent?.meta_response?.payment_confirmation?.proof_media || null;
  const proofPath = String(proofMedia?.storage_path || "");
  const proofBucket = String(proofMedia?.storage_bucket || "payment-proofs");
  const hasProof = Boolean(proofPath);
  const proofWasSubmitted = hasProof || options.forceWithProof === true;

  if (proofWasSubmitted && currentAlertStatus === "sent_with_proof") {
    return { sent: false, reason: "manager already alerted with proof" };
  }
  if (!proofWasSubmitted && currentAlertStatus === "sent_without_proof") {
    return { sent: false, reason: "manager already alerted without proof" };
  }
  if (!proofWasSubmitted && !options.forceWithoutProof && currentAlertStatus !== "scheduled") {
    return { sent: false, reason: "manager alert not due" };
  }

  const student = reminderEvent.student_id
    ? await fetchStudent(String(reminderEvent.student_id))
    : { name: "Unknown player" };
  const to = normalizePhone(MANAGER_PAYMENT_ALERT_PHONE);
  const playerName = String(student?.name || "Unknown player");
  const sentAt = new Date().toISOString();
  let proofSignedUrl = "";
  const bodyComponent = {
    type: "body",
    parameters: [
      { type: "text", text: playerName },
    ],
  };

  if (proofWasSubmitted && hasProof) {
    proofSignedUrl = await createPaymentProofSignedUrl(proofBucket, proofPath);
  }
  const shouldUseProofTemplate = proofWasSubmitted && Boolean(proofSignedUrl);
  const templateName = shouldUseProofTemplate
    ? (env("META_MANAGER_PAYMENT_ALERT_WITH_PROOF_TEMPLATE_NAME") || "manager_payment_alert_with_proof")
    : (env("META_MANAGER_PAYMENT_ALERT_TEMPLATE_NAME") || "manager_payment_alert");
  const components = shouldUseProofTemplate
    ? [
      {
        type: "header",
        parameters: [
          {
            type: "image",
            image: { link: proofSignedUrl },
          },
        ],
      },
      bodyComponent,
    ]
    : [bodyComponent];

  const templateResponse = await sendTemplatePayload(to, templateName, components);

  await updateReminderEvent(reminderEvent.id, {
    manager_payment_alert_status: proofWasSubmitted ? "sent_with_proof" : "sent_without_proof",
    manager_payment_alert_sent_at: sentAt,
    manager_payment_alert_error: null,
    manager_payment_alert_meta_response: {
      source: options.source || "system",
      template_name: templateName,
      proof_path: proofPath,
      template_response: templateResponse,
      proof_signed_url_created: Boolean(proofSignedUrl),
      sent_at: sentAt,
    },
  });
  await insertWhatsappFlowEvent({
    student_id: reminderEvent.student_id,
    reminder_event_id: reminderEvent.id,
    event_type: proofWasSubmitted
      ? "manager_payment_alert_with_proof_sent"
      : "manager_payment_alert_without_proof_sent",
    direction: "outbound",
    parent_phone: MANAGER_PAYMENT_ALERT_PHONE,
    message_kind: proofWasSubmitted ? "manager_alert_with_proof_template" : "manager_alert_template",
    message_body: `${templateName}: ${playerName}`,
    message_id: String(templateResponse?.messages?.[0]?.id || ""),
    status: String(templateResponse?.messages?.[0]?.id || "")
      ? "accepted"
      : "sent",
    status_at: sentAt,
    sent_at: sentAt,
    proof_bucket: proofBucket,
    proof_path: proofPath,
    provider_payload: {
      template_name: templateName,
      template_response: templateResponse,
      proof_signed_url_created: Boolean(proofSignedUrl),
    },
    created_by: options.source || "system",
  });

  return { sent: true, hasProof, templateName, templateResponse };
}

async function processDueManagerPaymentAlerts(limit = 20) {
  const nowIso = new Date().toISOString();
  let events: any[] = [];
  try {
    events = await rest(
      `reminder_events?select=*&manager_payment_alert_status=eq.scheduled&manager_payment_alert_due_at=lte.${
        encodeURIComponent(nowIso)
      }&order=manager_payment_alert_due_at.asc&limit=${limit}`,
    );
  } catch (_error) {
    return [];
  }

  const results = [];
  for (const event of events || []) {
    try {
      const sent = await sendManagerPaymentAlert(event, { source: "system_cron" });
      results.push({
        reminderEventId: event.id,
        status: sent.sent ? "sent_without_proof" : "skipped",
        reason: sent.reason || "",
      });
    } catch (error) {
      await updateReminderEvent(event.id, {
        manager_payment_alert_status: "failed",
        manager_payment_alert_error: parseProviderError(error),
      });
      results.push({
        reminderEventId: event.id,
        status: "failed",
        error: error instanceof Error ? error.message : String(error),
      });
    }
  }
  return results;
}

async function handlePaymentOptions(payload: any) {
  const eventId = String(payload.eventId || payload.event_id || payload.reminderEventId || "");
  if (!eventId) return jsonResponse({ error: "eventId is required." }, 400);

  const reminderEvent = await findReminderEvent(eventId);
  if (!reminderEvent) {
    return jsonResponse({ error: "Reminder event not found." }, 404);
  }
  const student = await fetchStudent(String(reminderEvent.student_id || ""));
  return jsonResponse({
    success: true,
    eventId: reminderEvent.id,
    playerName: String(student?.name || "Player"),
    dueDate: reminderEvent.due_date || "",
    reminderType: reminderEvent.reminder_type || "renewal",
    defaultPlan: String(reminderEvent.selected_plan || "") || "monthly",
    selectedPlan: String(reminderEvent.selected_plan || ""),
    selectedAmount: Number(reminderEvent.amount || 0) || null,
    options: buildPaymentOptions(reminderEvent),
  });
}

async function latestPaymentLinkRequest(reminderEventId: string) {
  const rows = await rest(
    `payment_link_requests?select=*&reminder_event_id=eq.${
      encodeURIComponent(reminderEventId)
    }&order=created_at.desc&limit=1`,
  );
  return rows?.[0] || null;
}

async function savePaymentPlanSelection(
  reminderEvent: any,
  plan: string,
  source: string,
) {
  if (!PAID_PLAN_OPTIONS.includes(plan)) return reminderEvent;

  const student = await fetchStudent(String(reminderEvent.student_id || ""));
  const amount = paymentAmountForReminderType(
    String(reminderEvent.reminder_type || "renewal"),
    plan,
  );
  const months = PLAN_MONTHS[plan] || 0;
  const paymentPageUrl = buildPaymentPageUrl(
    student,
    plan,
    amount,
    reminderEvent.id,
  );
  const upiLink = buildUpiLink(student, plan, amount);
  const selectedAt = new Date().toISOString();
  const existingLink = await latestPaymentLinkRequest(reminderEvent.id);
  let paymentLinkRequestId = existingLink?.id || null;

  if (existingLink?.id) {
    await rest(`payment_link_requests?id=eq.${encodeURIComponent(existingLink.id)}`, {
      method: "PATCH",
      body: JSON.stringify({
        plan_type: plan,
        months_covered: months,
        amount,
        provider: "upi",
        status: "payment_link_sent",
        dry_run: false,
        payment_link_url: paymentPageUrl,
        payment_link_id: upiLink,
        payment_link_sent_at: existingLink.payment_link_sent_at || selectedAt,
      }),
    });
  } else {
    const linkRequest = await insertPaymentLinkRequest({
      reminder_event_id: reminderEvent.id,
      student_id: reminderEvent.student_id,
      payment_type: reminderEvent.reminder_type === "joining_fee"
        ? "joining"
        : "renewal",
      plan_type: plan,
      months_covered: months,
      amount,
      cycle_start_date: reminderEvent.due_date,
      provider: "upi",
      status: "payment_link_sent",
      dry_run: false,
      payment_link_url: paymentPageUrl,
      payment_link_id: upiLink,
      payment_link_sent_at: selectedAt,
      created_by: source,
    });
    paymentLinkRequestId = linkRequest?.id || null;
  }

  await updateReminderEvent(reminderEvent.id, {
    selected_plan: plan,
    amount,
    payment_link_url: paymentPageUrl,
    payment_link_id: upiLink,
    payment_link_sent_at: reminderEvent.payment_link_sent_at || selectedAt,
  });
  await insertWhatsappFlowEvent({
    student_id: reminderEvent.student_id,
    reminder_event_id: reminderEvent.id,
    payment_link_request_id: paymentLinkRequestId,
    event_type: "parent_plan_selected",
    direction: "parent",
    parent_phone: String(reminderEvent.parent_phone || ""),
    message_kind: "payment_page_plan",
    message_body: PLAN_LABELS[plan],
    status: "plan_selected",
    status_at: selectedAt,
    payment_plan: plan,
    payment_amount: amount,
    payment_months: months,
    payment_from_date: reminderEvent.due_date || null,
    provider_payload: {
      source,
      payment_page_url: paymentPageUrl,
    },
    created_by: source,
  });

  return {
    ...reminderEvent,
    selected_plan: plan,
    amount,
    payment_link_url: paymentPageUrl,
    payment_link_id: upiLink,
    payment_link_sent_at: reminderEvent.payment_link_sent_at || selectedAt,
  };
}

async function handlePaymentAttempted(payload: any) {
  const eventId = String(
    payload.eventId || payload.event_id || payload.reminderEventId || "",
  );
  if (!eventId) return jsonResponse({ error: "eventId is required." }, 400);

  let reminderEvent = await findReminderEvent(eventId);
  if (!reminderEvent) {
    return jsonResponse({ error: "Reminder event not found." }, 404);
  }

  const selectedPlan = normalizeSelectedPlan(
    payload.selectedPlan || payload.plan || payload.planType,
  );
  if (selectedPlan && selectedPlan !== "need_help") {
    reminderEvent = await savePaymentPlanSelection(
      reminderEvent,
      selectedPlan,
      "pay.html",
    );
  }

  const to = normalizePhone(String(reminderEvent.parent_phone || ""));
  if (!to) {
    return jsonResponse({ error: "Parent phone number is missing." }, 400);
  }

  const shouldSendFollowup = ![
    "payment_attempted",
    "payment_pending_verification",
    "payment_confirmed",
  ].includes(String(reminderEvent.status || ""));
  const managerAlertStatus = String(reminderEvent.manager_payment_alert_status || "");
  const shouldScheduleManagerAlert = ![
    "sent_with_proof",
    "sent_without_proof",
  ].includes(managerAlertStatus);

  await updateReminderEvent(eventId, {
    status: "payment_attempted",
    payment_attempted_at: new Date().toISOString(),
    manager_payment_alert_status: shouldScheduleManagerAlert
      ? "scheduled"
      : managerAlertStatus,
    manager_payment_alert_due_at: shouldScheduleManagerAlert
      ? new Date(Date.now() + MANAGER_PAYMENT_ALERT_DELAY_MINUTES * 60 * 1000).toISOString()
      : reminderEvent.manager_payment_alert_due_at || null,
  });
  await updateLatestPaymentLinkRequest(eventId, {
    status: "payment_attempted",
    payment_attempted_at: new Date().toISOString(),
  });
  await insertWhatsappFlowEvent({
    student_id: reminderEvent.student_id,
    reminder_event_id: reminderEvent.id,
    event_type: "payment_attempted",
    direction: "parent",
    parent_phone: String(reminderEvent.parent_phone || ""),
    message_kind: "pay_now_click",
    status: "payment_attempted",
    status_at: new Date().toISOString(),
    payment_plan: String(reminderEvent.selected_plan || ""),
    payment_amount: Number(reminderEvent.amount || 0) || null,
    payment_from_date: reminderEvent.due_date || null,
    created_by: "pay.html",
  });

  let metaResponse = null;
  if (shouldSendFollowup) {
    metaResponse = await sendTextMessage(to, AFTER_PAY_NOW_FOLLOWUP);
    await insertWhatsappFlowEvent({
      student_id: reminderEvent.student_id,
      reminder_event_id: reminderEvent.id,
      event_type: "payment_followup_message_sent",
      direction: "outbound",
      parent_phone: String(reminderEvent.parent_phone || ""),
      message_kind: "text",
      message_body: AFTER_PAY_NOW_FOLLOWUP,
      message_id: String(metaResponse?.messages?.[0]?.id || ""),
      status: String(metaResponse?.messages?.[0]?.id || "") ? "accepted" : "sent",
      status_at: new Date().toISOString(),
      sent_at: new Date().toISOString(),
      provider_payload: metaResponse,
      created_by: "pay.html",
    });
  }

  return jsonResponse({
    success: true,
    message: shouldSendFollowup
      ? "Payment follow-up sent."
      : "Payment attempt already tracked.",
    metaResponse,
  });
}

function isPaymentConfirmationMessage(message: any): boolean {
  const type = String(message?.type || "");
  if (["image", "document"].includes(type)) return true;

  const text = normalizeChoiceText(message?.text?.body);
  return [
    "paid",
    "payed",
    "done",
    "paymentdone",
    "paiddone",
    "sent",
    "paymentsent",
    "screenshot",
    "screenshotsent",
  ].includes(text);
}

async function handlePaymentConfirmationMessage(
  from: string,
  message: any,
  webhookLog: any,
) {
  const reminderEvent = await findLatestReminderByPhone(from);
  if (!reminderEvent) {
    await insertWhatsappFlowEvent({
      event_type: "payment_pending_verification",
      direction: "parent",
      parent_phone: from.slice(-10),
      message_kind: String(message?.type || ""),
      message_body: String(message?.text?.body || message?.image?.caption || message?.document?.caption || ""),
      message_id: String(message.id || ""),
      status: "no_matching_reminder",
      status_at: new Date().toISOString(),
      error_message: "Payment confirmation received but no matching reminder event found.",
      provider_payload: message,
      created_by: "WhatsApp",
    });
    if (webhookLog?.id) {
      await rest(
        `whatsapp_webhook_events?id=eq.${encodeURIComponent(webhookLog.id)}`,
        {
          method: "PATCH",
          body: JSON.stringify({
            processing_note:
              "Payment confirmation received but no matching reminder event found.",
          }),
        },
      );
    }
    return;
  }

  const proofMedia = await storePaymentProofMedia(message, reminderEvent);
  const paymentConfirmation = {
    message_id: String(message.id || ""),
    type: String(message.type || ""),
    text: String(message?.text?.body || ""),
    image_id: String(message?.image?.id || ""),
    document_id: String(message?.document?.id || ""),
    caption: String(message?.image?.caption || message?.document?.caption || ""),
    proof_media: proofMedia,
    received_at: new Date().toISOString(),
  };

  await updateReminderEvent(reminderEvent.id, {
    status: "payment_pending_verification",
    payment_pending_verification_at: new Date().toISOString(),
    meta_response: {
      ...(reminderEvent.meta_response || {}),
      payment_confirmation: paymentConfirmation,
    },
  });
  await updateLatestPaymentLinkRequest(reminderEvent.id, {
    status: "payment_pending_verification",
    payment_pending_verification_at: new Date().toISOString(),
  });
  const proofPath = String(proofMedia?.storage_path || "");
  const auditRow = await insertWhatsappFlowEvent({
    student_id: reminderEvent.student_id,
    event_type: "payment_pending_verification",
    reminder_event_id: reminderEvent.id,
    direction: "parent",
    parent_phone: from.slice(-10),
    message_kind: paymentConfirmation.type,
    message_body: paymentConfirmation.text || paymentConfirmation.caption,
    message_id: paymentConfirmation.message_id,
    status: "payment_pending_verification",
    status_at: new Date().toISOString(),
    payment_plan: String(reminderEvent.selected_plan || ""),
    payment_amount: Number(reminderEvent.amount || 0) || null,
    payment_from_date: reminderEvent.due_date || null,
    proof_bucket: String(proofMedia?.storage_bucket || ""),
    proof_path: proofPath,
    provider_payload: paymentConfirmation,
    created_by: "WhatsApp",
  });
  if (!auditRow) {
    await insertStudentTimelineEvent({
      student_id: reminderEvent.student_id,
      event_type: "payment_pending_verification",
      event_date: new Date().toISOString().slice(0, 10),
      title: "Parent payment proof received",
      details: proofPath
        ? `Parent replied with ${paymentConfirmation.type}. Proof stored at ${paymentConfirmation.proof_media?.storage_bucket || "payment-proofs"}/${proofPath}.`
      : `Parent replied with ${paymentConfirmation.type || "message"}${
        paymentConfirmation.text ? `: ${paymentConfirmation.text}` : ""
      }.`,
      changed_by: "WhatsApp",
    });
  }

  await sendManagerPaymentAlert(
    {
      ...reminderEvent,
      meta_response: {
        ...(reminderEvent.meta_response || {}),
        payment_confirmation: paymentConfirmation,
      },
    },
    {
      proofMedia,
      forceWithProof: Boolean(proofPath),
      forceWithoutProof: !proofPath,
      source: "WhatsApp",
    },
  );

  if (webhookLog?.id) {
    await rest(
      `whatsapp_webhook_events?id=eq.${encodeURIComponent(webhookLog.id)}`,
      {
        method: "PATCH",
        body: JSON.stringify({
          reminder_event_id: reminderEvent.id,
          processed: true,
          processing_note: "Payment confirmation marked pending verification.",
        }),
      },
    );
  }

  const replyResponse = await sendTextMessage(from, PAYMENT_CONFIRMATION_REPLY);
  await insertWhatsappFlowEvent({
    student_id: reminderEvent.student_id,
    reminder_event_id: reminderEvent.id,
    event_type: "payment_verification_reply_sent",
    direction: "outbound",
    parent_phone: from.slice(-10),
    message_kind: "text",
    message_body: PAYMENT_CONFIRMATION_REPLY,
    message_id: String(replyResponse?.messages?.[0]?.id || ""),
    status: String(replyResponse?.messages?.[0]?.id || "") ? "accepted" : "sent",
    status_at: new Date().toISOString(),
    sent_at: new Date().toISOString(),
    provider_payload: replyResponse,
    created_by: "WhatsApp",
  });
}

async function handleSendReminder(request: Request, payload: any) {
  const managerEmail = await assertAuthenticated(request);
  const { studentId, dueDate: inputDueDate, reminderType: inputType } = payload;
  if (!studentId) return jsonResponse({ error: "studentId is required." }, 400);

  const settings = await loadSettings();
  const student = await fetchStudent(String(studentId));
  if (isWhatsappContactBlocked(student)) {
    return jsonResponse({
      success: false,
      status: "manual_followup",
      reason: String(student.whatsapp_contact_status || "wrong_number"),
      error: `${whatsappContactBlockedMessage(student)} Update the player contact before sending another reminder.`,
    }, 409);
  }
  const reminderType = String(
    inputType || (student.fees_paid ? "renewal" : "joining_fee"),
  );
  const dueDate = String(
    inputDueDate ||
      (reminderType === "joining_fee" ? student.join_date : localIsoDate()),
  );
  const overdueDays = Math.max(0, getDaysSinceDate(dueDate));
  const dryRun = settings.dryRunMode || !settings.whatsappRemindersEnabled;
  const parentPhone = String(student.parent_contact_no || "").replace(/\D/g, "")
    .slice(-10);
  const event = await insertReminderEvent({
    student_id: student.id,
    reminder_type: reminderType,
    channel: "whatsapp",
    status: dryRun ? "dry_run" : "queued",
    dry_run: dryRun,
    due_date: dueDate,
    overdue_days: overdueDays,
    plan_options: PLAN_OPTIONS,
    parent_phone: parentPhone,
    manager_phone: settings.managerPhone,
    message_preview: buildReminderPreview(
      student,
      dueDate,
      settings,
      reminderType,
    ),
    created_by: managerEmail,
  });
  await insertWhatsappFlowEvent({
    student_id: student.id,
    reminder_event_id: event.id,
    event_type: "reminder_created",
    direction: "outbound",
    parent_phone: parentPhone,
    message_kind: "template",
    message_body: event.message_preview || "",
    status: dryRun ? "dry_run" : "queued",
    status_at: new Date().toISOString(),
    payment_from_date: dueDate,
    created_by: managerEmail,
  });

  if (dryRun) {
    const dryRunLink = await insertPaymentLinkRequest({
      reminder_event_id: event.id,
      student_id: student.id,
      payment_type: reminderType === "joining_fee" ? "joining" : "renewal",
      plan_type: "awaiting_parent_choice",
      months_covered: 0,
      amount: 0,
      cycle_start_date: dueDate,
      provider: "upi",
      status: "dry_run",
      dry_run: true,
      created_by: managerEmail,
    });
    await insertWhatsappFlowEvent({
      student_id: student.id,
      reminder_event_id: event.id,
      payment_link_request_id: dryRunLink?.id || null,
      event_type: "payment_link_sent",
      direction: "system",
      parent_phone: parentPhone,
      message_kind: "payment_link",
      status: "dry_run",
      status_at: new Date().toISOString(),
      payment_from_date: dueDate,
      created_by: managerEmail,
    });
    return jsonResponse({
      success: true,
      dryRun: true,
      message: `Dry-run reminder logged for ${student.name}.`,
    });
  }

  const to = normalizePhone(parentPhone);
  if (!to) {
    await markReminderSendFailed(
      event,
      { message: "Parent phone number is missing." },
      new Date().toISOString(),
      managerEmail,
    );
    return jsonResponse({ error: "Parent phone number is missing." }, 400);
  }
  let sendResult;
  try {
    sendResult = await sendReminderTemplate(
      to,
      event,
      student,
      dueDate,
      reminderType,
      settings,
      managerEmail,
    );
  } catch (error) {
    const errorPayload = parseProviderError(error);
    const failedAt = new Date().toISOString();
    if (isHealthyEcosystemError(errorPayload)) {
      const retry = await scheduleHealthyEcosystemRetry(
        event,
        errorPayload,
        failedAt,
        managerEmail,
      );
      return jsonResponse({
        success: true,
        source: "meta_whatsapp",
        status: retry.scheduled ? "retry_scheduled" : "send_failed",
        nextRetryAt: retry.nextRetryAt || null,
        message: retry.scheduled
          ? `Meta limited this reminder. Retry scheduled for ${retry.nextRetryAt}.`
          : "Meta limited this reminder repeatedly. Manual follow-up is required.",
      }, retry.scheduled ? 202 : 502);
    }
    await markReminderSendFailed(event, errorPayload, failedAt, managerEmail);
    const message = providerErrorMessage(errorPayload);
    return jsonResponse({
      success: false,
      source: "meta_whatsapp",
      error: `Meta WhatsApp send failed: ${message}`,
    }, 502);
  }
  return jsonResponse({
    success: true,
    dryRun: false,
    directPayment: Boolean(sendResult?.directPayment),
    fallbackUsed: Boolean(sendResult?.fallbackUsed),
    message: `WhatsApp reminder sent for ${student.name}.`,
    metaResponse: sendResult?.metaResponse,
  });
}

async function handleSendSampleReminder(request: Request, payload: any) {
  const sampleToken = env("SAMPLE_REMINDER_TOKEN");
  const requestSampleToken = request.headers.get("x-sample-reminder-token") ||
    "";
  if (sampleToken && requestSampleToken === sampleToken) {
    // One-off operational sample sends can use this private token because the
    // WhatsApp webhook itself must remain publicly reachable by Meta.
  } else {
    await assertAuthenticated(request);
  }

  const to = normalizePhone(String(payload.phone || payload.to || ""));
  if (!to) return jsonResponse({ error: "Phone number is required." }, 400);

  const settings = await loadSettings();
  if (!settings.whatsappRemindersEnabled) {
    return jsonResponse({ error: "WhatsApp reminders are disabled." }, 400);
  }

  const sampleStudent = {
    name: String(payload.name || "Parent"),
  };
  const dueDate = String(payload.dueDate || localIsoDate());
  const reminderType = String(payload.reminderType || "renewal");
  const sampleFlow = normalizeSampleReminderFlow(
    payload.flow || payload.version || payload.mode,
    payload.action,
  );
  const sampleEventId = crypto.randomUUID();
  let metaResponse;
  let paymentPageUrl = "";
  let templateName = "";

  try {
    if (sampleFlow === "direct_pay") {
      templateName = directPaymentTemplateName();
      metaResponse = await sendDirectPaymentTemplateMessage(
        to,
        sampleEventId,
        sampleStudent,
        dueDate,
        reminderType,
      );
      paymentPageUrl = buildDirectPaymentPageUrl(sampleEventId);
    } else {
      templateName = reminderTemplateName(reminderType);
      metaResponse = await sendTemplateMessage(
        to,
        sampleEventId,
        sampleStudent,
        dueDate,
        reminderType,
      );
      const amount = paymentAmountForReminderType(reminderType, "monthly");
      paymentPageUrl = buildPaymentPageUrl(sampleStudent, "monthly", amount);
      await sendTextMessage(
        to,
        `Sample Gen Alpha fee link: ${paymentPageUrl}\n\n${paymentContactDetails()}`,
      );
    }
  } catch (error) {
    const message = error instanceof Error
      ? error.message
      : "Meta WhatsApp send failed.";
    return jsonResponse({
      success: false,
      source: "meta_whatsapp",
      error: `Meta WhatsApp send failed: ${message}`,
    }, 502);
  }

  return jsonResponse({
    success: true,
    flow: sampleFlow,
    sampleEventId,
    templateName,
    message: `${sampleFlow === "direct_pay" ? "V2 Direct Pay" : "Legacy"} sample WhatsApp reminder sent to ${to.slice(-10)}.`,
    metaResponse,
    paymentPageUrl,
  });
}

async function handleRenewalVerified(request: Request, payload: any) {
  const managerEmail = await assertAuthenticated(request);
  const studentId = String(payload.studentId || "");
  if (!studentId) return jsonResponse({ error: "studentId is required." }, 400);

  const student = await fetchStudent(studentId);
  if (isWhatsappContactBlocked(student)) {
    return jsonResponse({
      error: `${whatsappContactBlockedMessage(student)} Payment can still be confirmed, but no WhatsApp confirmation was sent.`,
    }, 409);
  }
  const to = normalizePhone(String(student.parent_contact_no || ""));
  if (!to) {
    return jsonResponse({ error: "Parent phone number is missing." }, 400);
  }

  const fromDate = String(payload.fromDate || payload.cycleStartDate || "");
  const toDate = String(payload.toDate || "");
  const planLabel = String(payload.planLabel || "renewal");
  const isJoining = payload.isJoiningFee === true || planLabel.toLowerCase().includes("joining") || planLabel.toLowerCase().includes("admission");
  
  const amount = Number(payload.amount || 0);
  const amountText = Number.isFinite(amount) && amount > 0
    ? ` Amount received: Rs ${amount.toLocaleString("en-IN")}.`
    : "";

  const actionText = isJoining ? "admission and first cycle" : planLabel;
  const terms = getGenderTerms(student.name);
  
  const happyMessage = isJoining
    ? `Welcome to the Gen Alpha family! We are thrilled to start this journey with your ${terms.child} and help develop skills on the field. 🏏`
    : `Great to see the commitment! We are excited to continue working with your ${terms.child} and watching progress every day. Let's keep the game going! 🏏`;

  const message = `✅ *Payment Confirmed!* 🏏\n\nHi! We've successfully received the payment for *${student.name || "Player"}'s* *${actionText}*. The training status has been updated until *${displayDate(toDate)}*.\n\n*Amount received: Rs ${amount.toLocaleString("en-IN")}.*\n\n${happyMessage}\n\nThank you for being part of Gen Alpha Cricket Academy!`;

  const metaResponse = await sendTextMessage(to, message);
  const confirmationMessageId = String(metaResponse?.messages?.[0]?.id || "");
  const confirmedAt = new Date().toISOString();

  const latestReminder = await findLatestReminderByPhone(to);
  if (latestReminder?.id && latestReminder.student_id === student.id) {
    await updateReminderEvent(latestReminder.id, {
      status: "payment_confirmed",
      payment_confirmed_at: confirmedAt,
      confirmation_message_id: confirmationMessageId,
      confirmation_sent_at: confirmedAt,
      confirmation_meta_response: metaResponse,
      meta_response: {
        ...(latestReminder.meta_response || {}),
        renewal_verified: {
          student_id: student.id,
          from_date: fromDate,
          to_date: toDate,
          plan_label: planLabel,
          amount,
          sent_by: managerEmail,
          sent_at: confirmedAt,
          meta_response: metaResponse,
        },
      },
    });
    await updateLatestPaymentLinkRequest(latestReminder.id, {
      status: "payment_confirmed",
      payment_confirmed_at: confirmedAt,
    });
    await insertWhatsappFlowEvent({
      student_id: student.id,
      reminder_event_id: latestReminder.id,
      event_type: "payment_confirmed",
      direction: "manager",
      parent_phone: String(student.parent_contact_no || "").slice(-10),
      message_kind: "manager_confirmation",
      status: "payment_confirmed",
      status_at: confirmedAt,
      payment_plan: planLabel,
      payment_amount: amount,
      payment_from_date: fromDate || null,
      payment_to_date: toDate || null,
      created_by: managerEmail,
    });
    await insertWhatsappFlowEvent({
      student_id: student.id,
      reminder_event_id: latestReminder.id,
      event_type: "confirmation_message_sent",
      direction: "outbound",
      parent_phone: String(student.parent_contact_no || "").slice(-10),
      message_kind: "confirmation_text",
      message_body: message,
      message_id: confirmationMessageId,
      status: confirmationMessageId ? "accepted" : "sent",
      status_at: confirmedAt,
      sent_at: confirmedAt,
      payment_plan: planLabel,
      payment_amount: amount,
      payment_from_date: fromDate || null,
      payment_to_date: toDate || null,
      provider_payload: metaResponse,
      created_by: managerEmail,
    });
  }
  await insertStudentTimelineEvent({
    student_id: student.id,
    event_type: "renewal_whatsapp_confirmation",
    event_date: new Date().toISOString().slice(0, 10),
    title: "Renewal confirmation sent",
    details: `${planLabel} renewed from ${displayDate(fromDate)} to ${displayDate(toDate)}${
      amountText ? ` - ${amountText.trim()}` : ""
    }`,
    changed_by: managerEmail,
  });

  return jsonResponse({
    success: true,
    message: `Renewal confirmation sent for ${student.name}.`,
    metaResponse,
  });
}

async function handleSendAdmissionReminder(request: Request, payload: any) {
  const managerEmail = await assertAuthenticated(request);
  const admissionId = String(payload.admissionId || "");
  if (!admissionId) return jsonResponse({ error: "admissionId is required." }, 400);

  const admission = (await rest(`admissions?id=eq.${encodeURIComponent(admissionId)}&review_status=eq.pending&limit=1`))?.[0];
  if (!admission) return jsonResponse({ error: "Admission form not found." }, 404);

  const to = normalizePhone(String(admission.parent_contact_no || admission.alternate_contact_no || ""));
  if (!to) return jsonResponse({ error: "Parent contact number is missing." }, 400);

  const amount = Number(admission.admission_fee_total || 4000);
  const plan = String(admission.fee_plan || "monthly");
  const terms = getGenderTerms(admission.applicant_name);
  
  // Build payment page URL
  const paymentPageUrl = `${PAYMENT_PAGE_URL}?a=${amount}&name=${encodeURIComponent(admission.applicant_name)}&p=${encodeURIComponent(plan)}`;
  
  const message = `🏏 *Gen Alpha Cricket Academy - Follow up*\n\nHi! Coach here from Gen Alpha—just following up on *${admission.applicant_name}'s* admission. We're excited to have *${admission.applicant_name}* start training with us!\n\n*Amount: Rs ${amount.toLocaleString("en-IN")}*\n\n*Pay here: ${paymentPageUrl}*\n\nOnce paid, the spot will be confirmed in the *${admission.time_slot}* slot. Let us know if you have any questions! 🏏`;

  let metaResponse;
  try {
    metaResponse = await sendTextMessage(to, message);
  } catch (error) {
    const errorPayload = error instanceof Error
      ? { message: error.message }
      : { message: String(error) };
    await insertWhatsappFlowEvent({
      admission_id: admission.id,
      event_type: "admission_reminder_failed",
      direction: "outbound",
      parent_phone: to.slice(-10),
      message_kind: "text",
      message_body: message,
      status: "send_failed",
      failed_at: new Date().toISOString(),
      error_message: errorPayload.message,
      provider_payload: errorPayload,
      created_by: managerEmail,
    });
    return jsonResponse({
      success: false,
      source: "meta_whatsapp",
      error: `Meta WhatsApp send failed: ${errorPayload.message}`,
    }, 502);
  }
  await insertWhatsappFlowEvent({
    admission_id: admission.id,
    event_type: "admission_reminder_sent",
    direction: "outbound",
    parent_phone: to.slice(-10),
    message_kind: "text",
    message_body: message,
    message_id: String(metaResponse?.messages?.[0]?.id || ""),
    status: String(metaResponse?.messages?.[0]?.id || "") ? "accepted" : "sent",
    status_at: new Date().toISOString(),
    sent_at: new Date().toISOString(),
    payment_plan: plan,
    payment_amount: amount,
    provider_payload: metaResponse,
    created_by: managerEmail,
  });

  // Update last_nudge_at to ensure automated schedule respects manual nudges
  await rest(`admissions?id=eq.${admission.id}`, {
    method: "PATCH",
    body: JSON.stringify({ last_nudge_at: new Date().toISOString() })
  });

  return jsonResponse({
    success: true,
    message: `Reminder sent to ${admission.applicant_name}'s parent.`,
    metaResponse
  });
}

async function handleWebhook(payload: any) {
  for (const entry of payload?.entry || []) {
    for (const change of entry?.changes || []) {
      for (const statusUpdate of change?.value?.statuses || []) {
        const messageId = String(statusUpdate?.id || "");
        if (!messageId) continue;

        const status = String(statusUpdate?.status || "unknown");
        const timestamp = whatsappTimestampToIso(statusUpdate?.timestamp);
        const trackedReminder = await findReminderByAnyWhatsappMessageId(
          messageId,
        );
        const trackedFlowEvent = await findWhatsappFlowEventByMessageId(
          messageId,
        );
        if (!trackedReminder?.id && !trackedFlowEvent?.id) continue;

        const flowStatusPatch: Record<string, unknown> = {
          status,
          status_at: timestamp,
        };
        if (status === "sent") flowStatusPatch.sent_at = timestamp;
        if (status === "delivered") flowStatusPatch.delivered_at = timestamp;
        if (status === "read") flowStatusPatch.read_at = timestamp;
        if (status === "failed") {
          flowStatusPatch.failed_at = timestamp;
          flowStatusPatch.error_code = String(
            statusUpdate?.errors?.[0]?.code || "",
          );
          flowStatusPatch.error_message = String(
            statusUpdate?.errors?.[0]?.message || "",
          );
        }
        if (trackedFlowEvent?.id) {
          await updateWhatsappFlowEvent(trackedFlowEvent.id, flowStatusPatch);
        }

        if (!trackedReminder?.id) {
          await insertWhatsappFlowEvent({
            student_id: trackedFlowEvent?.student_id || null,
            admission_id: trackedFlowEvent?.admission_id || null,
            reminder_event_id: trackedFlowEvent?.reminder_event_id || null,
            payment_link_request_id:
              trackedFlowEvent?.payment_link_request_id || null,
            event_type: "whatsapp_message_status",
            direction: "provider",
            parent_phone: String(trackedFlowEvent?.parent_phone || ""),
            message_kind: String(trackedFlowEvent?.message_kind || ""),
            message_id: messageId,
            status,
            status_at: timestamp,
            sent_at: status === "sent" ? timestamp : null,
            delivered_at: status === "delivered" ? timestamp : null,
            read_at: status === "read" ? timestamp : null,
            failed_at: status === "failed" ? timestamp : null,
            error_code: String(statusUpdate?.errors?.[0]?.code || ""),
            error_message: String(statusUpdate?.errors?.[0]?.message || ""),
            provider_payload: statusUpdate,
            created_by: "Meta",
          });
          continue;
        }

        const isConfirmationMessage =
          String(trackedReminder.confirmation_message_id || "") === messageId;
        const updatePayload: Record<string, unknown> = isConfirmationMessage
          ? {
            confirmation_meta_response: statusUpdate,
          }
          : {
            status,
            meta_response: statusUpdate,
          };

        if (status === "sent") {
          updatePayload[isConfirmationMessage ? "confirmation_sent_at" : "accepted_at"] = timestamp;
        }
        if (status === "delivered") {
          updatePayload[isConfirmationMessage ? "confirmation_delivered_at" : "delivered_at"] = timestamp;
        }
        if (status === "read") {
          updatePayload[isConfirmationMessage ? "confirmation_read_at" : "read_at"] = timestamp;
        }
        if (status === "failed") {
          updatePayload[isConfirmationMessage ? "confirmation_failed_at" : "failed_at"] = timestamp;
          updatePayload[isConfirmationMessage ? "confirmation_meta_error" : "meta_error"] = statusUpdate;
          if (!isConfirmationMessage) {
            updatePayload.next_retry_at = null;
            updatePayload.retry_reason = providerErrorMessage(
              parseProviderError(statusUpdate),
            );
            updatePayload.manual_followup_required = true;
            updatePayload.manual_followup_reason = "delivery_failure";
          }
        }

        if (
          status === "failed" &&
          !isConfirmationMessage &&
          isHealthyEcosystemError(parseProviderError(statusUpdate))
        ) {
          await scheduleHealthyEcosystemRetry(
            trackedReminder,
            parseProviderError(statusUpdate),
            timestamp,
            "Meta",
          );
        } else {
          await updateReminderEvent(trackedReminder.id, updatePayload);
        }
        await insertWhatsappFlowEvent({
          student_id: trackedReminder.student_id,
          reminder_event_id: trackedReminder.id,
          event_type: isConfirmationMessage
            ? "confirmation_message_status"
            : "reminder_message_status",
          direction: "provider",
          parent_phone: String(trackedReminder.parent_phone || ""),
          message_kind: isConfirmationMessage ? "confirmation_text" : "template",
          message_body: trackedFlowEvent?.message_body ||
            (isConfirmationMessage ? "" : trackedReminder.message_preview || ""),
          message_id: messageId,
          status,
          status_at: timestamp,
          sent_at: status === "sent" ? timestamp : null,
          accepted_at: status === "sent" && !isConfirmationMessage
            ? timestamp
            : null,
          delivered_at: status === "delivered" ? timestamp : null,
          read_at: status === "read" ? timestamp : null,
          failed_at: status === "failed" ? timestamp : null,
          error_code: String(statusUpdate?.errors?.[0]?.code || ""),
          error_message: String(statusUpdate?.errors?.[0]?.message || ""),
          provider_payload: statusUpdate,
          created_by: "Meta",
        });
      }

      for (const message of change?.value?.messages || []) {
        const from = String(message.from || "");
        try {
          const intakeResult = await forwardToAdmissionIntake(change?.value, message);
          if (intakeResult) continue;
        } catch (error) {
          // A message addressed to the dedicated admission number must never
          // fall through into the renewal-payment conversation handler.
          console.error("Admission intake forwarding failed", error);
          continue;
        }
        const reply = message?.interactive?.button_reply || message?.button;
        const replyPayload = String(
          reply?.id || reply?.payload || reply?.title || reply?.text || "",
        );
        const parts = replyPayload.startsWith("renewal:")
          ? replyPayload.split(":")
          : [];
        const eventId = parts[1] || "";
        const labelText = String(reply?.title || reply?.text || "");
        const plan = parts[2] ||
          normalizeSelectedPlan(replyPayload) ||
          normalizeSelectedPlan(labelText) ||
          normalizeSelectedPlan(message?.text?.body);
        const webhookLog = await insertWebhookEvent({
          event_type: "incoming_message",
          from_phone: from.slice(-10),
          message_id: String(message.id || ""),
          reminder_event_id: eventId || null,
          processed: false,
          processing_note: plan
            ? `Plan detected: ${plan}`
            : "No matching plan detected.",
          payload: message,
        });
        if (!plan && isPaymentConfirmationMessage(message)) {
          await handlePaymentConfirmationMessage(from, message, webhookLog);
          continue;
        }
        if (!plan) {
          continue;
        }

        const settings = await loadSettings();
        const reminderEvent = eventId
          ? (await rest(
            `reminder_events?select=*&id=eq.${
              encodeURIComponent(eventId)
            }&limit=1`,
          ))?.[0]
          : await findLatestReminderByPhone(from);
        if (!reminderEvent) {
          if (webhookLog?.id) {
            await rest(
              `whatsapp_webhook_events?id=eq.${
                encodeURIComponent(webhookLog.id)
              }`,
              {
                method: "PATCH",
                body: JSON.stringify({
                  processing_note:
                    "Plan detected but no matching reminder event found.",
                }),
              },
            );
          }
          continue;
        }

        if (plan === "need_help") {
          await updateReminderEvent(reminderEvent.id, {
            selected_plan: plan,
            help_requested: true,
            status: "help_requested",
          });
          await insertWhatsappFlowEvent({
            student_id: reminderEvent.student_id,
            reminder_event_id: reminderEvent.id,
            event_type: "parent_help_requested",
            direction: "parent",
            parent_phone: from.slice(-10),
            message_kind: String(message.type || "button"),
            message_body: labelText || replyPayload,
            message_id: String(message.id || ""),
            status: "help_requested",
            status_at: new Date().toISOString(),
            provider_payload: message,
            created_by: "WhatsApp",
          });
          if (webhookLog?.id) {
            await rest(
              `whatsapp_webhook_events?id=eq.${
                encodeURIComponent(webhookLog.id)
              }`,
              {
                method: "PATCH",
                body: JSON.stringify({
                  reminder_event_id: reminderEvent.id,
                  processed: true,
                  processing_note: "Help request processed.",
                }),
              },
            );
          }
          const helpMessage =
            `Please contact Gen Alpha Cricket Academy manager: ${settings.managerPhone}`;
          const helpResponse = await sendTextMessage(from, helpMessage);
          await insertWhatsappFlowEvent({
            student_id: reminderEvent.student_id,
            reminder_event_id: reminderEvent.id,
            event_type: "help_reply_sent",
            direction: "outbound",
            parent_phone: from.slice(-10),
            message_kind: "text",
            message_body: helpMessage,
            message_id: String(helpResponse?.messages?.[0]?.id || ""),
            status: String(helpResponse?.messages?.[0]?.id || "")
              ? "accepted"
              : "sent",
            status_at: new Date().toISOString(),
            sent_at: new Date().toISOString(),
            provider_payload: helpResponse,
            created_by: "whatsapp-webhook",
          });
          continue;
        }

        const student = await fetchStudent(reminderEvent.student_id);
        const linkRequest = await createUpiPaymentLink(
          student,
          plan,
          reminderEvent,
          settings,
        );
        const selectedAmount = paymentAmountForReminderType(
          String(reminderEvent.reminder_type || "renewal"),
          plan,
        );
        await insertWhatsappFlowEvent({
          student_id: reminderEvent.student_id,
          reminder_event_id: reminderEvent.id,
          payment_link_request_id: linkRequest?.id || null,
          event_type: "parent_plan_selected",
          direction: "parent",
          parent_phone: from.slice(-10),
          message_kind: String(message.type || "button"),
          message_body: labelText || replyPayload,
          message_id: String(message.id || ""),
          status: "plan_selected",
          status_at: new Date().toISOString(),
          payment_plan: plan,
          payment_amount: selectedAmount,
          payment_months: PLAN_MONTHS[plan],
          payment_from_date: reminderEvent.due_date || null,
          provider_payload: message,
          created_by: "WhatsApp",
        });
        await updateReminderEvent(reminderEvent.id, {
          selected_plan: plan,
          amount: selectedAmount,
          payment_link_url: linkRequest.payment_link_url || "",
          payment_link_id: linkRequest.payment_link_id || "",
          status: linkRequest.dry_run
            ? "payment_link_dry_run"
            : "payment_link_sent",
          dry_run: Boolean(linkRequest.dry_run),
          payment_link_sent_at: new Date().toISOString(),
        });
        await updateLatestPaymentLinkRequest(reminderEvent.id, {
          payment_link_sent_at: new Date().toISOString(),
        });
        if (webhookLog?.id) {
          await rest(
            `whatsapp_webhook_events?id=eq.${
              encodeURIComponent(webhookLog.id)
            }`,
            {
              method: "PATCH",
              body: JSON.stringify({
                reminder_event_id: reminderEvent.id,
                processed: true,
                processing_note: linkRequest.dry_run
                  ? "Payment link dry-run processed."
                  : "UPI payment link sent.",
              }),
            },
          );
        }
        if (!linkRequest.dry_run && linkRequest.payment_link_url) {
          const paymentMessage = `🏏 *Gen Alpha Cricket Academy - Payment Request*\n*Amount: Rs ${selectedAmount.toLocaleString("en-IN")}*\n\n*Pay here: ${linkRequest.payment_link_url}*\n\n${paymentContactDetails()}`;
          const paymentMessageResponse = await sendTextMessage(
            from,
            paymentMessage,
          );
          await insertWhatsappFlowEvent({
            student_id: reminderEvent.student_id,
            reminder_event_id: reminderEvent.id,
            payment_link_request_id: linkRequest?.id || null,
            event_type: "payment_link_sent",
            direction: "outbound",
            parent_phone: from.slice(-10),
            message_kind: "payment_link",
            message_body: paymentMessage,
            message_id: String(paymentMessageResponse?.messages?.[0]?.id || ""),
            status: String(paymentMessageResponse?.messages?.[0]?.id || "")
              ? "accepted"
              : "sent",
            status_at: new Date().toISOString(),
            sent_at: new Date().toISOString(),
            payment_plan: plan,
            payment_amount: selectedAmount,
            payment_months: PLAN_MONTHS[plan],
            payment_from_date: reminderEvent.due_date || null,
            provider_payload: paymentMessageResponse,
            created_by: "whatsapp-webhook",
          });
        }
      }
    }
  }
  return jsonResponse({ success: true });
}

async function handleAutoSchedule() {
  const settings = await loadSettings();
  if (!settings.whatsappRemindersEnabled) {
    return jsonResponse({ success: true, message: "Auto-reminders disabled." });
  }

  const retryResults = await processDueReminderRetries(RETRY_WORKER_LIMIT, settings);
  const todayIso = localIsoDate();

  // Fetch all active students
  const students = await rest(
    "students?discontinued=eq.false",
  );

  // Fetch payments to calculate next renewal date
  const payments = await rest(
    "student_payments?order=paid_on.desc",
  );

  // Fetch recent follow-ups to deduplicate and check rules
  const followUps = await rest(
    "reminder_events?order=created_at.desc&limit=1000",
  );

  const results = [];

  for (const student of students) {
    const isJoiningFee = student.fees_paid !== true &&
      student.fees_paid !== "yes";

    // Skip if initial joining payment is pending verification
    const amountPaid = Number(student.amount_paid || 0);
    const paymentReference = String(student.payment_reference || "").trim();
    if (isJoiningFee && (amountPaid > 0 || paymentReference)) continue;
    const dueDate = isJoiningFee
      ? student.join_date
      : getPaidThroughDate(student, payments);
    const rawDaysSince = getDaysSinceDate(dueDate);
    const overdueDays = Math.max(0, rawDaysSince);
    const lastFollowUp = followUps.find((f: any) => f.student_id === student.id);
    let isHeadsUp = false;
    let isRenewalDay = false;

    if (isWhatsappContactBlocked(student)) {
      if (lastFollowUp?.id) {
        await markReminderContactBlocked(lastFollowUp, student);
      }
      console.log(`Skipping ${student.name}: ${whatsappContactBlockedMessage(student)}`);
      continue;
    }

    console.log(`Checking student ${student.name}: dueDate=${dueDate}, overdueDays=${overdueDays}, isJoiningFee=${isJoiningFee}`);

    let shouldSend = false;
    const sentToday = lastFollowUp && lastFollowUp.created_at.slice(0, 10) === todayIso;

    if (sentToday) {
      console.log(`Skipping ${student.name}: already sent a reminder today.`);
      continue;
    }

    if (rawDaysSince === -2 && !isJoiningFee) {
      // Soft heads-up 2 days before
      const alreadySentHeadsUp = lastFollowUp?.reminder_type === "heads_up" && lastFollowUp?.due_date === dueDate;
      if (!alreadySentHeadsUp) {
        isHeadsUp = true;
        shouldSend = true;
      }
    } else if (rawDaysSince === 0) {
      // Formal reminder on due day (for both joining and renewal)
      const dueReminderType = isJoiningFee ? "joining_fee" : "renewal_day";
      const alreadySentDueDay = lastFollowUp?.reminder_type === dueReminderType &&
        lastFollowUp?.due_date === dueDate;
      if (!alreadySentDueDay) {
        isRenewalDay = !isJoiningFee;
        shouldSend = true;
      }
    } else if (overdueDays >= MANUAL_FOLLOWUP_OVERDUE_DAYS) {
      // Stop the existing daily reminder flow after 15 overdue days and hand it to staff.
      if (lastFollowUp?.id && lastFollowUp?.manual_followup_required !== true) {
        await updateReminderEvent(lastFollowUp.id, {
          status: "manual_followup",
          manual_followup_required: true,
          manual_followup_reason: "overdue_15_days",
          next_retry_at: null,
          retry_reason: `Overdue ${overdueDays} days. Automatic reminders paused; manual follow-up required.`,
        });
      }
      console.log(`Skipping ${student.name}: overdue ${overdueDays} days, manual follow-up required.`);
      continue;
    } else if (overdueDays === 5) {
      // 5-day overdue nudge
      shouldSend = true;
    } else if (overdueDays >= 7) {
      // Daily nudge from Day 7 until the manual follow-up cutoff.
      shouldSend = true;
    }

    if (shouldSend) {
      const reminderType = isHeadsUp ? "heads_up" : (isRenewalDay ? "renewal_day" : (isJoiningFee ? "joining_fee" : "renewal"));
      const dryRun = settings.dryRunMode;
      const parentPhone = String(student.parent_contact_no || "").replace(
        /\D/g,
        "",
      ).slice(-10);

      const sendTask = (async () => {
        let event: any = null;
        try {
          event = await insertReminderEvent({
            student_id: student.id,
            reminder_type: reminderType,
            channel: "whatsapp",
            status: dryRun ? "dry_run" : "queued",
            dry_run: dryRun,
            due_date: dueDate,
            overdue_days: overdueDays,
            plan_options: PLAN_OPTIONS,
            parent_phone: parentPhone,
            manager_phone: settings.managerPhone,
            message_preview: buildReminderPreview(
              student,
              dueDate,
              settings,
              reminderType,
            ),
            created_by: "system_auto",
          });
          await insertWhatsappFlowEvent({
            student_id: student.id,
            reminder_event_id: event.id,
            event_type: "reminder_created",
            direction: "outbound",
            parent_phone: parentPhone,
            message_kind: "template",
            message_body: event.message_preview || "",
            status: dryRun ? "dry_run" : "queued",
            status_at: new Date().toISOString(),
            payment_from_date: dueDate,
            created_by: "system_auto",
          });

          if (!dryRun) {
            const to = normalizePhone(parentPhone);
            if (!to) {
              await markReminderSendFailed(
                event,
                { message: "Parent phone number is missing." },
                new Date().toISOString(),
                "system_auto",
              );
              return {
                student: student.name,
                error: "Parent phone number is missing.",
              };
            }
            await sendReminderTemplate(
              to,
              event,
              student,
              dueDate,
              reminderType,
              settings,
              "system_auto",
            );
          }
          return { student: student.name, status: "sent" };
        } catch (e) {
          if (event?.id) {
            const errorPayload = parseProviderError(e);
            const failedAt = new Date().toISOString();
            if (isHealthyEcosystemError(errorPayload)) {
              const retry = await scheduleHealthyEcosystemRetry(
                event,
                errorPayload,
                failedAt,
                "system_auto",
              );
              return {
                student: student.name,
                status: retry.scheduled ? "retry_scheduled" : "send_failed",
                nextRetryAt: retry.nextRetryAt || null,
              };
            }
            await markReminderSendFailed(
              event,
              errorPayload,
              failedAt,
              "system_auto",
            );
          }
          return { student: student.name, error: (e as Error).message };
        }
      })();
      
      results.push(sendTask);
      // Small stagger to not hit Meta all at once, but don't await the whole thing
      await new Promise((r) => setTimeout(r, 200)); 
    }
  }

  const processed = await Promise.all(results);
  console.log(`Auto-schedule finished. Processed ${processed.length} tasks.`);
  return jsonResponse({ success: true, processed, retries: retryResults });
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method === "GET") {
    const url = new URL(request.url);
    const mode = url.searchParams.get("hub.mode");
    const token = url.searchParams.get("hub.verify_token");
    const challenge = url.searchParams.get("hub.challenge");
    if (
      mode === "subscribe" && token === env("WHATSAPP_WEBHOOK_VERIFY_TOKEN")
    ) {
      return new Response(challenge || "", { headers: corsHeaders });
    }
    return jsonResponse({ error: "Webhook verification failed." }, 403);
  }

  if (request.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  try {
    const payload = await request.json();
    if (payload?.action === "send_reminder") {
      return await handleSendReminder(request, payload);
    }
    if (
      [
        "send_sample_reminder",
        "send_sample_direct_pay_reminder",
        "send_sample_v2_reminder",
        "send_sample_legacy_reminder",
      ].includes(String(payload?.action || ""))
    ) {
      return await handleSendSampleReminder(request, payload);
    }
    if (payload?.action === "renewal_verified") {
      return await handleRenewalVerified(request, payload);
    }
    if (payload?.action === "setup_direct_payment_template") {
      return await handleSetupDirectPaymentTemplate(request);
    }
    if (payload?.action === "payment_options") {
      return await handlePaymentOptions(payload);
    }
    if (payload?.action === "payment_attempted") {
      return await handlePaymentAttempted(payload);
    }
    if (payload?.action === "auto_schedule") {
      await assertAuthenticatedOrServiceRole(request);
      return await handleAutoSchedule();
    }
    if (payload?.action === "retry_due_reminders") {
      await assertAuthenticatedOrServiceRole(request);
      const settings = await loadSettings();
      const retries = settings.whatsappRemindersEnabled
        ? await processDueReminderRetries(RETRY_WORKER_LIMIT, settings)
        : [];
      const managerPaymentAlerts = await processDueManagerPaymentAlerts();
      return jsonResponse({
        success: true,
        remindersPaused: !settings.whatsappRemindersEnabled,
        message: settings.whatsappRemindersEnabled
          ? "Due reminder retries processed."
          : "Reminder retries paused by global reminder setting.",
        retries,
        managerPaymentAlerts,
      });
    }
    if (payload?.action === "send_admission_reminder") {
      return await handleSendAdmissionReminder(request, payload);
    }
    if (payload?.action === "auto_backup") {
      return await handleAutoBackup(payload);
    }
    return await handleWebhook(payload);
  } catch (error) {
    return jsonResponse({
      error: error instanceof Error
        ? error.message
        : "WhatsApp reminder failed.",
    }, 500);
  }
});
