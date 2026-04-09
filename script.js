const SUPABASE_CONFIG = window.GEN_ALPHA_SUPABASE_CONFIG ?? {};

const kidForm = document.getElementById("kidForm");
const kidsList = document.getElementById("kidsList");
const emptyState = document.getElementById("emptyState");
const alertCount = document.getElementById("alertCount");
const alertSummary = document.getElementById("alertSummary");
const feesPaidSelect = document.getElementById("feesPaid");
const amountPaidInput = document.getElementById("amountPaid");
const joinDateInput = document.getElementById("joinDate");
const formMessage = document.getElementById("formMessage");
const saveButton = document.getElementById("saveButton");
const cancelEditButton = document.getElementById("cancelEditButton");
const loginForm = document.getElementById("loginForm");
const loginMessage = document.getElementById("loginMessage");
const managerTools = document.getElementById("managerTools");
const logoutButton = document.getElementById("logoutButton");
const accessMode = document.getElementById("accessMode");
const loginHint = document.getElementById("loginHint");
const editorLock = document.getElementById("editorLock");
const authPanel = document.getElementById("authPanel");
const authToggleButton = document.getElementById("authToggleButton");
const authCloseButton = document.getElementById("authCloseButton");
const authBackdrop = document.getElementById("authBackdrop");
const quickLogoutButton = document.getElementById("quickLogoutButton");
const managerIdentity = document.getElementById("managerIdentity");
const lastLoginHint = document.getElementById("lastLoginHint");
const formPanel = document.getElementById("formPanel");
const recordsHelper = document.getElementById("recordsHelper");
const joinedCount = document.getElementById("joinedCount");
const activeCount = document.getElementById("activeCount");
const paidCount = document.getElementById("paidCount");
const returningCount = document.getElementById("returningCount");
const slotFilters = document.getElementById("slotFilters");
const globalToast = document.getElementById("globalToast");

const TIME_SLOTS = ["6AM", "7:30AM", "4PM", "5:30PM", "7PM"];

const hasSupabaseConfig = Boolean(SUPABASE_CONFIG.url && SUPABASE_CONFIG.anonKey);
const supabaseClient =
  hasSupabaseConfig && window.supabase
    ? window.supabase.createClient(SUPABASE_CONFIG.url, SUPABASE_CONFIG.anonKey)
    : null;
const isBackendReady = Boolean(supabaseClient);
const LAST_EMAIL_STORAGE_KEY = "gen-alpha-last-manager-email";
const LAST_PASSWORD_STORAGE_KEY = "gen-alpha-last-manager-password";

let kids = [];
let isManagerLoggedIn = false;
let editingKidId = null;
let isAuthPanelOpen = false;
let lastManagerEmail = localStorage.getItem(LAST_EMAIL_STORAGE_KEY) ?? "";
let lastManagerPassword = localStorage.getItem(LAST_PASSWORD_STORAGE_KEY) ?? "";
let activeSlotFilter = "";
let toastTimeoutId = null;

const getActiveManagerEmail = () => lastManagerEmail || "manager";

const normalizeKid = (kid) => {
  const renewals = Array.isArray(kid.renewals) ? kid.renewals.filter(Boolean) : [];

  return {
    id: kid.id,
    name: kid.name || "",
    age: Number(kid.age) || 0,
    timeSlot: kid.time_slot || "",
    joinDate: kid.join_date || "",
    feesPaid: kid.fees_paid ? "yes" : "no",
    amountPaid: Number(kid.amount_paid) || 0,
    renewals,
    addedBy: kid.added_by || "Unknown",
    updatedBy: kid.updated_by || kid.added_by || "Unknown",
    discontinued: Boolean(kid.discontinued),
  };
};

const toDatabasePayload = ({
  name,
  age,
  timeSlot,
  joinDate,
  feesPaid,
  amountPaid,
  renewals,
  addedBy,
  updatedBy,
  discontinued,
}) => ({
  name,
  age,
  time_slot: timeSlot,
  join_date: joinDate,
  fees_paid: feesPaid === "yes",
  amount_paid: Number(amountPaid),
  renewals,
  added_by: addedBy,
  updated_by: updatedBy,
  discontinued: Boolean(discontinued),
});

const setJoinDateLimit = () => {
  joinDateInput.max = new Date().toISOString().split("T")[0];
};

const formatDate = (value) =>
  value
    ? new Date(`${value}T00:00:00`).toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      })
    : "Not renewed";

const getReferenceDate = (kid) =>
  kid.renewals.length > 0 ? kid.renewals[kid.renewals.length - 1] : kid.joinDate;

const getDaysSinceDate = (dateValue) => {
  const targetDate = new Date(`${dateValue}T00:00:00`);
  const msPerDay = 1000 * 60 * 60 * 24;
  return Math.floor((new Date() - targetDate) / msPerDay);
};

const getStudentType = (kid) => (kid.renewals.length > 0 ? "Returning" : "New");
const isActiveKid = (kid) => !kid.discontinued;
const isFeesPending = (kid) => isActiveKid(kid) && kid.feesPaid !== "yes";
const isRenewalPending = (kid) =>
  isActiveKid(kid) && getDaysSinceDate(getReferenceDate(kid)) >= 30;
const getFilteredKids = () =>
  !activeSlotFilter
    ? kids
    : activeSlotFilter === "not-set"
      ? kids.filter((kid) => isActiveKid(kid) && !kid.timeSlot)
      : kids.filter((kid) => isActiveKid(kid) && kid.timeSlot === activeSlotFilter);

const updateStats = () => {
  const activeKids = kids.filter(isActiveKid);
  joinedCount.textContent = String(kids.length);
  activeCount.textContent = String(activeKids.length);
  paidCount.textContent = String(activeKids.filter((kid) => kid.feesPaid === "yes").length);
  returningCount.textContent = String(activeKids.filter((kid) => kid.renewals.length > 0).length);
};

const renderSlotFilters = () => {
  const activeKids = kids.filter(isActiveKid);
  const notSetCount = activeKids.filter((kid) => !kid.timeSlot).length;
  const filters = [
    { value: "all", label: "All", count: kids.length },
    ...TIME_SLOTS.map((slot) => ({
      value: slot,
      label: slot,
      count: activeKids.filter((kid) => kid.timeSlot === slot).length,
    })),
  ];

  if (notSetCount > 0) {
    filters.push({
      value: "not-set",
      label: "Not set",
      count: notSetCount,
    });
  } else if (activeSlotFilter === "not-set") {
    activeSlotFilter = "";
  }

  slotFilters.innerHTML = filters
    .map(
      (filter) => `
        <button
          type="button"
          class="slot-chip ${
            (filter.value === "all" && !activeSlotFilter) || filter.value === activeSlotFilter
              ? "active"
              : ""
          }"
          data-slot-filter="${filter.value}"
        >
          <span>${filter.label}</span>
          <strong>${filter.count}</strong>
        </button>
      `
    )
    .join("");
};

const showToast = (message) => {
  if (!globalToast) {
    return;
  }

  if (toastTimeoutId) {
    window.clearTimeout(toastTimeoutId);
  }

  globalToast.textContent = message;
  globalToast.hidden = false;

  toastTimeoutId = window.setTimeout(() => {
    globalToast.hidden = true;
  }, 2800);
};

const syncAmountState = () => {
  const isPaid = feesPaidSelect.value === "yes" && isManagerLoggedIn && isBackendReady;
  amountPaidInput.disabled = !isPaid;

  if (!isPaid) {
    amountPaidInput.value = "0";
  }
};

const resetFormState = () => {
  editingKidId = null;
  kidForm.reset();
  saveButton.textContent = "Save kid details";
  cancelEditButton.hidden = true;
  syncAmountState();
};

const updateAuthPanel = () => {
  authPanel.hidden = !isAuthPanelOpen;
  authBackdrop.hidden = !isAuthPanelOpen;
  authToggleButton.textContent = isManagerLoggedIn ? "Manager Access" : "Manager Login";
  quickLogoutButton.hidden = !isManagerLoggedIn;
  managerIdentity.hidden = !isManagerLoggedIn || !lastManagerEmail;
  managerIdentity.textContent = isManagerLoggedIn ? `Logged in: ${lastManagerEmail}` : "";
  lastLoginHint.hidden = !isManagerLoggedIn || !lastManagerEmail;
  lastLoginHint.textContent = isManagerLoggedIn
    ? `Last used manager email: ${lastManagerEmail}`
    : "";
};

const renderSummary = (alertKids) => {
  const totalAlerts = alertKids.length;

  alertCount.textContent =
    totalAlerts === 1 ? "1 kid needs attention" : `${totalAlerts} kids need attention`;

  if (!isBackendReady) {
    alertSummary.textContent = "Connect Supabase to load academy records.";
    return;
  }

  if (totalAlerts === 0) {
    alertSummary.textContent = "All current join fees and renewals are up to date.";
    return;
  }

  alertSummary.textContent = `Need fees or renewal action for: ${alertKids
    .map((kid) => kid.name)
    .join(", ")}`;
};

const updateAccessUI = () => {
  const canEdit = isBackendReady && isManagerLoggedIn;
  const formControls = kidForm.querySelectorAll("input, select, button");

  if (!hasSupabaseConfig) {
    loginForm.hidden = true;
    managerTools.hidden = true;
    accessMode.textContent = "Setup required";
    loginHint.textContent =
      "Add your Supabase URL and anon key in supabase-config.js, then create a manager user in Supabase Auth.";
    editorLock.hidden = false;
    editorLock.textContent = "Complete Supabase setup before editing academy records.";
  } else if (!isBackendReady) {
    loginForm.hidden = true;
    managerTools.hidden = true;
    accessMode.textContent = "Connection error";
    loginHint.textContent =
      "Supabase config exists, but the browser client did not load. Check the CDN script and redeploy.";
    editorLock.hidden = false;
    editorLock.textContent = "Supabase client failed to load, so editing is unavailable.";
  } else {
    loginForm.hidden = canEdit;
    managerTools.hidden = !canEdit;
    accessMode.textContent = canEdit ? "Manager edit mode" : "View-only mode";
    loginHint.textContent = canEdit
      ? "Manager editing is active on this device."
      : "Sign in with a manager email created in Supabase Auth.";
    editorLock.hidden = canEdit;
    editorLock.textContent = "Login as manager to add or edit academy records.";
  }

  formControls.forEach((control) => {
    control.disabled = !canEdit;
  });

  if (lastManagerEmail) {
    document.getElementById("email").value = lastManagerEmail;
  }

  if (lastManagerPassword) {
    document.getElementById("password").value = lastManagerPassword;
  }

  formPanel.hidden = !canEdit;
  recordsHelper.textContent = canEdit
    ? "Manager editing is enabled. Use the cards below to update, renew, or discontinue players."
    : "Public users can review the register here. Login as manager to edit players.";

  formMessage.textContent = !hasSupabaseConfig
    ? "Supabase connection is required before edits can be made."
    : !isBackendReady
      ? "Supabase client failed to load."
      : canEdit
      ? "Manager access enabled. You can add and update records."
      : "Login is required before any edits can be made.";

  if (!canEdit) {
    resetFormState();
  }

  syncAmountState();
  updateAuthPanel();
};

const renderKids = () => {
  kidsList.innerHTML = "";
  updateStats();
  renderSlotFilters();

  const visibleKids = getFilteredKids();

  if (kids.length === 0) {
    emptyState.hidden = false;
    kidsList.hidden = true;
    emptyState.textContent = isBackendReady
      ? isManagerLoggedIn
        ? "No Gen Alpha players added yet. Use the form above to create the first record."
        : "No Gen Alpha players added yet. Login as manager to create the first record."
      : "No data source is connected yet. Finish the Supabase setup to start storing academy records.";
    renderSummary([]);
    return;
  }

  if (visibleKids.length === 0) {
    emptyState.hidden = false;
    kidsList.hidden = true;
    emptyState.textContent =
      !activeSlotFilter
        ? "No registered players match the current view."
        : activeSlotFilter === "not-set"
          ? "No active kids are currently missing a time slot."
          : `No active kids are currently assigned to the ${activeSlotFilter} slot.`;
    renderSummary(kids.filter((kid) => isFeesPending(kid) || isRenewalPending(kid)));
    return;
  }

  emptyState.hidden = true;
  kidsList.hidden = false;

  const alertKids = [];

  kids.forEach((kid) => {
    if (isFeesPending(kid) || isRenewalPending(kid)) {
      alertKids.push(kid);
    }
  });

  visibleKids.forEach((kid) => {
    const referenceDate = getReferenceDate(kid);
    const daysSinceCycle = getDaysSinceDate(referenceDate);
    const renewalPending = isRenewalPending(kid);
    const feesPending = isFeesPending(kid);
    const needsAttention = feesPending || renewalPending;
    const canRenew = renewalPending && isActiveKid(kid);
    const studentType = getStudentType(kid);
    const latestRenewal = kid.renewals.length > 0 ? kid.renewals[kid.renewals.length - 1] : "";
    const renewalStatus = kid.discontinued
      ? "Tracking paused"
      : renewalPending
        ? `${daysSinceCycle} days passed, renewal pending`
        : kid.renewals.length > 0
          ? `Renewed, next due in ${30 - daysSinceCycle} days`
          : `Not due yet, ${30 - daysSinceCycle} days left`;

    const card = document.createElement("article");
    card.className = `kid-card${needsAttention ? " alert" : ""}`;

    card.innerHTML = `
      <div class="kid-card-top">
        <div class="kid-card-title">
          <h3>${kid.name}</h3>
          <div class="kid-card-meta">
            <span>Age ${kid.age}</span>
            <span class="meta-dot" aria-hidden="true"></span>
            <span>${kid.timeSlot || "Time slot not set"}</span>
          </div>
        </div>

        <div class="kid-card-badges">
          <span class="slot-pill">${kid.timeSlot || "Not set"}</span>
          <span class="state-pill ${kid.discontinued ? "discontinued" : "active"}">
            ${kid.discontinued ? "Discontinued" : "Active"}
          </span>
          <span class="type-pill ${studentType === "Returning" ? "returning" : "new"}">
            ${studentType}
          </span>
        </div>
      </div>

      <div class="kid-data-grid">
        <div class="data-tile">
          <span class="data-label">Join Date</span>
          <span class="data-value">${formatDate(kid.joinDate)}</span>
        </div>
        <div class="data-tile">
          <span class="data-label">Latest Renewal</span>
          <span class="data-value">${formatDate(latestRenewal)}</span>
        </div>
        <div class="data-tile">
          <span class="data-label">Fees</span>
          <span class="data-value">
            <span class="status-pill ${feesPending ? "status-unpaid" : "status-paid"}">
              ${feesPending ? "Not paid" : "Paid"}
            </span>
          </span>
        </div>
        <div class="data-tile">
          <span class="data-label">Amount Paid</span>
          <span class="data-value">Rs ${Number(kid.amountPaid).toFixed(2)}</span>
        </div>
      </div>

      <div class="kid-renewal">
        <span class="alert-pill ${renewalPending ? "" : "safe"}">${renewalStatus}</span>
        <p class="kid-renewal-text">
          ${
            kid.discontinued
              ? "Removed from active renewal tracking."
              : `Tracking from ${formatDate(referenceDate)}. ${kid.renewals.length} renewal${
                  kid.renewals.length === 1 ? "" : "s"
                } recorded.`
          }
        </p>
      </div>

      <div class="kid-card-footer">
        <span class="meta-text">Last updated by ${kid.updatedBy}</span>
        ${
          isManagerLoggedIn
            ? `
          <div class="kid-card-actions">
            <button class="secondary-btn" data-action="edit" data-id="${kid.id}" type="button">
              Edit
            </button>
            <button class="secondary-btn" data-action="toggle-status" data-id="${kid.id}" type="button">
              ${kid.discontinued ? "Mark active" : "Discontinue"}
            </button>
            ${
              canRenew
                ? `
              <button class="renew-btn" data-action="renew" data-id="${kid.id}" type="button">
                Mark renewal paid
              </button>
            `
                : `
              <span class="action-note">${
                kid.discontinued
                  ? "Renewal tracking paused"
                  : `Renew in ${30 - daysSinceCycle} day${30 - daysSinceCycle === 1 ? "" : "s"}`
              }</span>
            `
            }
            <button class="danger-btn" data-action="delete" data-id="${kid.id}" type="button">
              Delete
            </button>
          </div>
        `
            : ""
        }
      </div>
    `;

    kidsList.appendChild(card);
  });

  renderSummary(alertKids);
};

const refreshSession = async () => {
  if (!isBackendReady) {
    isManagerLoggedIn = false;
    updateAccessUI();
    return;
  }

  const {
    data: { session },
    error,
  } = await supabaseClient.auth.getSession();

  if (error) {
    loginMessage.textContent = error.message;
  }

  if (session?.user?.email) {
    lastManagerEmail = session.user.email;
    localStorage.setItem(LAST_EMAIL_STORAGE_KEY, lastManagerEmail);
  }

  isManagerLoggedIn = Boolean(session);
  updateAccessUI();
};

const loadKids = async () => {
  if (!isBackendReady) {
    kids = [];
    renderKids();
    return;
  }

  const { data, error } = await supabaseClient
    .from("students")
    .select("*")
    .order("join_date", { ascending: false });

  if (error) {
    kids = [];
    emptyState.hidden = false;
    kidsList.hidden = true;
    emptyState.textContent = `Supabase error: ${error.message}`;
    alertCount.textContent = "0 kids need attention";
    alertSummary.textContent = "Fix the Supabase setup to load academy records.";
    return;
  }

  kids = data.map(normalizeKid);
  renderKids();
};

const initializeAuthListener = () => {
  if (!isBackendReady) {
    return;
  }

  supabaseClient.auth.onAuthStateChange((_event, session) => {
    setTimeout(() => {
      if (session?.user?.email) {
        lastManagerEmail = session.user.email;
        localStorage.setItem(LAST_EMAIL_STORAGE_KEY, lastManagerEmail);
      }

      isManagerLoggedIn = Boolean(session);
      updateAccessUI();
      renderKids();
    }, 0);
  });
};

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!isBackendReady) {
    loginMessage.textContent = "Supabase is not configured yet.";
    return;
  }

  const formData = new FormData(loginForm);
  const email = formData.get("email").toString().trim();
  const password = formData.get("password").toString();

  const { error } = await supabaseClient.auth.signInWithPassword({
    email,
    password,
  });

  if (error) {
    loginMessage.textContent = error.message;
    return;
  }

  lastManagerEmail = email;
  lastManagerPassword = password;
  localStorage.setItem(LAST_EMAIL_STORAGE_KEY, lastManagerEmail);
  localStorage.setItem(LAST_PASSWORD_STORAGE_KEY, lastManagerPassword);
  loginForm.reset();
  loginMessage.textContent = "";
  isAuthPanelOpen = false;
  await refreshSession();
  await loadKids();
});

const handleLogout = async () => {
  if (!isBackendReady) {
    return;
  }

  const { error } = await supabaseClient.auth.signOut();

  if (error) {
    loginMessage.textContent = error.message;
    return;
  }

  loginMessage.textContent = "";
  isAuthPanelOpen = false;
  await refreshSession();
  renderKids();
};

logoutButton.addEventListener("click", handleLogout);
quickLogoutButton.addEventListener("click", handleLogout);

kidForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!isBackendReady || !isManagerLoggedIn) {
    formMessage.textContent = "Login as manager after connecting Supabase to edit records.";
    return;
  }

  const formData = new FormData(kidForm);
  const payload = {
    name: formData.get("name").toString().trim(),
    age: Number(formData.get("age")),
    timeSlot: formData.get("timeSlot").toString(),
    joinDate: formData.get("joinDate").toString(),
    feesPaid: formData.get("feesPaid").toString(),
    amountPaid: Number(formData.get("amountPaid")),
    renewals: [],
    addedBy: getActiveManagerEmail(),
    updatedBy: getActiveManagerEmail(),
    discontinued: false,
  };

  if (!payload.name || !payload.joinDate || !payload.timeSlot) {
    formMessage.textContent = "Please complete all required fields.";
    return;
  }

  if (new Date(payload.joinDate) > new Date()) {
    formMessage.textContent = "Join date cannot be in the future.";
    return;
  }

  const wasEditing = Boolean(editingKidId);
  let error = null;

  if (wasEditing) {
    const currentKid = kids.find((kid) => kid.id === editingKidId);

    ({ error } = await supabaseClient
      .from("students")
      .update(
        toDatabasePayload({
          ...payload,
          renewals: currentKid ? currentKid.renewals : [],
          addedBy: currentKid ? currentKid.addedBy : getActiveManagerEmail(),
          updatedBy: getActiveManagerEmail(),
          discontinued: currentKid ? currentKid.discontinued : false,
        })
      )
      .eq("id", editingKidId));
  } else {
    ({ error } = await supabaseClient.from("students").insert(toDatabasePayload(payload)));
  }

  if (error) {
    formMessage.textContent = error.message;
    return;
  }

  resetFormState();
  formMessage.textContent = wasEditing
    ? "Gen Alpha player record updated successfully."
    : "Gen Alpha player record saved successfully.";
  await loadKids();
});

kidsList.addEventListener("click", async (event) => {
  const target = event.target;

  if (!(target instanceof HTMLButtonElement) || !isBackendReady || !isManagerLoggedIn) {
    return;
  }

  const { id, action } = target.dataset;

  if (action === "edit") {
    const kidToEdit = kids.find((kid) => kid.id === id);

    if (!kidToEdit) {
      return;
    }

    editingKidId = kidToEdit.id;
    document.getElementById("name").value = kidToEdit.name;
    document.getElementById("age").value = String(kidToEdit.age);
    document.getElementById("timeSlot").value = kidToEdit.timeSlot;
    joinDateInput.value = kidToEdit.joinDate;
    feesPaidSelect.value = kidToEdit.feesPaid;
    amountPaidInput.value = String(kidToEdit.amountPaid);
    saveButton.textContent = "Save changes";
    cancelEditButton.hidden = false;
    syncAmountState();
    formMessage.textContent = `Editing ${kidToEdit.name}. Save changes when ready.`;
    window.scrollTo({ top: kidForm.offsetTop - 40, behavior: "smooth" });
    return;
  }

  if (action === "delete") {
    const kidToDelete = kids.find((kid) => kid.id === id);
    const { error } = await supabaseClient.from("students").delete().eq("id", id);

    if (error) {
      formMessage.textContent = error.message;
      return;
    }

    if (editingKidId === id) {
      resetFormState();
      formMessage.textContent = "Editing record was deleted.";
    }

    await loadKids();
    if (kidToDelete) {
      showToast(`Player ${kidToDelete.name} deleted`);
    }
    return;
  }

  if (action === "renew") {
    const kidToRenew = kids.find((kid) => kid.id === id);

    if (!kidToRenew) {
      return;
    }

    if (getDaysSinceDate(getReferenceDate(kidToRenew)) < 30) {
      formMessage.textContent = "This student is not due for renewal yet.";
      return;
    }

    const renewals = [...kidToRenew.renewals, new Date().toISOString().split("T")[0]];
    const { error } = await supabaseClient
      .from("students")
      .update({
        renewals,
        updated_by: getActiveManagerEmail(),
      })
      .eq("id", id);

    if (error) {
      formMessage.textContent = error.message;
      return;
    }

    formMessage.textContent = `${kidToRenew.name} marked as renewed for the next 30-day cycle.`;
    await loadKids();
    return;
  }

  if (action === "toggle-status") {
    const kidToUpdate = kids.find((kid) => kid.id === id);

    if (!kidToUpdate) {
      return;
    }

    const { error } = await supabaseClient
      .from("students")
      .update({
        discontinued: !kidToUpdate.discontinued,
        updated_by: getActiveManagerEmail(),
      })
      .eq("id", id);

    if (error) {
      formMessage.textContent = error.message;
      return;
    }

    formMessage.textContent = kidToUpdate.discontinued
      ? `${kidToUpdate.name} marked as active again.`
      : `${kidToUpdate.name} marked as discontinued.`;
    await loadKids();
  }
});

cancelEditButton.addEventListener("click", () => {
  resetFormState();
  formMessage.textContent = "Edit cancelled.";
});

authToggleButton.addEventListener("click", () => {
  isAuthPanelOpen = !isAuthPanelOpen;
  updateAuthPanel();
});

authCloseButton.addEventListener("click", () => {
  isAuthPanelOpen = false;
  updateAuthPanel();
});

authBackdrop.addEventListener("click", () => {
  isAuthPanelOpen = false;
  updateAuthPanel();
});

feesPaidSelect.addEventListener("change", syncAmountState);
slotFilters.addEventListener("click", (event) => {
  if (!(event.target instanceof Element)) {
    return;
  }

  const target = event.target.closest("[data-slot-filter]");

  if (!(target instanceof HTMLButtonElement)) {
    return;
  }

  const nextFilter = target.dataset.slotFilter || "";
  activeSlotFilter =
    nextFilter === "all" || nextFilter === activeSlotFilter ? "" : nextFilter;
  renderKids();
});

const initializeApp = async () => {
  setJoinDateLimit();
  updateAccessUI();
  renderKids();

  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("./sw.js").catch(() => {});
    });
  }

  if (!isBackendReady) {
    return;
  }

  initializeAuthListener();
  await refreshSession();
  await loadKids();
};

initializeApp();
