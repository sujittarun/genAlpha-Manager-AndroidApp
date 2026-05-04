const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

type ReminderSettings = {
  whatsappRemindersEnabled: boolean;
  paymentLinksEnabled: boolean;
  dryRunMode: boolean;
  managerPhone: string;
};

const DEFAULT_SETTINGS: ReminderSettings = {
  whatsappRemindersEnabled: false,
  paymentLinksEnabled: false,
  dryRunMode: true,
  managerPhone: "8143960950",
};
const ACADEMY_UPI_ID = "9059962498@ybl";
const ACADEMY_PAYEE_NAME = "Gen Alpha Cricket Academy";

const PLAN_OPTIONS = ["monthly", "quarterly", "halfyearly", "need_help"];
const PLAN_LABELS: Record<string, string> = {
  monthly: "1 Month",
  quarterly: "3 Months",
  halfyearly: "6 Months",
  need_help: "Need Help",
};
const PLAN_AMOUNTS: Record<string, number> = {
  monthly: 3500,
  quarterly: 10500,
  halfyearly: 21000,
};
const PLAN_MONTHS: Record<string, number> = {
  monthly: 1,
  quarterly: 3,
  halfyearly: 6,
};

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

function localIsoDate(date = new Date()): string {
  return date.toISOString().slice(0, 10);
}

function buildUpiLink(student: any, plan: string, amount: number): string {
  const note = `Gen Alpha ${PLAN_LABELS[plan]} fee - ${student.name || "Player"}`;
  const params = new URLSearchParams({
    pa: ACADEMY_UPI_ID,
    pn: ACADEMY_PAYEE_NAME,
    am: String(amount),
    cu: "INR",
    tn: note,
  });
  return `upi://pay?${params.toString()}`;
}

function daysSince(dateValue: string): number {
  const due = new Date(`${dateValue}T00:00:00+05:30`);
  const now = new Date();
  return Math.floor((now.getTime() - due.getTime()) / 86400000);
}

function parseBoolean(value: unknown, fallback: boolean): boolean {
  if (typeof value === "boolean") return value;
  if (typeof value === "string") return value.toLowerCase() === "true";
  return fallback;
}

function parseText(value: unknown, fallback: string): string {
  return typeof value === "string" && value.trim() ? value.trim() : fallback;
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

async function assertAuthenticated(request: Request) {
  const authHeader = request.headers.get("authorization") || "";
  const token = authHeader.replace(/^Bearer\s+/i, "");
  if (!token) throw new Error("Manager login is required.");

  const response = await fetch(`${env("SUPABASE_URL").replace(/\/+$/, "")}/auth/v1/user`, {
    headers: {
      apikey: env("SUPABASE_ANON_KEY"),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!response.ok) throw new Error("Manager session is not valid.");
  const user = await response.json();
  return user?.email || "manager";
}

async function loadSettings(): Promise<ReminderSettings> {
  const rows = await rest(
    "system_settings?select=setting_key,setting_value&setting_key=in.(whatsapp_reminders_enabled,payment_links_enabled,dry_run_mode)",
  );
  const byKey = Object.fromEntries((rows || []).map((row: any) => [row.setting_key, row.setting_value]));
  return {
    whatsappRemindersEnabled: parseBoolean(byKey.whatsapp_reminders_enabled, false),
    paymentLinksEnabled: parseBoolean(byKey.payment_links_enabled, false),
    dryRunMode: parseBoolean(byKey.dry_run_mode, true),
    managerPhone: DEFAULT_SETTINGS.managerPhone,
  };
}

async function fetchStudent(studentId: string) {
  const rows = await rest(`students?select=*&id=eq.${encodeURIComponent(studentId)}&limit=1`);
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

async function updateReminderEvent(id: string, payload: Record<string, unknown>) {
  await rest(`reminder_events?id=eq.${encodeURIComponent(id)}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

async function findLatestReminderByPhone(phone: string) {
  const parentPhone = phone.slice(-10);
  const rows = await rest(
    `reminder_events?select=*&parent_phone=eq.${encodeURIComponent(parentPhone)}&order=created_at.desc&limit=1`,
  );
  return rows?.[0] || null;
}

function buildReminderPreview(student: any, dueDate: string, settings: ReminderSettings, reminderType: string) {
  const choices = PLAN_OPTIONS.map((option) => PLAN_LABELS[option]).join(" / ");
  const dueText = reminderType === "joining_fee" ? `joining fee from ${dueDate}` : `renewal due ${dueDate}`;
  return `Gen Alpha Cricket Academy reminder for ${student.name}: ${dueText}. Parent can choose ${choices}. Manager help: ${settings.managerPhone}.`;
}

async function sendTemplateMessage(to: string, eventId: string, student: any, dueDate: string) {
  const token = env("META_WHATSAPP_TOKEN");
  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  const templateName = env("META_WHATSAPP_TEMPLATE_NAME") || "gen_alpha_fee_reminder";
  const languageCode = env("META_WHATSAPP_TEMPLATE_LANGUAGE") || "en";
  if (!token || !phoneNumberId) {
    throw new Error("Meta WhatsApp secrets are missing.");
  }

  const response = await fetch(`https://graph.facebook.com/v20.0/${phoneNumberId}/messages`, {
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
              { type: "text", text: dueDate },
              { type: "text", text: "1 Month, 3 Months, 6 Months" },
            ],
          },
          ...["monthly", "quarterly", "halfyearly", "need_help"].map((plan, index) => ({
            type: "button",
            sub_type: "quick_reply",
            index: String(index),
            parameters: [{ type: "payload", payload: `renewal:${eventId}:${plan}` }],
          })),
        ],
      },
    }),
  });

  const body = await response.json();
  if (!response.ok) {
    throw new Error(body?.error?.message || "Meta WhatsApp send failed.");
  }
  return body;
}

async function sendTextMessage(to: string, text: string) {
  const token = env("META_WHATSAPP_TOKEN");
  const phoneNumberId = env("META_WHATSAPP_PHONE_NUMBER_ID");
  if (!token || !phoneNumberId) return null;

  const response = await fetch(`https://graph.facebook.com/v20.0/${phoneNumberId}/messages`, {
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
  });
  return await response.json();
}

async function createUpiPaymentLink(student: any, plan: string, reminderEvent: any, settings: ReminderSettings) {
  const dryRun = settings.dryRunMode || !settings.paymentLinksEnabled;
  const amount = PLAN_AMOUNTS[plan] || 0;
  const months = PLAN_MONTHS[plan] || 0;

  if (dryRun) {
    return await insertPaymentLinkRequest({
      reminder_event_id: reminderEvent.id,
      student_id: reminderEvent.student_id,
      payment_type: reminderEvent.reminder_type === "joining_fee" ? "joining" : "renewal",
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

  return await insertPaymentLinkRequest({
    reminder_event_id: reminderEvent.id,
    student_id: reminderEvent.student_id,
    payment_type: reminderEvent.reminder_type === "joining_fee" ? "joining" : "renewal",
    plan_type: plan,
    months_covered: months,
    amount,
    cycle_start_date: reminderEvent.due_date,
    provider: "upi",
    status: "created",
    dry_run: false,
    payment_link_url: upiLink,
    payment_link_id: "",
    created_by: "whatsapp-webhook",
  });
}

async function handleSendReminder(request: Request, payload: any) {
  const managerEmail = await assertAuthenticated(request);
  const { studentId, dueDate: inputDueDate, reminderType: inputType } = payload;
  if (!studentId) return jsonResponse({ error: "studentId is required." }, 400);

  const settings = await loadSettings();
  const student = await fetchStudent(String(studentId));
  const reminderType = String(inputType || (student.fees_paid ? "renewal" : "joining_fee"));
  const dueDate = String(inputDueDate || (reminderType === "joining_fee" ? student.join_date : localIsoDate()));
  const overdueDays = Math.max(0, daysSince(dueDate));
  const dryRun = settings.dryRunMode || !settings.whatsappRemindersEnabled;
  const parentPhone = String(student.parent_contact_no || "").replace(/\D/g, "").slice(-10);
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
    message_preview: buildReminderPreview(student, dueDate, settings, reminderType),
    created_by: managerEmail,
  });

  if (dryRun) {
    await insertPaymentLinkRequest({
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
    return jsonResponse({ success: true, dryRun: true, message: `Dry-run reminder logged for ${student.name}.` });
  }

  const to = normalizePhone(parentPhone);
  if (!to) return jsonResponse({ error: "Parent phone number is missing." }, 400);
  const metaResponse = await sendTemplateMessage(to, event.id, student, dueDate);
  await updateReminderEvent(event.id, { status: "sent" });
  return jsonResponse({ success: true, dryRun: false, message: `WhatsApp reminder sent for ${student.name}.`, metaResponse });
}

async function handleWebhook(payload: any) {
  for (const entry of payload?.entry || []) {
    for (const change of entry?.changes || []) {
      for (const message of change?.value?.messages || []) {
        const from = String(message.from || "");
        const reply = message?.interactive?.button_reply || message?.button;
        const replyPayload = String(reply?.id || reply?.payload || reply?.title || reply?.text || "");
        const parts = replyPayload.startsWith("renewal:") ? replyPayload.split(":") : [];
        const eventId = parts[1] || "";
        const labelText = String(reply?.title || reply?.text || "");
        const plan = parts[2] || Object.entries(PLAN_LABELS).find(([, label]) => label === labelText)?.[0] || "";
        if (!plan) continue;

        const settings = await loadSettings();
        const reminderEvent = eventId
          ? (await rest(`reminder_events?select=*&id=eq.${encodeURIComponent(eventId)}&limit=1`))?.[0]
          : await findLatestReminderByPhone(from);
        if (!reminderEvent) continue;

        if (plan === "need_help") {
          await updateReminderEvent(reminderEvent.id, {
            selected_plan: plan,
            help_requested: true,
            status: "help_requested",
          });
          await sendTextMessage(from, `Please contact Gen Alpha Cricket Academy manager: ${settings.managerPhone}`);
          continue;
        }

        const student = await fetchStudent(reminderEvent.student_id);
        const linkRequest = await createUpiPaymentLink(student, plan, reminderEvent, settings);
        await updateReminderEvent(reminderEvent.id, {
          selected_plan: plan,
          amount: PLAN_AMOUNTS[plan],
          payment_link_url: linkRequest.payment_link_url || "",
          payment_link_id: linkRequest.payment_link_id || "",
          status: linkRequest.dry_run ? "payment_link_dry_run" : "payment_link_sent",
          dry_run: Boolean(linkRequest.dry_run),
        });
        if (!linkRequest.dry_run && linkRequest.payment_link_url) {
          await sendTextMessage(
            from,
            `Gen Alpha ${PLAN_LABELS[plan]} fee: Rs ${PLAN_AMOUNTS[plan]}. Pay using UPI: ${linkRequest.payment_link_url}\n\nUPI ID: ${ACADEMY_UPI_ID}`,
          );
        }
      }
    }
  }
  return jsonResponse({ success: true });
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  if (request.method === "GET") {
    const url = new URL(request.url);
    const mode = url.searchParams.get("hub.mode");
    const token = url.searchParams.get("hub.verify_token");
    const challenge = url.searchParams.get("hub.challenge");
    if (mode === "subscribe" && token === env("WHATSAPP_WEBHOOK_VERIFY_TOKEN")) {
      return new Response(challenge || "", { headers: corsHeaders });
    }
    return jsonResponse({ error: "Webhook verification failed." }, 403);
  }

  if (request.method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);

  try {
    const payload = await request.json();
    if (payload?.action === "send_reminder") return await handleSendReminder(request, payload);
    return await handleWebhook(payload);
  } catch (error) {
    return jsonResponse({ error: error instanceof Error ? error.message : "WhatsApp reminder failed." }, 500);
  }
});
