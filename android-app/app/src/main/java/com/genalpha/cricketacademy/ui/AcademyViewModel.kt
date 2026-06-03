package com.genalpha.cricketacademy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genalpha.cricketacademy.data.DashboardStats
import com.genalpha.cricketacademy.data.AdmissionDraft
import com.genalpha.cricketacademy.data.ManagerSession
import com.genalpha.cricketacademy.data.OperationResult
import com.genalpha.cricketacademy.data.PaymentFollowUp
import com.genalpha.cricketacademy.data.PendingAdmission
import com.genalpha.cricketacademy.data.ReminderSettings
import com.genalpha.cricketacademy.data.SessionPrefs
import com.genalpha.cricketacademy.data.SlotSummary
import com.genalpha.cricketacademy.data.Student
import com.genalpha.cricketacademy.data.StudentDraft
import com.genalpha.cricketacademy.data.StudentRealtimeListener
import com.genalpha.cricketacademy.data.StudentTimelineItem
import com.genalpha.cricketacademy.data.SupabaseException
import com.genalpha.cricketacademy.data.SupabaseRepository
import com.genalpha.cricketacademy.data.AcademyExpense
import com.genalpha.cricketacademy.data.StudentPayment
import com.genalpha.cricketacademy.data.addMonthsForPlan
import com.genalpha.cricketacademy.data.buildSlotSummary
import com.genalpha.cricketacademy.data.buildStats
import com.genalpha.cricketacademy.data.calculateAgeFromDate
import com.genalpha.cricketacademy.data.daysBetweenIso
import com.genalpha.cricketacademy.data.isFutureDate
import com.genalpha.cricketacademy.data.isActive
import com.genalpha.cricketacademy.data.isPaymentPendingVerification
import com.genalpha.cricketacademy.data.isFeesPending
import com.genalpha.cricketacademy.data.isRenewalPending
import com.genalpha.cricketacademy.data.todayIsoDate
import com.genalpha.cricketacademy.data.nextRenewalCycleDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val TIME_SLOTS = listOf("6AM", "7:30AM", "4PM", "5:30PM", "7PM")
private const val JERSEY_PAIR_REVENUE = 750.0

private fun chargeableJerseyPairCount(pairCount: Int): Int =
    pairCount.coerceAtLeast(0)

data class AcademyUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isAuthLoading: Boolean = false,
    val isAttendanceRefreshing: Boolean = false,
    val kids: List<Student> = emptyList(),
    val todayAttendanceIds: Set<String> = emptySet(),
    val attendanceCounts: Map<String, Int> = emptyMap(),
    val recentAttendanceDates: Map<String, List<String>> = emptyMap(),
    val pendingAdmissions: List<PendingAdmission> = emptyList(),
    val isAdmissionReviewLoading: Boolean = false,
    val expenses: List<AcademyExpense> = emptyList(),
    val payments: List<StudentPayment> = emptyList(),
    val paymentFollowUps: List<PaymentFollowUp> = emptyList(),
    val isFinanceLoading: Boolean = false,
    val selectedSlotFilter: String = "",
    val searchQuery: String = "",
    val rosterSortKey: String = "nextDue",
    val rosterSortAscending: Boolean = true,
    val rosterStatusFilter: String = "active",
    val rosterJerseyFilter: String = "all",
    val rosterTypeFilter: String = "all",
    val rosterFeePaidFilter: String = "all",
    val rosterFeeDueFilter: String = "all",
    val darkModeEnabled: Boolean = false,
    val session: ManagerSession? = null,
    val lastEmail: String = "",
    val lastPassword: String = "",
    val errorMessage: String? = null,
)

class AcademyViewModel(
    private val repository: SupabaseRepository,
    private val sessionPrefs: SessionPrefs,
) : ViewModel() {
    private var playersLiveSyncJob: Job? = null
    private var attendanceLiveSyncJob: Job? = null

    private val realtimeListener = object : StudentRealtimeListener {
        override fun onStudentUpsert(student: Student) {
            _uiState.update { state ->
                state.copy(kids = sortStudents(state.kids.filterNot { it.id == student.id } + student))
            }
        }

        override fun onStudentDeleted(studentId: String) {
            _uiState.update { state ->
                state.copy(kids = sortStudents(state.kids.filterNot { it.id == studentId }))
            }
        }

        override fun onAttendanceUpsert(studentId: String, attendanceDate: String) {
            _uiState.update { state ->
                val currentDates = state.recentAttendanceDates[studentId].orEmpty()
                val nextRecentDates = state.recentAttendanceDates + (studentId to ((currentDates + attendanceDate).distinct().sortedDescending()))
                if (attendanceDate != todayIsoDate()) return@update state.copy(recentAttendanceDates = nextRecentDates)
                if (state.todayAttendanceIds.contains(studentId)) return@update state.copy(recentAttendanceDates = nextRecentDates)
                val currentCount = state.attendanceCounts[studentId] ?: 0
                state.copy(
                    todayAttendanceIds = state.todayAttendanceIds + studentId,
                    attendanceCounts = state.attendanceCounts + (studentId to (currentCount + 1)),
                    recentAttendanceDates = nextRecentDates,
                )
            }
        }

        override fun onAttendanceDeleted(studentId: String, attendanceDate: String) {
            _uiState.update { state ->
                val nextRecentDates = state.recentAttendanceDates + (studentId to state.recentAttendanceDates[studentId].orEmpty().filterNot { it == attendanceDate })
                if (attendanceDate != todayIsoDate()) return@update state.copy(recentAttendanceDates = nextRecentDates)
                if (!state.todayAttendanceIds.contains(studentId)) return@update state.copy(recentAttendanceDates = nextRecentDates)
                val currentCount = state.attendanceCounts[studentId] ?: 0
                state.copy(
                    todayAttendanceIds = state.todayAttendanceIds - studentId,
                    attendanceCounts = state.attendanceCounts + (studentId to (currentCount - 1).coerceAtLeast(0)),
                    recentAttendanceDates = nextRecentDates,
                )
            }
        }

        override fun onExpenseUpsert(expense: AcademyExpense) {
            upsertLocalExpense(expense)
        }

        override fun onExpenseDeleted(expenseId: String) {
            _uiState.update { state ->
                state.copy(expenses = state.expenses.filterNot { it.id == expenseId })
            }
        }

        override fun onPaymentUpsert(payment: StudentPayment) {
            upsertLocalPayment(payment)
        }

        override fun onPaymentDeleted(paymentId: String) {
            _uiState.update { state ->
                state.copy(payments = state.payments.filterNot { it.id == paymentId })
            }
        }

        override fun onReminderPaymentChanged() {
            viewModelScope.launch {
                loadFinance()
            }
        }
    }

    private val _uiState = MutableStateFlow(
        AcademyUiState(
            lastEmail = sessionPrefs.loadSavedEmail(),
            lastPassword = sessionPrefs.loadSavedPassword(),
            darkModeEnabled = sessionPrefs.loadDarkModeEnabled(),
            session = null,
        )
    )
    val uiState: StateFlow<AcademyUiState> = _uiState.asStateFlow()

    init {
        sessionPrefs.clearSession()
        viewModelScope.launch {
            refreshSessionIfPossible()
            loadKids()
            loadTodayAttendance()
            loadAttendanceCounts()
            loadRecentAttendanceDates()
            loadFinance()
            startPlayersLiveSync()
            startAttendanceLiveSync()
        }
    }

    override fun onCleared() {
        repository.stopStudentRealtime()
        playersLiveSyncJob?.cancel()
        attendanceLiveSyncJob?.cancel()
        super.onCleared()
    }

    fun setSlotFilter(filter: String) {
        _uiState.update { current ->
            current.copy(
                selectedSlotFilter = when {
                    filter == "all" -> ""
                    current.selectedSlotFilter == filter -> ""
                    else -> filter
                }
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setRosterStatusFilter(filter: String) {
        _uiState.update { it.copy(rosterStatusFilter = filter.ifBlank { "all" }) }
    }

    fun setRosterJerseyFilter(filter: String) {
        _uiState.update { it.copy(rosterJerseyFilter = filter.ifBlank { "all" }) }
    }

    fun setRosterTypeFilter(filter: String) {
        _uiState.update { it.copy(rosterTypeFilter = filter.ifBlank { "all" }) }
    }

    fun setRosterFeePaidFilter(filter: String) {
        _uiState.update { it.copy(rosterFeePaidFilter = filter.ifBlank { "all" }) }
    }

    fun setRosterFeeDueFilter(filter: String) {
        _uiState.update { it.copy(rosterFeeDueFilter = filter.ifBlank { "all" }) }
    }

    fun setRosterSort(key: String) {
        _uiState.update { state ->
            if (state.rosterSortKey == key) {
                state.copy(rosterSortAscending = !state.rosterSortAscending)
            } else {
                state.copy(
                    rosterSortKey = key,
                    rosterSortAscending = key in setOf("name", "slot", "status"),
                )
            }
        }
    }

    fun toggleDarkMode() {
        val enabled = !_uiState.value.darkModeEnabled
        sessionPrefs.saveDarkModeEnabled(enabled)
        _uiState.update { it.copy(darkModeEnabled = enabled) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun AcademyUiState.hasRosterDetailFilters(): Boolean {
        return rosterStatusFilter != "all" ||
            rosterJerseyFilter != "all" ||
            rosterTypeFilter != "all" ||
            rosterFeePaidFilter != "all" ||
            rosterFeeDueFilter != "all"
    }

    fun onAppForegrounded() {
        repository.startStudentRealtime(realtimeListener, _uiState.value.session)
        startPlayersLiveSync()
        startAttendanceLiveSync()
        viewModelScope.launch {
            refreshSessionIfPossible()
            repository.startStudentRealtime(realtimeListener, _uiState.value.session)
            loadKids()
            loadTodayAttendance()
            loadAttendanceCounts()
            loadRecentAttendanceDates()
            loadFinance()
        }
    }

    fun onAppBackgrounded() {
        repository.pauseStudentRealtime()
        playersLiveSyncJob?.cancel()
        playersLiveSyncJob = null
        attendanceLiveSyncJob?.cancel()
        attendanceLiveSyncJob = null
    }

    suspend fun login(email: String, password: String): OperationResult {
        if (email.isBlank() || password.isBlank()) {
            return OperationResult(false, "Enter manager email and password.")
        }

        _uiState.update { it.copy(isAuthLoading = true) }
        return try {
            val session = repository.signIn(email.trim(), password)
            sessionPrefs.saveRememberedCredentials(email.trim(), password)
            repository.startStudentRealtime(realtimeListener, session)
            _uiState.update {
                it.copy(
                    isAuthLoading = false,
                    session = session,
                    lastEmail = email.trim(),
                    lastPassword = password,
                    errorMessage = null,
                )
            }
            refreshAdmissionReviewInBackground()
            OperationResult(true, "Manager login successful.")
        } catch (error: Exception) {
            _uiState.update { it.copy(isAuthLoading = false) }
            OperationResult(false, error.message ?: "Unable to login.")
        }
    }

    fun logout() {
        sessionPrefs.clearSession()
        repository.startStudentRealtime(realtimeListener, null)
        _uiState.update { it.copy(session = null, pendingAdmissions = emptyList(), isAdmissionReviewLoading = false) }
    }

    suspend fun peekNextAdmissionRegNo(): Long = repository.peekNextAdmissionRegNo()

    suspend fun loadTodayAttendance() {
        _uiState.update { it.copy(isAttendanceRefreshing = true) }
        try {
            val attendanceIds = repository.fetchTodayAttendance()
            _uiState.update {
                it.copy(
                    todayAttendanceIds = attendanceIds,
                    isAttendanceRefreshing = false,
                )
            }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isAttendanceRefreshing = false,
                    errorMessage = error.message ?: "Unable to load today's attendance.",
                )
            }
        }
    }

    suspend fun loadFinance() {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update {
                it.copy(
                    expenses = emptyList(),
                    payments = emptyList(),
                    paymentFollowUps = emptyList(),
                    isFinanceLoading = false,
                )
            }
            return
        }

        _uiState.update { it.copy(isFinanceLoading = true) }
        try {
            val fetchedExpenses = repository.fetchExpenses(session.accessToken)
            val fetchedPayments = repository.fetchPayments(session.accessToken)
            val fetchedFollowUps = repository.fetchPaymentFollowUps(session.accessToken)

            _uiState.update {
                it.copy(
                    expenses = fetchedExpenses,
                    payments = fetchedPayments,
                    paymentFollowUps = fetchedFollowUps,
                    isFinanceLoading = false,
                )
            }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isFinanceLoading = false,
                    errorMessage = error.message ?: "Unable to load finance data.",
                )
            }
        }
    }

    suspend fun addExpense(
        expenseType: String,
        amountText: String,
        paidBy: String,
        comment: String,
        expenseDate: String = "",
    ): OperationResult {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (expenseType.isBlank()) return OperationResult(false, "Choose an expense type.")
        if (amount <= 0.0) return OperationResult(false, "Enter a valid expense amount.")

        return try {
            val created = withFreshSession { session ->
                repository.addExpense(session, expenseType, amount, paidBy, comment.trim(), expenseDate)
            }
            upsertLocalExpense(created)
            OperationResult(true, "Expense added.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to add expense.")
        }
    }

    suspend fun deleteExpense(expenseId: String): OperationResult {
        if (expenseId.isBlank()) return OperationResult(false, "Expense record not found.")

        return try {
            withFreshSession { session ->
                repository.deleteExpense(expenseId, session)
            }
            _uiState.update { state ->
                state.copy(expenses = state.expenses.filterNot { it.id == expenseId })
            }
            OperationResult(true, "Expense deleted.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to delete expense.")
        }
    }

    suspend fun deletePayment(paymentId: String, student: Student): OperationResult {
        if (paymentId.isBlank()) return OperationResult(false, "Payment record not found.")

        return try {
            withFreshSession { session ->
                repository.rollbackPayment(paymentId, student, session)
            }
            _uiState.update { state ->
                state.copy(payments = state.payments.filterNot { it.id == paymentId })
            }
            // Trigger a student refresh to get the updated renewals array
            loadKids()
            OperationResult(true, "Payment deleted and renewal date rolled back.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to delete payment.")
        }
    }

    suspend fun loadAttendanceCounts() {
        try {
            val attendanceCounts = repository.fetchAttendanceCounts()
            _uiState.update { it.copy(attendanceCounts = attendanceCounts) }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = error.message ?: "Unable to load attendance summary.",
                )
            }
        }
    }

    suspend fun loadRecentAttendanceDates() {
        try {
            val since = LocalDate.now().minusDays(120).toString()
            val recentDates = repository.fetchRecentAttendanceDates(since)
            _uiState.update { it.copy(recentAttendanceDates = recentDates) }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = error.message ?: "Unable to load attendance follow-up nudges.",
                )
            }
        }
    }

    suspend fun attendanceHistory(studentId: String): List<String> {
        return repository.fetchAttendanceDates(studentId)
    }

    suspend fun studentTimeline(studentId: String): List<StudentTimelineItem> {
        val session = _uiState.value.session
        val timeline = repository.fetchStudentTimeline(studentId, session?.accessToken)
        if (session == null) return timeline
        return timeline.compactPlayerTimeline().map { item ->
            val proofPath = paymentProofPath(item.details.orEmpty())
            if (proofPath.isBlank()) {
                item
            } else {
                item.copy(proofUrl = repository.createPaymentProofSignedUrl(session.accessToken, proofPath))
            }
        }
    }

    private fun List<StudentTimelineItem>.compactPlayerTimeline(): List<StudentTimelineItem> {
        val seen = mutableSetOf<String>()
        return mapNotNull { it.compactTimelineItem() }
            .filter { item ->
                val title = item.title.ifBlank { item.eventType }
                val dateKey = item.eventDate.ifBlank { item.createdAt.orEmpty().take(10) }
                val detailKey = if (title == "Reminder failed") item.details.orEmpty() else ""
                seen.add("$dateKey|$title|$detailKey")
            }
    }

    private fun StudentTimelineItem.compactTimelineItem(): StudentTimelineItem? {
        val eventText = "$eventType $title ${details.orEmpty()}".lowercase()
        return when {
            "renewal reminder prepared" in eventText || "joining fee reminder prepared" in eventText -> null
            "reminder accepted" in eventText || " accepted " in eventText -> null
            "confirmation" in eventText && "failed" !in eventText && "delivered" !in eventText && "read" !in eventText -> null
            "retry scheduled" in eventText -> null
            "whatsapp reminder prepared" in eventText || "status: queued" in eventText -> copy(
                title = "Fee reminder prepared",
                details = "",
                changedBy = changedBy.orEmpty().ifBlank { "System" },
            )
            "failed" in eventText || "send_failed" in eventText || "delivery_failed" in eventText -> copy(
                title = title.takeIf { it != "Reminder failed" } ?: "Fee reminder failed to parent",
                details = extractReminderTimelineReason(details.orEmpty()).ifBlank { "Provider did not return a detailed reason." },
                changedBy = changedBy.orEmpty().ifBlank { "WhatsApp" },
            )
            "read" in eventText -> copy(
                title = title.ifBlank { "WhatsApp follow-up read by parent" },
                details = details.orEmpty(),
                changedBy = changedBy.orEmpty().ifBlank { "WhatsApp" },
            )
            "delivered" in eventText -> copy(
                title = title.ifBlank { "WhatsApp follow-up delivered to parent" },
                details = details.orEmpty(),
                changedBy = changedBy.orEmpty().ifBlank { "WhatsApp" },
            )
            "message: template" in eventText -> copy(details = "")
            else -> this
        }
    }

    private fun extractReminderTimelineReason(details: String): String {
        val marker = "Reminder failed:"
        if (details.contains(marker)) return details.substringAfter(marker).trim()
        return details
            .split("•")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .asReversed()
            .firstOrNull { piece ->
                val lower = piece.lowercase()
                lower !in setOf("failed", "send_failed", "delivery_failed", "undelivered", "accepted", "sent", "delivered", "read") &&
                    !lower.startsWith("message:") &&
                    !lower.startsWith("parent:") &&
                    !lower.startsWith("from:") &&
                    !lower.startsWith("to:") &&
                    !lower.startsWith("plan:") &&
                    !lower.startsWith("amount:") &&
                    !lower.startsWith("months:")
            }
            .orEmpty()
    }

    suspend fun markAttendance(student: Student): OperationResult {
        if (!student.isActive()) {
            return OperationResult(false, "Only active players can mark attendance.")
        }
        if (_uiState.value.todayAttendanceIds.contains(student.id)) {
            return OperationResult(true, "${student.name} is already marked present.")
        }

        return try {
            repository.markTodayAttendance(student.id)
            _uiState.update {
                val currentDates = it.recentAttendanceDates[student.id].orEmpty()
                val today = todayIsoDate()
                it.copy(
                    todayAttendanceIds = it.todayAttendanceIds + student.id,
                    attendanceCounts = it.attendanceCounts + (student.id to ((it.attendanceCounts[student.id] ?: 0) + 1)),
                    recentAttendanceDates = it.recentAttendanceDates + (student.id to ((currentDates + today).distinct().sortedDescending())),
                )
            }
            OperationResult(true, "${student.name} marked present.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to mark attendance.")
        }
    }

    suspend fun unmarkAttendance(student: Student): OperationResult {
        if (!_uiState.value.todayAttendanceIds.contains(student.id)) {
            return OperationResult(false, "${student.name} is not marked present today.")
        }

        return try {
            repository.unmarkTodayAttendance(student.id)
            _uiState.update {
                val currentCount = it.attendanceCounts[student.id] ?: 0
                val today = todayIsoDate()
                val nextDates = it.recentAttendanceDates[student.id].orEmpty().filterNot { date -> date == today }
                it.copy(
                    todayAttendanceIds = it.todayAttendanceIds - student.id,
                    attendanceCounts = it.attendanceCounts + (student.id to (currentCount - 1).coerceAtLeast(0)),
                    recentAttendanceDates = it.recentAttendanceDates + (student.id to nextDates),
                )
            }
            OperationResult(true, "${student.name} attendance reverted.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to revert attendance.")
        }
    }

    suspend fun submitAdmission(draft: AdmissionDraft): OperationResult {
        val validation = validateAdmissionDraft(draft)
        if (validation != null) {
            return OperationResult(false, validation)
        }

        return try {
            val created = repository.submitAdmission(draft)
            refreshAdmissionReviewInBackground()
            OperationResult(true, "Admission submitted for review. Reg No ${created.regNo}")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to submit admission form.")
        }
    }

    suspend fun loadPendingAdmissions() {
        val session = _uiState.value.session ?: run {
            _uiState.update { it.copy(pendingAdmissions = emptyList(), isAdmissionReviewLoading = false) }
            return
        }
        _uiState.update { it.copy(isAdmissionReviewLoading = true) }
        try {
            val admissions = repository.fetchPendingAdmissions(session)
            _uiState.update {
                it.copy(
                    pendingAdmissions = admissions,
                    isAdmissionReviewLoading = false,
                    errorMessage = null,
                )
            }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isAdmissionReviewLoading = false,
                    errorMessage = error.message ?: "Unable to load admission review queue.",
                )
            }
        }
    }

    suspend fun approveAdmission(admission: PendingAdmission): OperationResult {
        return try {
            val session = withFreshSession { session ->
                repository.approveAdmission(admission.id, session.email, session)
                session
            }
            _uiState.update { state ->
                state.copy(pendingAdmissions = state.pendingAdmissions.filterNot { it.id == admission.id })
            }
            refreshInBackground()
            refreshAdmissionReviewInBackground()
            OperationResult(true, "${admission.applicantName} approved by ${session.email}.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to approve admission.")
        }
    }

    suspend fun rejectAdmission(admission: PendingAdmission): OperationResult {
        return try {
            withFreshSession { session ->
                repository.rejectAdmission(admission.id, session.email, session)
            }
            _uiState.update { state ->
                state.copy(pendingAdmissions = state.pendingAdmissions.filterNot { it.id == admission.id })
            }
            refreshAdmissionReviewInBackground()
            OperationResult(true, "${admission.applicantName} rejected.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to reject admission.")
        }
    }

    suspend fun sendAdmissionReminder(admission: PendingAdmission): OperationResult {
        val session = _uiState.value.session ?: return OperationResult(false, "Login to send reminders.")
        return try {
            repository.sendAdmissionReminder(admission.id, session)
            OperationResult(true, "Reminder sent to ${admission.applicantName}'s parent.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to send admission reminder.")
        }
    }

    suspend fun saveStudent(draft: StudentDraft, editingStudent: Student?): OperationResult {
        val validation = validateDraft(draft)
        if (validation != null) {
            return OperationResult(false, validation)
        }

        return try {
            var profileFieldsSaved = true
            var jerseyPayment: StudentPayment? = null
            val session = withFreshSession { session ->
                profileFieldsSaved = if (editingStudent == null) {
                    repository.createStudent(draft, session.email, session)
                } else {
                    repository.updateStudent(editingStudent, draft, session.email, session)
                }
                if (editingStudent != null) {
                    val nextPairs = draft.jerseyPairs.toIntOrNull() ?: 0
                    if (nextPairs != editingStudent.jerseyPairs) {
                        val previousJerseyAmount = editingStudent.jerseyAmount.coerceAtLeast(0.0)
                        val nextJerseyAmount = draft.jerseyAmount.toDoubleOrNull()?.coerceAtLeast(0.0)
                            ?: (nextPairs.coerceAtLeast(0) * JERSEY_PAIR_REVENUE)
                        jerseyPayment = repository.recordJerseyPairAdjustment(
                            student = editingStudent,
                            managerEmail = session.email,
                            session = session,
                            nextPairs = nextPairs,
                            adjustmentAmount = kotlin.math.abs(nextJerseyAmount - previousJerseyAmount),
                            patchStudent = false,
                        )
                    }
                }
                session
            }
            if (editingStudent == null) {
                refreshInBackground()
            } else {
                upsertLocalStudent(
                    editingStudent.copy(
                        name = draft.name.trim(),
                        age = draft.age.toInt(),
                        timeSlot = draft.timeSlot,
                        joinDate = draft.joinDate,
                        feesPaid = draft.feesPaid,
                        amountPaid = draft.amountPaid.toDoubleOrNull() ?: 0.0,
                        feePlan = draft.feePlan.ifBlank { "monthly" },
                        coachingFee = draft.coachingFee.toDoubleOrNull() ?: 0.0,
                        admissionFee = draft.admissionFee.toDoubleOrNull() ?: 0.0,
                        jerseyAmount = draft.jerseyAmount.toDoubleOrNull() ?: 0.0,
                        totalFeeAmount = draft.totalFeeAmount.toDoubleOrNull() ?: 0.0,
                        jerseySize = draft.jerseySize,
                        jerseyPairs = draft.jerseyPairs.toIntOrNull() ?: 0,
                        fatherGuardianName = if (profileFieldsSaved) draft.fatherGuardianName.trim() else editingStudent.fatherGuardianName,
                        parentContactNo = if (profileFieldsSaved) draft.parentContactNo.filter(Char::isDigit).take(10) else editingStudent.parentContactNo,
                        alternateContactNo = if (profileFieldsSaved) draft.alternateContactNo.filter(Char::isDigit).take(10) else editingStudent.alternateContactNo,
                        schoolCollege = if (profileFieldsSaved) draft.schoolCollege.trim() else editingStudent.schoolCollege,
                        grade = if (profileFieldsSaved) draft.grade.trim() else editingStudent.grade,
                        address = if (profileFieldsSaved) draft.address.trim() else editingStudent.address,
                        updatedBy = session.email,
                        discontinuedAt = editingStudent.discontinuedAt,
                    )
                )
                jerseyPayment?.let { upsertLocalPayment(it) }
                if (jerseyPayment != null) refreshFinanceInBackground()
                refreshInBackground()
            }
            OperationResult(
                true,
                if (!profileFieldsSaved) {
                    "Player saved, but parent/school fields need the latest Supabase SQL migration."
                } else if (editingStudent == null) {
                    "Player added successfully."
                } else {
                    "Player updated successfully."
                }
            )
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to save player.")
        }
    }

    suspend fun deleteStudent(student: Student): OperationResult {
        return try {
            withFreshSession { session ->
                repository.deleteStudent(student.id, session)
            }
            removeLocalStudent(student.id)
            refreshInBackground()
            OperationResult(true, "Player ${student.name} deleted")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to delete player.")
        }
    }

    suspend fun updateJerseyPairs(student: Student, nextPairs: Int, adjustmentAmount: Double? = null): OperationResult {
        val safeNextPairs = nextPairs.coerceAtLeast(0)
        val delta = safeNextPairs - student.jerseyPairs.coerceAtLeast(0)
        val chargeableDelta = chargeableJerseyPairCount(safeNextPairs) -
            chargeableJerseyPairCount(student.jerseyPairs)
        if (delta == 0) {
            return OperationResult(true, "Jersey pair count is unchanged.")
        }

        return try {
            val (session, payment) = withFreshSession { session ->
                val insertedPayment = repository.recordJerseyPairAdjustment(
                    student = student,
                    managerEmail = session.email,
                    session = session,
                    nextPairs = safeNextPairs,
                    adjustmentAmount = adjustmentAmount,
                    patchStudent = true,
                )
                session to insertedPayment
            }
            upsertLocalStudent(student.copy(jerseyPairs = safeNextPairs, updatedBy = session.email))
            payment?.let { upsertLocalPayment(it) }
            if (payment != null) refreshFinanceInBackground()
            refreshInBackground()

            val amount = (adjustmentAmount ?: (kotlin.math.abs(chargeableDelta) * JERSEY_PAIR_REVENUE)).coerceAtLeast(0.0)
            OperationResult(
                true,
                if (amount == 0.0) {
                    "Jersey pair count updated. No revenue entry was recorded."
                } else if (chargeableDelta > 0) {
                    "Added ${kotlin.math.abs(chargeableDelta)} jersey pair${if (kotlin.math.abs(chargeableDelta) == 1) "" else "s"} and recorded Rs ${amount.toInt()}."
                } else {
                    "Removed ${kotlin.math.abs(chargeableDelta)} jersey pair${if (kotlin.math.abs(chargeableDelta) == 1) "" else "s"} and subtracted Rs ${amount.toInt()}."
                },
            )
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to update jersey pairs.")
        }
    }

    suspend fun renewStudent(student: Student): OperationResult {
        val payments = _uiState.value.payments
        val cycleDate = student.nextRenewalCycleDate(payments)
        if (!student.isRenewalPending(payments)) {
            return OperationResult(false, "This player is not due for renewal yet.")
        }

        return try {
            val session = withFreshSession { session ->
                repository.renewStudent(student, session.email, session, cycleDate)
                session
            }
            upsertLocalStudent(
                student.copy(
                    renewals = student.renewals + cycleDate,
                    updatedBy = session.email,
                    discontinued = false,
                    rejoinedAt = if (student.discontinued) todayIsoDate() else student.rejoinedAt,
                    feePauseDays = if (student.discontinued) student.rejoinedFeePauseDays() else student.feePauseDays,
                )
            )
            refreshInBackground()
            OperationResult(true, "${student.name} marked as renewed.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to update renewal.")
        }
    }

    suspend fun recordRenewalPayment(
        student: Student,
        planType: String,
        monthsCovered: Int,
        amount: Double,
        comment: String,
        cycleDateOverride: String? = null,
        paidOn: String = "",
        proofPath: String = "",
        isJoiningFee: Boolean = false,
        coachingFee: Double = 0.0,
        admissionFee: Double = 0.0,
        jerseyAmount: Double = 0.0,
        totalFeeAmount: Double = 0.0,
        jerseySize: String = student.jerseySize,
        jerseyPairs: Int = student.jerseyPairs,
    ): OperationResult {
        val payments = _uiState.value.payments
        val cycleDate = cycleDateOverride?.takeIf { it.isNotBlank() } ?: student.nextRenewalCycleDate(payments)
        if (amount <= 0.0) {
            return OperationResult(false, "Enter a valid renewal amount.")
        }

        var whatsappWarning: String? = null
        return try {
            val (session, insertedPayment) = withFreshSession { session ->
                val payment = repository.recordRenewalPayment(
                    student = student,
                    managerEmail = session.email,
                    session = session,
                    planType = planType,
                    monthsCovered = monthsCovered,
                    amount = amount,
                    comment = comment,
                    cycleDate = cycleDate,
                    paidOn = paidOn.ifBlank { LocalDate.now().toString() },
                    proofPath = proofPath,
                    isJoiningFee = isJoiningFee,
                    coachingFee = coachingFee,
                    admissionFee = admissionFee,
                    jerseyAmount = jerseyAmount,
                    totalFeeAmount = totalFeeAmount,
                    jerseySize = jerseySize,
                    jerseyPairs = jerseyPairs,
                )
                
                // Run slow WhatsApp trigger in background
                viewModelScope.launch {
                    try {
                        repository.sendRenewalVerifiedMessage(
                            student = student,
                            session = session,
                            planLabel = if (isJoiningFee) "Joining Fee" else renewalPlanLabel(planType),
                            amount = amount,
                            fromDate = cycleDate,
                            toDate = addMonthsForPlan(cycleDate, monthsCovered),
                        )
                    } catch (e: Exception) {
                        // Background failure - log but don't block
                        println("Background WhatsApp trigger failed: ${e.message}")
                    }
                }
                session to payment
            }
            if (isJoiningFee) {
                upsertLocalStudent(
                    student.copy(
                        feesPaid = true,
                        amountPaid = amount,
                        paymentStatus = "paid",
                        feePlan = planType,
                        coachingFee = coachingFee,
                        admissionFee = admissionFee,
                        jerseyAmount = jerseyAmount,
                        totalFeeAmount = totalFeeAmount,
                        jerseySize = jerseySize,
                        jerseyPairs = jerseyPairs,
                        updatedBy = session.email,
                        discontinued = false,
                        rejoinedAt = if (student.discontinued) todayIsoDate() else student.rejoinedAt,
                        feePauseDays = if (student.discontinued) student.rejoinedFeePauseDays() else student.feePauseDays,
                    )
                )
            } else {
                upsertLocalStudent(
                    student.copy(
                        renewals = student.renewals + cycleDate,
                        updatedBy = session.email,
                        discontinued = false,
                        rejoinedAt = if (student.discontinued) todayIsoDate() else student.rejoinedAt,
                        feePauseDays = if (student.discontinued) student.rejoinedFeePauseDays() else student.feePauseDays,
                    )
                )
            }
            upsertLocalPayment(insertedPayment)
            refreshFinanceInBackground()
            refreshInBackground()
            OperationResult(
                true,
                "${student.name} ${if (isJoiningFee) "joining fee" else "renewal payment"} recorded.${whatsappWarning?.let { " $it" } ?: ""}",
            )
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to record renewal payment.")
        }
    }

    suspend fun confirmPendingPayment(student: Student, followUp: PaymentFollowUp?): OperationResult {
        val isJoiningFee = student.isPaymentPendingVerification() && followUp == null
        if (followUp?.isPendingVerification() != true && !isJoiningFee) {
            return OperationResult(false, "No pending payment proof found for this player.")
        }
        val planType = followUp?.selectedPlan?.takeIf { it in setOf("monthly", "quarterly", "halfyearly", "special", "custom") } ?: "monthly"
        val monthsCovered = followUp?.monthsCovered?.takeIf { it > 0 } ?: when (planType) {
            "quarterly" -> 3
            "halfyearly" -> 6
            else -> 1
        }
        val amount = followUp?.amount?.takeIf { it > 0.0 } ?: when (planType) {
            "quarterly" -> 9975.0
            "halfyearly" -> 18900.0
            "special" -> 10000.0
            else -> 3500.0
        }
        val cycleDate = followUp?.cycleStartDate?.takeIf { it.isNotBlank() } ?: (if (isJoiningFee) student.joinDate else student.nextRenewalCycleDate(_uiState.value.payments))
        return recordRenewalPayment(
            student = student,
            planType = planType,
            monthsCovered = monthsCovered,
            amount = amount,
            comment = if (isJoiningFee) "Joining fee confirmed by manager." else "Confirmed from WhatsApp payment proof.",
            cycleDateOverride = cycleDate,
            isJoiningFee = isJoiningFee,
            coachingFee = if (isJoiningFee) student.coachingFee else 0.0,
            admissionFee = if (isJoiningFee) student.admissionFee else 0.0,
            jerseyAmount = if (isJoiningFee) student.jerseyAmount else 0.0,
            totalFeeAmount = if (isJoiningFee) student.totalFeeAmount else 0.0,
        )
    }

    suspend fun sendRenewalReminder(student: Student): OperationResult {
        val payments = _uiState.value.payments
        val isRenewalDue = student.isRenewalPending(payments)
        if (!isRenewalDue) {
            return OperationResult(false, "${student.name} is not due for a renewal reminder.")
        }

        return try {
            val (settings, dueDate, reminderMessage) = withFreshSession { session ->
                val reminderSettings = repository.fetchReminderSettings(session)
                val nextDue = student.nextRenewalCycleDate(payments)
                val overdueDays = maxOf(0, ChronoUnit.DAYS.between(LocalDate.parse(nextDue), LocalDate.now()).toInt())
                val resultMessage = repository.logRenewalReminder(
                    session = session,
                    student = student,
                    reminderType = "renewal",
                    dueDate = nextDue,
                    overdueDays = overdueDays,
                    messagePreview = buildReminderPreview(student, nextDue, reminderSettings.managerPhone, false),
                    settings = reminderSettings,
                )
                Triple(reminderSettings, nextDue, resultMessage)
            }
            val mode = if (settings.dryRunMode || !settings.whatsappRemindersEnabled) "Reminder" else "WhatsApp"
            loadFinance()
            OperationResult(true, reminderMessage.ifBlank { "$mode sent for ${student.name} (${dueDate})." })
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to log reminder.")
        }
    }



    suspend fun toggleStatus(student: Student): OperationResult {
        return try {
            val session = withFreshSession { session ->
                repository.toggleStudentStatus(student, session.email, session)
                session
            }
            upsertLocalStudent(
                student.copy(
                    discontinued = !student.discontinued,
                    updatedBy = session.email,
                    discontinuedAt = if (student.discontinued) student.discontinuedAt else todayIsoDate(),
                    rejoinedAt = if (student.discontinued) todayIsoDate() else student.rejoinedAt,
                    feePauseDays = if (student.discontinued) student.rejoinedFeePauseDays() else student.feePauseDays,
                )
            )
            refreshInBackground()
            OperationResult(
                true,
                if (student.discontinued) "${student.name} marked active again."
                else "${student.name} marked as discontinued."
            )
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to update player status.")
        }
    }

    suspend fun loadKids() {
        val hasExistingKids = _uiState.value.kids.isNotEmpty()
        _uiState.update {
            it.copy(
                isLoading = !hasExistingKids,
                isRefreshing = hasExistingKids,
            )
        }
        try {
            val kids = repository.fetchStudents()
            _uiState.update {
                val selected = if (it.selectedSlotFilter == "not-set" && kids.none { kid -> kid.isActive() && kid.timeSlot.isBlank() }) {
                    ""
                } else {
                    it.selectedSlotFilter
                }
                it.copy(
                    kids = sortStudents(kids),
                    selectedSlotFilter = selected,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                )
            }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = error.message ?: "Unable to load academy data.",
                )
            }
        }
    }

    fun filteredKids(): List<Student> {
        val state = _uiState.value
        val filter = state.selectedSlotFilter
        val search = state.searchQuery.trim().lowercase()
        val slotFiltered = when {
            filter.isBlank() -> state.kids
            filter == "not-set" -> state.kids.filter { it.isActive() && it.timeSlot.isBlank() }
            else -> state.kids.filter { it.isActive() && it.timeSlot == filter }
        }
        val searched = if (search.isBlank()) {
            slotFiltered
        } else {
            slotFiltered.filter { student ->
            student.name.lowercase().contains(search) ||
                student.timeSlot.lowercase().contains(search) ||
                student.updatedBy.lowercase().contains(search)
            }
        }
        val filtered = searched.filter { student ->
            student.matchesRosterFilters(
                payments = state.payments,
                statusFilter = state.rosterStatusFilter,
                jerseyFilter = state.rosterJerseyFilter,
                typeFilter = state.rosterTypeFilter,
                feePaidFilter = state.rosterFeePaidFilter,
                feeDueFilter = state.rosterFeeDueFilter,
            )
        }
        return sortRosterStudents(filtered)
    }

    fun stats(): DashboardStats = buildStats(_uiState.value.kids)

    fun slotSummary(): List<SlotSummary> = buildSlotSummary(
        students = _uiState.value.kids,
        timeSlots = TIME_SLOTS,
        selected = _uiState.value.selectedSlotFilter,
    )

    fun alertKids(): List<Student> {
        val state = _uiState.value
        return state.kids.filter { it.isFeesPending() || it.isRenewalPending(state.payments) }
    }

    fun emptyStateMessage(): String {
        val state = _uiState.value
        if (state.kids.isEmpty()) {
            return if (state.session != null) {
                "No Gen Alpha players added yet. Use the add button to create the first record."
            } else {
                "No Gen Alpha players added yet. Login as manager to create the first record."
            }
        }

        if (state.searchQuery.isNotBlank()) {
            return "No players match \"${state.searchQuery}\" in the current view."
        }

        if (state.hasRosterDetailFilters()) {
            return "No registered players match the selected filters."
        }

        return when (state.selectedSlotFilter) {
            "" -> "No registered players match the current view."
            "not-set" -> "No active kids are currently missing a time slot."
            else -> "No active kids are currently assigned to the ${state.selectedSlotFilter} slot."
        }
    }

    fun canEdit(): Boolean = _uiState.value.session != null

    fun managerIdentity(): String = _uiState.value.session?.email.orEmpty()

    fun alertSummaryText(): String {
        val alerts = alertKids()
        return when {
            alerts.isEmpty() -> "All current join fees and renewals are up to date."
            else -> "Need fees or renewal action for: ${alerts.joinToString { it.name }}"
        }
    }

    private suspend fun refreshSessionIfPossible() {
        repository.startStudentRealtime(realtimeListener, _uiState.value.session)
    }

    private fun startAttendanceLiveSync() {
        if (attendanceLiveSyncJob?.isActive == true) return
        attendanceLiveSyncJob = viewModelScope.launch {
            while (isActive) {
                runCatching { repository.fetchTodayAttendance() }
                    .onSuccess { ids ->
                        _uiState.update { state ->
                            if (state.todayAttendanceIds == ids) state else state.copy(todayAttendanceIds = ids)
                        }
                    }
                runCatching { repository.fetchAttendanceCounts() }
                    .onSuccess { counts ->
                        _uiState.update { state ->
                            if (state.attendanceCounts == counts) state else state.copy(attendanceCounts = counts)
                        }
                    }
                delay(1000)
            }
        }
    }

    private fun startPlayersLiveSync() {
        if (playersLiveSyncJob?.isActive == true) return
        playersLiveSyncJob = viewModelScope.launch {
            while (isActive) {
                runCatching { repository.fetchStudents() }
                    .onSuccess { latestKids ->
                        val sortedLatest = sortStudents(latestKids)
                        _uiState.update { state ->
                            if (state.kids == sortedLatest) state else state.copy(kids = sortedLatest)
                        }
                    }
                _uiState.value.session?.let { session ->
                    runCatching { repository.fetchPendingAdmissions(session) }
                        .onSuccess { admissions ->
                            _uiState.update { state ->
                                if (state.pendingAdmissions == admissions) state else state.copy(pendingAdmissions = admissions)
                            }
                        }
                }
                delay(1000)
            }
        }
    }

    private fun upsertLocalExpense(expense: AcademyExpense) {
        _uiState.update { state ->
            state.copy(
                expenses = (state.expenses.filterNot { it.id == expense.id } + expense)
                    .sortedByDescending { it.expenseDate },
            )
        }
    }

    private fun upsertLocalPayment(payment: StudentPayment) {
        _uiState.update { state ->
            state.copy(
                payments = (state.payments.filterNot { it.id == payment.id } + payment)
                    .sortedByDescending { it.paidOn },
            )
        }
    }

    private fun refreshFinanceInBackground() {
        viewModelScope.launch {
            loadFinance()
        }
    }

    private fun refreshAdmissionReviewInBackground() {
        viewModelScope.launch {
            loadPendingAdmissions()
        }
    }

    private fun expireSession() {
        sessionPrefs.clearSession()
        _uiState.update {
            it.copy(
                session = null,
                errorMessage = "Session expired. Login again to continue editing.",
            )
        }
    }

    private suspend fun <T> withFreshSession(block: suspend (ManagerSession) -> T): T {
        val current = _uiState.value.session ?: throw IllegalStateException("Login as manager first.")
        return try {
            block(current)
        } catch (error: SupabaseException) {
            val refreshed = refreshOrRelogin(current, error)
            block(refreshed)
        }
    }

    private suspend fun refreshOrRelogin(current: ManagerSession, originalError: SupabaseException): ManagerSession {
        if (current.refreshToken.isNotBlank()) {
            try {
                val refreshed = repository.refreshSession(current.refreshToken)
                _uiState.update { it.copy(session = refreshed) }
                return refreshed
            } catch (_: Exception) {
            }
        }

        val savedEmail = _uiState.value.lastEmail
        val savedPassword = _uiState.value.lastPassword
        if (savedEmail.isNotBlank() && savedPassword.isNotBlank()) {
            try {
                val relogged = repository.signIn(savedEmail, savedPassword)
                _uiState.update { it.copy(session = relogged) }
                return relogged
            } catch (_: Exception) {
            }
        }

        if (originalError.statusCode == 401 || originalError.statusCode == 403) {
            expireSession()
        }
        throw originalError
    }

    private fun validateDraft(draft: StudentDraft): String? {
        if (draft.name.isBlank() || draft.age.isBlank() || draft.timeSlot.isBlank() || draft.joinDate.isBlank()) {
            return "Please complete all required fields."
        }
        val age = draft.age.toIntOrNull()
        if (age == null || age !in 4..18) {
            return "Age should be between 4 and 18."
        }
        if (isFutureDate(draft.joinDate)) {
            return "Join date cannot be in the future."
        }
        val parentDigits = draft.parentContactNo.filter(Char::isDigit)
        val alternateDigits = draft.alternateContactNo.filter(Char::isDigit)
        if (parentDigits.isNotBlank() && parentDigits.length != 10) {
            return "Mobile number should be 10 digits."
        }
        if (alternateDigits.isNotBlank() && alternateDigits.length != 10) {
            return "Alternate mobile number should be 10 digits."
        }
        val jerseyPairs = draft.jerseyPairs.toIntOrNull()
        if (draft.jerseyPairs.isNotBlank() && (jerseyPairs == null || jerseyPairs < 0)) {
            return "Enter a valid jersey pair count."
        }
        if (!draft.feesPaid) {
            return null
        }
        if (draft.amountPaid.isBlank()) {
            return null
        }
        val amount = draft.amountPaid.toDoubleOrNull()
        if (amount == null || amount < 0) {
            return "Enter a valid amount paid."
        }
        return null
    }

    private fun validateAdmissionDraft(draft: AdmissionDraft): String? {
        if (
            draft.applicantName.isBlank() ||
            draft.filledBy.isBlank() ||
            draft.dateOfBirth.isBlank() ||
            draft.gender.isBlank() ||
            draft.fatherGuardianName.isBlank() ||
            draft.parentContactNo.isBlank() ||
            draft.alternateContactNo.isBlank() ||
            draft.address.isBlank() ||
            draft.schoolCollege.isBlank() ||
            draft.timeSlot.isBlank() ||
            draft.joinDate.isBlank()
        ) {
            return "Please complete all required admission details."
        }

        val age = calculateAgeFromDate(draft.dateOfBirth)
        if (age == null || age !in 4..18) {
            return "Date of birth should map to an age between 4 and 18."
        }

        if (isFutureDate(draft.dateOfBirth)) {
            return "Date of birth cannot be in the future."
        }

        if (isFutureDate(draft.joinDate)) {
            return "Join date cannot be in the future."
        }

        val parentDigits = draft.parentContactNo.filter(Char::isDigit)
        val alternateDigits = draft.alternateContactNo.filter(Char::isDigit)
        if (parentDigits.length != 10 || alternateDigits.length != 10) {
            return "Parent and alternate contact numbers must be exactly 10 digits."
        }

        if (draft.feesPaid || draft.paymentPendingVerification) {
            val amount = draft.amountPaid.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                return "Enter a valid fee amount if payment is marked as made."
            }
        }

        val jerseyPairs = draft.jerseyPairs.toIntOrNull()
        if (draft.jerseyPairs.isNotBlank() && (jerseyPairs == null || jerseyPairs < 0)) {
            return "Enter a valid jersey pair count."
        }

        if (!draft.consentAccepted || !draft.termsAccepted) {
            return "Please accept the consent and terms to continue."
        }

        return null
    }

    private fun Student.matchesRosterFilters(
        payments: List<StudentPayment>,
        statusFilter: String,
        jerseyFilter: String,
        typeFilter: String,
        feePaidFilter: String,
        feeDueFilter: String,
    ): Boolean {
        val size = jerseySize.trim()
        val overdue = feesPaid && isActive() && runCatching {
            LocalDate.parse(nextRenewalCycleDate(payments)).isBefore(LocalDate.now())
        }.getOrDefault(false)

        if (statusFilter == "active" && !isActive()) return false
        if (statusFilter == "discontinued" && isActive()) return false
        if (jerseyFilter == "not-set" && size.isNotBlank()) return false
        if (jerseyFilter !in setOf("all", "not-set") && size != jerseyFilter) return false
        if (typeFilter == "new" && renewals.isNotEmpty()) return false
        if (typeFilter == "returning" && renewals.isEmpty()) return false
        if (feePaidFilter == "paid" && !feesPaid) return false
        if (feePaidFilter == "not-paid" && feesPaid) return false
        if (feeDueFilter == "joining-pending" && !isFeesPending()) return false
        if (feeDueFilter == "overdue" && !overdue) return false

        return true
    }

    private fun sortStudents(students: List<Student>): List<Student> {
        return students.sortedWith(
            compareByDescending<Student> { it.joinDate }
                .thenBy { it.name.lowercase() }
        )
    }

    private fun sortRosterStudents(students: List<Student>): List<Student> {
        val state = _uiState.value
        val comparator = Comparator<Student> { first, second ->
            val result = when (state.rosterSortKey) {
                "name" -> first.name.compareTo(second.name, ignoreCase = true)
                "age" -> first.age.compareTo(second.age)
                "slot" -> first.timeSlot.ifBlank { "Not set" }.compareTo(second.timeSlot.ifBlank { "Not set" }, ignoreCase = true)
                "status" -> (if (first.discontinued) "Discontinued" else "Active")
                    .compareTo(if (second.discontinued) "Discontinued" else "Active", ignoreCase = true)
                "amount" -> first.amountPaid.compareTo(second.amountPaid)
                "nextDue" -> first.nextRenewalCycleDate(state.payments).compareTo(second.nextRenewalCycleDate(state.payments))
                else -> first.joinDate.compareTo(second.joinDate)
            }
            if (result != 0) result else first.name.compareTo(second.name, ignoreCase = true)
        }
        return if (state.rosterSortAscending) students.sortedWith(comparator) else students.sortedWith(comparator.reversed())
    }

    private fun upsertLocalStudent(student: Student) {
        _uiState.update { state ->
            state.copy(kids = sortStudents(state.kids.filterNot { it.id == student.id } + student))
        }
    }

    private fun removeLocalStudent(studentId: String) {
        _uiState.update { state ->
            state.copy(kids = sortStudents(state.kids.filterNot { it.id == studentId }))
        }
    }

    private fun refreshInBackground() {
        viewModelScope.launch {
            loadKids()
        }
    }
}

private fun renewalPlanLabel(plan: String): String = when (plan) {
    "quarterly" -> "3 Months"
    "halfyearly" -> "6 Months"
    "special" -> "Special training"
    "custom" -> "Custom renewal"
    else -> "1 Month"
}

private fun paymentProofPath(details: String): String {
    val match = Regex("payment-proofs/([^\\s.]+/[^\\s.]+\\.(?:jpg|jpeg|png|webp|pdf))", RegexOption.IGNORE_CASE)
        .find(details)
    return match?.groupValues?.getOrNull(1).orEmpty()
}

private fun Student.rejoinedFeePauseDays(rejoinDate: String = todayIsoDate()): Int {
    val pauseStart = discontinuedAt
        ?: updatedAt.take(10).takeIf { it.isNotBlank() }
        ?: createdAt.take(10).takeIf { it.isNotBlank() }
        ?: joinDate
    return feePauseDays.coerceAtLeast(0) + daysBetweenIso(pauseStart, rejoinDate)
}

class AcademyViewModelFactory(
    private val repository: SupabaseRepository,
    private val sessionPrefs: SessionPrefs,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AcademyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcademyViewModel(repository, sessionPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun buildReminderPreview(
    student: Student,
    dueDate: String,
    managerPhone: String,
    isJoiningFee: Boolean,
): String {
    val dueText = if (isJoiningFee) "joining fee from $dueDate" else "renewal due $dueDate"
    return "Gen Alpha Cricket Academy reminder for ${student.name}: $dueText. Parent can choose 1 Month / 3 Months / 6 Months / Need Help. Help: $managerPhone."
}
