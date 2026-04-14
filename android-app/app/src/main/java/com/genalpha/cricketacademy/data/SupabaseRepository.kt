package com.genalpha.cricketacademy.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

class SupabaseRepository(
    private val baseUrl: String = SupabaseConfig.URL,
    private val anonKey: String = SupabaseConfig.ANON_KEY,
) {
    private val client = OkHttpClient.Builder().build()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val authAdapter = moshi.adapter(AuthResponse::class.java)
    private val errorAdapter = moshi.adapter(ErrorResponse::class.java)
    private val admissionRegPreviewAdapter = moshi.adapter<List<AdmissionRegPreview>>(
        Types.newParameterizedType(List::class.java, AdmissionRegPreview::class.java)
    )
    private val attendanceAdapter = moshi.adapter<List<AttendanceRecord>>(
        Types.newParameterizedType(List::class.java, AttendanceRecord::class.java)
    )
    private val attendanceDateAdapter = moshi.adapter<List<AttendanceDateRecord>>(
        Types.newParameterizedType(List::class.java, AttendanceDateRecord::class.java)
    )
    private val admissionInsertAdapter = moshi.adapter<List<AdmissionInsertResult>>(
        Types.newParameterizedType(List::class.java, AdmissionInsertResult::class.java)
    )
    private val studentListAdapter = moshi.adapter<List<StudentDto>>(
        Types.newParameterizedType(List::class.java, StudentDto::class.java)
    )
    private val realtimeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refCounter = AtomicInteger(1)
    private var realtimeSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var shouldReconnect = false
    private var realtimeListener: StudentRealtimeListener? = null
    private var realtimeSession: ManagerSession? = null

    suspend fun fetchStudents(): List<Student> = withContext(Dispatchers.IO) {
        val request = baseRequest("$baseUrl/rest/v1/students?select=*&order=join_date.desc")
            .header("Authorization", "Bearer $anonKey")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(response.body?.string()))
            }

            val body = response.body?.string().orEmpty()
            studentListAdapter.fromJson(body).orEmpty().map { it.toDomain() }
        }
    }

    suspend fun signIn(email: String, password: String): ManagerSession = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)

        val request = baseRequest("$baseUrl/auth/v1/token?grant_type=password")
            .post(body)
            .build()

        executeAuthRequest(request)
    }

    suspend fun refreshSession(refreshToken: String): ManagerSession = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("refresh_token", refreshToken)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)

        val request = baseRequest("$baseUrl/auth/v1/token?grant_type=refresh_token")
            .post(body)
            .build()

        executeAuthRequest(request)
    }

    suspend fun createStudent(draft: StudentDraft, managerEmail: String, session: ManagerSession) {
        withContext(Dispatchers.IO) {
            val body = studentPayload(
                draft = draft,
                renewals = emptyList(),
                addedBy = managerEmail,
                updatedBy = managerEmail,
                discontinued = false,
                discontinuedAt = null,
            )

            executeWriteRequest(
                url = "$baseUrl/rest/v1/students",
                session = session,
                method = "POST",
                body = body,
            )
        }
    }

    suspend fun submitAdmission(draft: AdmissionDraft): AdmissionInsertResult = withContext(Dispatchers.IO) {
        val age = calculateAgeFromDate(draft.dateOfBirth)
            ?: throw IllegalArgumentException("Enter a valid date of birth.")

        val body = JSONObject()
            .put("p_applicant_name", draft.applicantName.trim())
            .put("p_nationality", draft.nationality.trim())
            .put("p_date_of_birth", draft.dateOfBirth)
            .put("p_age", age)
            .put("p_gender", draft.gender)
            .put("p_father_guardian_name", draft.fatherGuardianName.trim())
            .put("p_alternate_contact_no", draft.alternateContactNo.trim())
            .put("p_parent_contact_no", draft.parentContactNo.trim())
            .put("p_city", draft.city.trim())
            .put("p_address", draft.address.trim())
            .put("p_school_college", draft.schoolCollege.trim())
            .put("p_parent_aadhaar_no", draft.parentAadhaarNo.trim())
            .put("p_time_slot", draft.timeSlot)
            .put("p_join_date", draft.joinDate)
            .put("p_fees_paid", draft.feesPaid)
            .put("p_amount_paid", draft.amountPaid.toDoubleOrNull() ?: 0.0)
            .put("p_jersey_size", draft.jerseySize.trim())
            .put("p_jersey_pairs", draft.jerseyPairs.toIntOrNull() ?: 0)
            .put("p_payment_method", draft.paymentMethod.trim())
            .put("p_payment_upi_id", draft.paymentUpiId.trim())
            .put("p_payment_reference", draft.paymentReference.trim())
            .put("p_comments", draft.comments.trim())
            .put("p_batsman_style", draft.batsmanStyle)
            .put("p_bowling_styles", JSONArray(draft.bowlingStyles))
            .put("p_ready_to_start", draft.readyToStartNow)
            .put("p_consent_accepted", draft.consentAccepted)
            .put("p_terms_accepted", draft.termsAccepted)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)

        val request = baseRequest("$baseUrl/rest/v1/rpc/submit_admission_form")
            .header("Authorization", "Bearer $anonKey")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(response.body?.string()))
            }

            val rows = admissionInsertAdapter.fromJson(response.body?.string().orEmpty()).orEmpty()
            rows.firstOrNull() ?: throw SupabaseException(response.code, "Admission submitted, but registration number was not returned.")
        }
    }

    suspend fun peekNextAdmissionRegNo(): Long = withContext(Dispatchers.IO) {
        val request = baseRequest("$baseUrl/rest/v1/rpc/peek_next_admission_reg_no")
            .header("Authorization", "Bearer $anonKey")
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(response.body?.string()))
            }

            val rows = admissionRegPreviewAdapter.fromJson(response.body?.string().orEmpty()).orEmpty()
            rows.firstOrNull()?.nextRegNo
                ?: throw SupabaseException(response.code, "Unable to fetch the next registration number.")
        }
    }

    suspend fun fetchTodayAttendance(date: String = todayIsoDate()): Set<String> = withContext(Dispatchers.IO) {
        val request = baseRequest("$baseUrl/rest/v1/attendance?select=student_id&attendance_date=eq.$date")
            .header("Authorization", "Bearer $anonKey")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(response.body?.string()))
            }

            attendanceAdapter.fromJson(response.body?.string().orEmpty())
                .orEmpty()
                .map { it.studentId }
                .toSet()
        }
    }

    suspend fun fetchAttendanceCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
        val request = baseRequest("$baseUrl/rest/v1/attendance?select=student_id")
            .header("Authorization", "Bearer $anonKey")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(response.body?.string()))
            }

            attendanceAdapter.fromJson(response.body?.string().orEmpty())
                .orEmpty()
                .groupingBy { it.studentId }
                .eachCount()
        }
    }

    suspend fun fetchAttendanceDates(studentId: String): List<String> = withContext(Dispatchers.IO) {
        val request = baseRequest("$baseUrl/rest/v1/attendance?select=attendance_date&student_id=eq.$studentId&order=attendance_date.desc")
            .header("Authorization", "Bearer $anonKey")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(response.body?.string()))
            }

            attendanceDateAdapter.fromJson(response.body?.string().orEmpty())
                .orEmpty()
                .map { it.attendanceDate }
        }
    }

    suspend fun markTodayAttendance(studentId: String, date: String = todayIsoDate()) {
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("p_student_id", studentId)
                .put("p_attendance_date", date)
                .toString()
                .toRequestBody(JSON_MEDIA_TYPE)

            val request = baseRequest("$baseUrl/rest/v1/rpc/mark_player_attendance")
                .header("Authorization", "Bearer $anonKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw SupabaseException(response.code, parseError(response.body?.string()))
                }
            }
        }
    }

    suspend fun unmarkTodayAttendance(studentId: String, date: String = todayIsoDate()) {
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("p_student_id", studentId)
                .put("p_attendance_date", date)
                .toString()
                .toRequestBody(JSON_MEDIA_TYPE)

            val request = baseRequest("$baseUrl/rest/v1/rpc/unmark_player_attendance")
                .header("Authorization", "Bearer $anonKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw SupabaseException(response.code, parseError(response.body?.string()))
                }
            }
        }
    }

    suspend fun updateStudent(
        current: Student,
        draft: StudentDraft,
        managerEmail: String,
        session: ManagerSession,
    ) {
        withContext(Dispatchers.IO) {
            val body = studentPayload(
                draft = draft,
                renewals = current.renewals,
                addedBy = current.addedBy,
                updatedBy = managerEmail,
                discontinued = current.discontinued,
                discontinuedAt = current.discontinuedAt,
            )

            executeWriteRequest(
                url = "$baseUrl/rest/v1/students?id=eq.${current.id}",
                session = session,
                method = "PATCH",
                body = body,
            )
        }
    }

    suspend fun deleteStudent(id: String, session: ManagerSession) {
        withContext(Dispatchers.IO) {
            executeWriteRequest(
                url = "$baseUrl/rest/v1/students?id=eq.$id",
                session = session,
                method = "DELETE",
                body = null,
            )
        }
    }

    suspend fun renewStudent(student: Student, managerEmail: String, session: ManagerSession) {
        withContext(Dispatchers.IO) {
            val renewals = JSONArray(student.renewals + todayIsoDate())
            val body = JSONObject()
                .put("renewals", renewals)
                .put("updated_by", managerEmail)

            executeWriteRequest(
                url = "$baseUrl/rest/v1/students?id=eq.${student.id}",
                session = session,
                method = "PATCH",
                body = body,
            )
        }
    }

    suspend fun toggleStudentStatus(student: Student, managerEmail: String, session: ManagerSession) {
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("discontinued", !student.discontinued)
                .put("discontinued_at", if (!student.discontinued) todayIsoDate() else JSONObject.NULL)
                .put("updated_by", managerEmail)

            executeWriteRequest(
                url = "$baseUrl/rest/v1/students?id=eq.${student.id}",
                session = session,
                method = "PATCH",
                body = body,
            )
        }
    }

    fun startStudentRealtime(listener: StudentRealtimeListener, session: ManagerSession? = null) {
        val sessionChanged = realtimeSession?.accessToken != session?.accessToken
        realtimeListener = listener
        realtimeSession = session
        shouldReconnect = true
        if (sessionChanged && realtimeSocket != null) {
            pauseStudentRealtime()
        }
        connectRealtime()
    }

    fun pauseStudentRealtime() {
        shouldReconnect = false
        heartbeatJob?.cancel()
        heartbeatJob = null
        reconnectJob?.cancel()
        reconnectJob = null
        realtimeSocket?.close(1000, "Closed by app")
        realtimeSocket = null
    }

    fun stopStudentRealtime() {
        pauseStudentRealtime()
        realtimeListener = null
        realtimeScope.cancel()
    }

    private fun executeAuthRequest(request: Request): ManagerSession {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(body))
            }

            val auth = authAdapter.fromJson(body)
                ?: throw SupabaseException(response.code, "Unable to read login response.")

            return ManagerSession(
                email = auth.user.email.orEmpty(),
                accessToken = auth.accessToken,
                refreshToken = auth.refreshToken,
            )
        }
    }

    private fun executeWriteRequest(
        url: String,
        session: ManagerSession,
        method: String,
        body: JSONObject?,
    ) {
        val requestBody = body?.toString()?.toRequestBody(JSON_MEDIA_TYPE)
        val builder = baseRequest(url)
            .header("Authorization", "Bearer ${session.accessToken}")
            .header("Prefer", "return=representation")

        when (method) {
            "POST" -> builder.post(requireNotNull(requestBody))
            "PATCH" -> builder.patch(requireNotNull(requestBody))
            "DELETE" -> builder.delete()
            else -> error("Unsupported method $method")
        }

        client.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw SupabaseException(response.code, parseError(response.body?.string()))
            }
        }
    }

    private fun studentPayload(
        draft: StudentDraft,
        renewals: List<String>,
        addedBy: String,
        updatedBy: String,
        discontinued: Boolean,
        discontinuedAt: String?,
    ): JSONObject {
        return JSONObject()
            .put("name", draft.name.trim())
            .put("age", draft.age.toInt())
            .put("time_slot", draft.timeSlot)
            .put("join_date", draft.joinDate)
            .put("fees_paid", draft.feesPaid)
            .put("amount_paid", draft.amountPaid.toDoubleOrNull() ?: 0.0)
            .put("jersey_size", if (draft.jerseySize.isBlank()) JSONObject.NULL else draft.jerseySize)
            .put("jersey_pairs", draft.jerseyPairs.toIntOrNull() ?: 0)
            .put("renewals", JSONArray(renewals))
            .put("added_by", addedBy)
            .put("updated_by", updatedBy)
            .put("discontinued", discontinued)
            .put("discontinued_at", discontinuedAt ?: JSONObject.NULL)
    }

    private fun baseRequest(url: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
    }

    private fun parseError(rawBody: String?): String {
        if (rawBody.isNullOrBlank()) return "Something went wrong."
        return try {
            val parsed = errorAdapter.fromJson(rawBody)
            parsed?.message
                ?: parsed?.msg
                ?: parsed?.errorDescription
                ?: parsed?.error
                ?: rawBody
        } catch (_: Exception) {
            rawBody
        }
    }

    private fun connectRealtime() {
        if (realtimeSocket != null) return

        val request = Request.Builder()
            .url(realtimeWebSocketUrl())
            .build()

        realtimeSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    startHeartbeat(webSocket)
                    webSocket.send(joinPayload())
                    realtimeSession?.accessToken?.takeIf { it.isNotBlank() }?.let { accessToken ->
                        webSocket.send(accessTokenPayload(accessToken))
                    }
                    realtimeListener?.onStatus("Realtime connected.")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleRealtimeMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    realtimeSocket = null
                    heartbeatJob?.cancel()
                    realtimeListener?.onStatus("Realtime disconnected.")
                    scheduleReconnect()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    realtimeSocket = null
                    heartbeatJob?.cancel()
                    realtimeListener?.onError(t.message ?: "Realtime connection failed.")
                    scheduleReconnect()
                }
            }
        )
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect || reconnectJob?.isActive == true) return
        reconnectJob = realtimeScope.launch {
            delay(3000)
            connectRealtime()
        }
    }

    private fun startHeartbeat(webSocket: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = realtimeScope.launch {
            while (true) {
                delay(20_000)
                val heartbeat = JSONObject()
                    .put("topic", "phoenix")
                    .put("event", "heartbeat")
                    .put("payload", JSONObject())
                    .put("ref", nextRef())
                webSocket.send(heartbeat.toString())
            }
        }
    }

    private fun joinPayload(): String {
        val ref = nextRef()
        val payload = JSONObject()
            .put(
                "config",
                JSONObject()
                    .put(
                        "broadcast",
                        JSONObject()
                            .put("ack", false)
                            .put("self", false)
                    )
                    .put(
                        "presence",
                        JSONObject()
                            .put("enabled", false)
                    )
                    .put(
                        "postgres_changes",
                        JSONArray()
                            .put(
                                JSONObject()
                                    .put("event", "*")
                                    .put("schema", "public")
                                    .put("table", "students")
                            )
                            .put(
                                JSONObject()
                                    .put("event", "*")
                                    .put("schema", "public")
                                    .put("table", "attendance")
                            )
                    )
                    .put("private", false)
            )

        realtimeSession?.accessToken?.takeIf { it.isNotBlank() }?.let { accessToken ->
            payload.put("access_token", accessToken)
        }

        return JSONObject()
            .put("topic", "realtime:students")
            .put("event", "phx_join")
            .put("payload", payload)
            .put("ref", ref)
            .put("join_ref", ref)
            .toString()
    }

    private fun accessTokenPayload(accessToken: String): String {
        val ref = nextRef()
        return JSONObject()
            .put("topic", "realtime:students")
            .put("event", "access_token")
            .put("payload", JSONObject().put("access_token", accessToken))
            .put("ref", ref)
            .put("join_ref", ref)
            .toString()
    }

    private fun handleRealtimeMessage(rawMessage: String) {
        val message = runCatching { JSONObject(rawMessage) }.getOrNull() ?: return
        when (message.optString("event")) {
            "phx_reply" -> {
                val payload = message.optJSONObject("payload") ?: return
                if (payload.optString("status") == "error") {
                    realtimeListener?.onError(payload.optJSONObject("response")?.toString() ?: "Realtime subscription failed.")
                }
            }
            "postgres_changes" -> {
                val payload = message.optJSONObject("payload") ?: return
                val data = payload.optJSONObject("data") ?: return
                val changeType = data.optString("type")
                val table = data.optString("table")
                when (changeType) {
                    "DELETE" -> {
                        val oldRecord = data.optJSONObject("old_record")
                        if (table == "attendance" || oldRecord?.has("attendance_date") == true) {
                            val studentId = oldRecord?.optString("student_id").orEmpty()
                            val attendanceDate = oldRecord?.optString("attendance_date").orEmpty()
                            if (studentId.isNotBlank() && attendanceDate.isNotBlank()) {
                                realtimeListener?.onAttendanceDeleted(studentId, attendanceDate)
                            }
                        } else {
                            val deletedId = oldRecord?.optString("id").orEmpty()
                            if (deletedId.isNotBlank()) {
                                realtimeListener?.onStudentDeleted(deletedId)
                            }
                        }
                    }
                    "INSERT", "UPDATE" -> {
                        val record = data.optJSONObject("record") ?: return
                        if (table == "attendance" || record.has("attendance_date")) {
                            val studentId = record.optString("student_id")
                            val attendanceDate = record.optString("attendance_date")
                            if (studentId.isNotBlank() && attendanceDate.isNotBlank()) {
                                realtimeListener?.onAttendanceUpsert(studentId, attendanceDate)
                            }
                        } else {
                            realtimeListener?.onStudentUpsert(record.toRealtimeStudent())
                        }
                    }
                }
            }
            "system" -> {
                val payload = message.optJSONObject("payload")
                val status = payload?.optString("status").orEmpty()
                val extension = payload?.optString("extension").orEmpty()
                if (status == "error" && extension == "postgres_changes") {
                    realtimeListener?.onError(payload?.optString("message").orEmpty())
                }
            }
        }
    }

    private fun realtimeWebSocketUrl(): String {
        val socketBase = baseUrl
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
        return "$socketBase/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"
    }

    private fun nextRef(): String = refCounter.incrementAndGet().toString()

    private fun JSONObject.toRealtimeStudent(): Student {
        return Student(
            id = optSafeString("id"),
            regNo = optLongValue("reg_no"),
            name = optSafeString("name"),
            age = optIntValue("age"),
            timeSlot = optSafeString("time_slot"),
            joinDate = optSafeString("join_date"),
            feesPaid = optBoolean("fees_paid", false),
            amountPaid = optDoubleValue("amount_paid"),
            renewals = optStringList("renewals"),
            addedBy = optSafeString("added_by").ifBlank { "Unknown" },
            updatedBy = optSafeString("updated_by").ifBlank { "Unknown" },
            jerseySize = optSafeString("jersey_size"),
            jerseyPairs = optIntValue("jersey_pairs"),
            paymentMethod = optSafeString("payment_method"),
            paymentUpiId = optSafeString("payment_upi_id"),
            paymentReference = optSafeString("payment_reference"),
            comments = optSafeString("comments"),
            discontinued = optBoolean("discontinued", false),
            discontinuedAt = if (has("discontinued_at") && !isNull("discontinued_at")) {
                optString("discontinued_at", "").takeIf { it.isNotBlank() }
            } else {
                null
            },
        )
    }

    private fun JSONObject.optSafeString(key: String): String {
        return if (has(key) && !isNull(key)) optString(key, "") else ""
    }

    private fun JSONObject.optIntValue(key: String): Int {
        val raw = opt(key)
        return when (raw) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun JSONObject.optLongValue(key: String): Long? {
        if (!has(key) || isNull(key)) return null
        val raw = opt(key)
        return when (raw) {
            is Number -> raw.toLong()
            is String -> raw.toLongOrNull()
            else -> null
        }
    }

    private fun JSONObject.optDoubleValue(key: String): Double {
        val raw = opt(key)
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun JSONObject.optStringList(key: String): List<String> {
        val array = optJSONArray(key) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val value = array.optString(index)
                if (value.isNotBlank()) add(value)
            }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

interface StudentRealtimeListener {
    fun onStudentUpsert(student: Student)
    fun onStudentDeleted(studentId: String)
    fun onAttendanceUpsert(studentId: String, attendanceDate: String) {}
    fun onAttendanceDeleted(studentId: String, attendanceDate: String) {}
    fun onStatus(message: String) {}
    fun onError(message: String) {}
}

class SupabaseException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)
