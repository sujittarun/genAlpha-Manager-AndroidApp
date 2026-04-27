package com.genalpha.cricketacademy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genalpha.cricketacademy.data.DashboardStats
import com.genalpha.cricketacademy.data.AdmissionDraft
import com.genalpha.cricketacademy.data.ManagerSession
import com.genalpha.cricketacademy.data.OperationResult
import com.genalpha.cricketacademy.data.SessionPrefs
import com.genalpha.cricketacademy.data.SlotSummary
import com.genalpha.cricketacademy.data.Student
import com.genalpha.cricketacademy.data.StudentDraft
import com.genalpha.cricketacademy.data.StudentRealtimeListener
import com.genalpha.cricketacademy.data.StudentTimelineItem
import com.genalpha.cricketacademy.data.SupabaseException
import com.genalpha.cricketacademy.data.SupabaseRepository
import com.genalpha.cricketacademy.data.buildSlotSummary
import com.genalpha.cricketacademy.data.buildStats
import com.genalpha.cricketacademy.data.calculateAgeFromDate
import com.genalpha.cricketacademy.data.isFutureDate
import com.genalpha.cricketacademy.data.isActive
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

private val TIME_SLOTS = listOf("6AM", "7:30AM", "4PM", "5:30PM", "7PM")

data class AcademyUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isAuthLoading: Boolean = false,
    val isAttendanceRefreshing: Boolean = false,
    val kids: List<Student> = emptyList(),
    val todayAttendanceIds: Set<String> = emptySet(),
    val attendanceCounts: Map<String, Int> = emptyMap(),
    val selectedSlotFilter: String = "",
    val searchQuery: String = "",
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
            if (attendanceDate != todayIsoDate()) return
            _uiState.update { state ->
                if (state.todayAttendanceIds.contains(studentId)) return@update state
                val currentCount = state.attendanceCounts[studentId] ?: 0
                state.copy(
                    todayAttendanceIds = state.todayAttendanceIds + studentId,
                    attendanceCounts = state.attendanceCounts + (studentId to (currentCount + 1)),
                )
            }
        }

        override fun onAttendanceDeleted(studentId: String, attendanceDate: String) {
            if (attendanceDate != todayIsoDate()) return
            _uiState.update { state ->
                if (!state.todayAttendanceIds.contains(studentId)) return@update state
                val currentCount = state.attendanceCounts[studentId] ?: 0
                state.copy(
                    todayAttendanceIds = state.todayAttendanceIds - studentId,
                    attendanceCounts = state.attendanceCounts + (studentId to (currentCount - 1).coerceAtLeast(0)),
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(
        AcademyUiState(
            lastEmail = sessionPrefs.loadSavedEmail(),
            lastPassword = sessionPrefs.loadSavedPassword(),
            darkModeEnabled = sessionPrefs.loadDarkModeEnabled(),
            session = sessionPrefs.loadSession(),
        )
    )
    val uiState: StateFlow<AcademyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshSessionIfPossible()
            loadKids()
            loadTodayAttendance()
            loadAttendanceCounts()
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

    fun toggleDarkMode() {
        val enabled = !_uiState.value.darkModeEnabled
        sessionPrefs.saveDarkModeEnabled(enabled)
        _uiState.update { it.copy(darkModeEnabled = enabled) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
            sessionPrefs.saveSession(session)
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
            OperationResult(true, "Manager login successful.")
        } catch (error: Exception) {
            _uiState.update { it.copy(isAuthLoading = false) }
            OperationResult(false, error.message ?: "Unable to login.")
        }
    }

    fun logout() {
        sessionPrefs.clearSession()
        repository.startStudentRealtime(realtimeListener, null)
        _uiState.update { it.copy(session = null) }
    }

    suspend fun peekNextAdmissionRegNo(): Long = repository.peekNextAdmissionRegNo()

    suspend fun extractAdmissionDraft(
        fileBase64: String,
        mimeType: String,
        fileName: String,
    ): AdmissionExtractionResult {
        return try {
            AdmissionExtractionResult(
                success = true,
                message = "AI filled the readable fields. Please review before submitting.",
                draft = repository.extractAdmissionDraft(fileBase64, mimeType, fileName),
            )
        } catch (error: Exception) {
            AdmissionExtractionResult(
                success = false,
                message = error.message ?: "Unable to read admission document.",
                draft = null,
            )
        }
    }

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

    suspend fun attendanceHistory(studentId: String): List<String> {
        return repository.fetchAttendanceDates(studentId)
    }

    suspend fun studentTimeline(studentId: String): List<StudentTimelineItem> {
        return repository.fetchStudentTimeline(studentId)
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
                it.copy(
                    todayAttendanceIds = it.todayAttendanceIds + student.id,
                    attendanceCounts = it.attendanceCounts + (student.id to ((it.attendanceCounts[student.id] ?: 0) + 1)),
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
                it.copy(
                    todayAttendanceIds = it.todayAttendanceIds - student.id,
                    attendanceCounts = it.attendanceCounts + (student.id to (currentCount - 1).coerceAtLeast(0)),
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
            refreshInBackground()
            OperationResult(true, "Admission submitted. Reg No ${created.regNo}")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to submit admission form.")
        }
    }

    suspend fun saveStudent(draft: StudentDraft, editingStudent: Student?): OperationResult {
        val validation = validateDraft(draft)
        if (validation != null) {
            return OperationResult(false, validation)
        }

        return try {
            val session = withFreshSession { session ->
                if (editingStudent == null) {
                    repository.createStudent(draft, session.email, session)
                } else {
                    repository.updateStudent(editingStudent, draft, session.email, session)
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
                        jerseySize = draft.jerseySize,
                        jerseyPairs = draft.jerseyPairs.toIntOrNull() ?: 0,
                        updatedBy = session.email,
                        discontinuedAt = editingStudent.discontinuedAt,
                    )
                )
                refreshInBackground()
            }
            OperationResult(
                true,
                if (editingStudent == null) "Player added successfully." else "Player updated successfully."
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

    suspend fun renewStudent(student: Student): OperationResult {
        if (!student.isRenewalPending()) {
            return OperationResult(false, "This player is not due for renewal yet.")
        }

        return try {
            val session = withFreshSession { session ->
                repository.renewStudent(student, session.email, session)
                session
            }
            upsertLocalStudent(
                student.copy(
                    renewals = student.renewals + student.nextRenewalCycleDate(),
                    updatedBy = session.email,
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
    ): OperationResult {
        if (!student.isRenewalPending()) {
            return OperationResult(false, "This player is not due for renewal yet.")
        }
        if (amount <= 0.0) {
            return OperationResult(false, "Enter a valid renewal amount.")
        }

        return try {
            val session = withFreshSession { session ->
                repository.recordRenewalPayment(student, session.email, session, planType, monthsCovered, amount, comment)
                session
            }
            upsertLocalStudent(
                student.copy(
                    renewals = student.renewals + student.nextRenewalCycleDate(),
                    updatedBy = session.email,
                )
            )
            refreshInBackground()
            OperationResult(true, "${student.name} renewal payment recorded.")
        } catch (error: Exception) {
            OperationResult(false, error.message ?: "Unable to record renewal payment.")
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
                    discontinuedAt = if (student.discontinued) null else todayIsoDate(),
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
        val filter = _uiState.value.selectedSlotFilter
        val search = _uiState.value.searchQuery.trim().lowercase()
        val slotFiltered = when {
            filter.isBlank() -> _uiState.value.kids
            filter == "not-set" -> _uiState.value.kids.filter { it.isActive() && it.timeSlot.isBlank() }
            else -> _uiState.value.kids.filter { it.isActive() && it.timeSlot == filter }
        }
        if (search.isBlank()) {
            return slotFiltered
        }
        return slotFiltered.filter { student ->
            student.name.lowercase().contains(search) ||
                student.timeSlot.lowercase().contains(search) ||
                student.updatedBy.lowercase().contains(search)
        }
    }

    fun stats(): DashboardStats = buildStats(_uiState.value.kids)

    fun slotSummary(): List<SlotSummary> = buildSlotSummary(
        students = _uiState.value.kids,
        timeSlots = TIME_SLOTS,
        selected = _uiState.value.selectedSlotFilter,
    )

    fun alertKids(): List<Student> = _uiState.value.kids.filter { it.isFeesPending() || it.isRenewalPending() }

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
        val session = _uiState.value.session
        val savedEmail = _uiState.value.lastEmail
        val savedPassword = _uiState.value.lastPassword

        try {
            when {
                session != null && session.refreshToken.isNotBlank() -> {
                    val refreshed = repository.refreshSession(session.refreshToken)
                    sessionPrefs.saveSession(refreshed)
                    repository.startStudentRealtime(realtimeListener, refreshed)
                    _uiState.update { it.copy(session = refreshed) }
                }
                session == null && savedEmail.isNotBlank() && savedPassword.isNotBlank() -> {
                    val relogged = repository.signIn(savedEmail, savedPassword)
                    sessionPrefs.saveSession(relogged)
                    repository.startStudentRealtime(realtimeListener, relogged)
                    _uiState.update { it.copy(session = relogged) }
                }
            }
        } catch (_: Exception) {
            if (session != null) {
                expireSession()
            }
        } finally {
            // Keep realtime active for public/player flows too, even without manager auth.
            repository.startStudentRealtime(realtimeListener, _uiState.value.session)
        }
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
                delay(1000)
            }
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
                sessionPrefs.saveSession(refreshed)
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
                sessionPrefs.saveSession(relogged)
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
        if (!draft.feesPaid) {
            if (draft.jerseyPairs.toIntOrNull()?.let { it < 0 } == true) {
                return "Jersey pairs cannot be negative."
            }
            return null
        }
        val amount = draft.amountPaid.toDoubleOrNull()
        if (amount == null || amount < 0) {
            return "Enter a valid amount paid."
        }
        val jerseyPairs = draft.jerseyPairs.toIntOrNull()
        if (jerseyPairs == null || jerseyPairs < 0) {
            return "Enter a valid jersey pair count."
        }
        return null
    }

    private fun validateAdmissionDraft(draft: AdmissionDraft): String? {
        if (
            draft.applicantName.isBlank() ||
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

        if (draft.feesPaid) {
            val amount = draft.amountPaid.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                return "Enter a valid fee amount if fees are marked as paid."
            }
        }

        val jerseyPairs = draft.jerseyPairs.toIntOrNull()
        if (jerseyPairs == null || jerseyPairs < 0) {
            return "Enter a valid jersey pair count."
        }

        if (!draft.consentAccepted || !draft.termsAccepted) {
            return "Please accept the consent and terms to continue."
        }

        return null
    }

    private fun sortStudents(students: List<Student>): List<Student> {
        return students.sortedWith(
            compareByDescending<Student> { it.joinDate }
                .thenBy { it.name.lowercase() }
        )
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

data class AdmissionExtractionResult(
    val success: Boolean,
    val message: String,
    val draft: AdmissionDraft?,
)
