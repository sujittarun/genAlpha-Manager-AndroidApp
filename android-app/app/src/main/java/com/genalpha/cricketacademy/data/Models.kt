package com.genalpha.cricketacademy.data

import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Student(
    val id: String,
    val regNo: Long?,
    val name: String,
    val age: Int,
    val timeSlot: String,
    val joinDate: String,
    val feesPaid: Boolean,
    val amountPaid: Double,
    val jerseySize: String,
    val jerseyPairs: Int,
    val paymentMethod: String,
    val paymentUpiId: String,
    val paymentReference: String,
    val comments: String,
    val fatherGuardianName: String,
    val parentContactNo: String,
    val alternateContactNo: String,
    val renewals: List<String>,
    val addedBy: String,
    val updatedBy: String,
    val discontinued: Boolean,
    val discontinuedAt: String?,
)

data class StudentDraft(
    val name: String = "",
    val age: String = "",
    val timeSlot: String = "",
    val joinDate: String = "",
    val feesPaid: Boolean = true,
    val amountPaid: String = "0",
    val jerseySize: String = "",
    val jerseyPairs: String = "0",
    val paymentMethod: String = "",
    val paymentUpiId: String = "",
    val paymentReference: String = "",
    val comments: String = "",
)

data class AdmissionDraft(
    val applicantName: String = "",
    val nationality: String = "Indian",
    val dateOfBirth: String = "",
    val gender: String = "",
    val fatherGuardianName: String = "",
    val alternateContactNo: String = "",
    val parentContactNo: String = "",
    val city: String = "",
    val address: String = "",
    val schoolCollege: String = "",
    val parentAadhaarNo: String = "",
    val timeSlot: String = "",
    val joinDate: String = todayIsoDate(),
    val feesPaid: Boolean = false,
    val amountPaid: String = "0",
    val jerseySize: String = "",
    val jerseyPairs: String = "0",
    val paymentMethod: String = "UPI",
    val paymentUpiId: String = "",
    val paymentReference: String = "",
    val comments: String = "",
    val batsmanStyle: String = "",
    val bowlingStyles: List<String> = emptyList(),
    val readyToStartNow: Boolean = false,
    val consentAccepted: Boolean = false,
    val termsAccepted: Boolean = false,
)

data class ManagerSession(
    val email: String,
    val accessToken: String,
    val refreshToken: String,
)

data class OperationResult(
    val success: Boolean,
    val message: String,
)

data class AdmissionInsertResult(
    @Json(name = "id") val id: String,
    @Json(name = "reg_no") val regNo: Long,
)

data class AdmissionRegPreview(
    @Json(name = "next_reg_no") val nextRegNo: Long,
)

data class AttendanceRecord(
    @Json(name = "student_id") val studentId: String,
)

data class AttendanceDateRecord(
    @Json(name = "attendance_date") val attendanceDate: String,
)

data class StudentTimelineItem(
    val id: String,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "event_type") val eventType: String,
    @Json(name = "event_date") val eventDate: String,
    val title: String,
    val details: String? = "",
    @Json(name = "changed_by") val changedBy: String? = "System",
    @Json(name = "created_at") val createdAt: String? = "",
)

data class DashboardStats(
    val joinedCount: Int,
    val activeCount: Int,
    val paidCount: Int,
    val returningCount: Int,
)

data class SlotSummary(
    val value: String,
    val label: String,
    val count: Int,
)

data class AuthPayload(
    val email: String,
    val password: String,
)

data class AuthResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    val user: AuthUser,
)

data class AuthUser(
    val email: String?,
)

data class ErrorResponse(
    val msg: String? = null,
    val message: String? = null,
    @Json(name = "error_description") val errorDescription: String? = null,
    val error: String? = null,
)

data class StudentDto(
    val id: String,
    @Json(name = "reg_no") val regNo: Long? = null,
    val name: String,
    val age: Int,
    @Json(name = "time_slot") val timeSlot: String? = "",
    @Json(name = "join_date") val joinDate: String,
    @Json(name = "fees_paid") val feesPaid: Boolean,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "jersey_size") val jerseySize: String? = "",
    @Json(name = "jersey_pairs") val jerseyPairs: Int? = 0,
    @Json(name = "payment_method") val paymentMethod: String? = "",
    @Json(name = "payment_upi_id") val paymentUpiId: String? = "",
    @Json(name = "payment_reference") val paymentReference: String? = "",
    val comments: String? = "",
    @Json(name = "father_guardian_name") val fatherGuardianName: String? = "",
    @Json(name = "parent_contact_no") val parentContactNo: String? = "",
    @Json(name = "alternate_contact_no") val alternateContactNo: String? = "",
    val renewals: List<String>? = emptyList(),
    @Json(name = "added_by") val addedBy: String? = "Unknown",
    @Json(name = "updated_by") val updatedBy: String? = "Unknown",
    val discontinued: Boolean? = false,
    @Json(name = "discontinued_at") val discontinuedAt: String? = null,
)

fun StudentDto.toDomain(): Student = Student(
    id = id,
    regNo = regNo,
    name = name,
    age = age,
    timeSlot = timeSlot.orEmpty(),
    joinDate = joinDate,
    feesPaid = feesPaid,
    amountPaid = amountPaid,
    jerseySize = jerseySize.orEmpty(),
    jerseyPairs = jerseyPairs ?: 0,
    paymentMethod = paymentMethod.orEmpty(),
    paymentUpiId = paymentUpiId.orEmpty(),
    paymentReference = paymentReference.orEmpty(),
    comments = comments.orEmpty(),
    fatherGuardianName = fatherGuardianName.orEmpty(),
    parentContactNo = parentContactNo.orEmpty(),
    alternateContactNo = alternateContactNo.orEmpty(),
    renewals = renewals.orEmpty().filter { it.isNotBlank() },
    addedBy = addedBy ?: "Unknown",
    updatedBy = updatedBy ?: addedBy ?: "Unknown",
    discontinued = discontinued == true,
    discontinuedAt = discontinuedAt,
)

fun Student.toDraft(): StudentDraft = StudentDraft(
    name = name,
    age = age.toString(),
    timeSlot = timeSlot,
    joinDate = joinDate,
    feesPaid = feesPaid,
    amountPaid = if (amountPaid % 1.0 == 0.0) amountPaid.toInt().toString() else amountPaid.toString(),
    jerseySize = jerseySize,
    jerseyPairs = jerseyPairs.toString(),
    paymentMethod = paymentMethod,
    paymentUpiId = paymentUpiId,
    paymentReference = paymentReference,
    comments = comments,
)

fun Student.referenceDate(): String = renewals.lastOrNull() ?: joinDate

fun Student.daysSinceReference(): Int = daysSince(referenceDate())

fun Student.nextRenewalCycleDate(): String {
    var cycleDate = referenceDate()
    while (daysSince(cycleDate) >= 30) {
        cycleDate = addDays(cycleDate, 30)
    }
    return cycleDate
}

fun Student.trainingDurationLabel(): String {
    val days = daysSince(joinDate).coerceAtLeast(0)
    val months = days / 30
    val remainingDays = days % 30
    return if (months <= 0) {
        "$days day${if (days == 1) "" else "s"}"
    } else {
        "$months month${if (months == 1) "" else "s"}, $remainingDays day${if (remainingDays == 1) "" else "s"}"
    }
}

fun Student.studentType(): String = if (renewals.isNotEmpty()) "Returning" else "New"

fun Student.regNoLabel(): String? = regNo?.let { "Reg #$it" }

fun Student.isActive(): Boolean = !discontinued

fun Student.isFeesPending(): Boolean = isActive() && !feesPaid

fun Student.isRenewalPending(): Boolean = isActive() && daysSinceReference() >= 30

fun Student.latestRenewal(): String? = renewals.lastOrNull()

fun Student.cardTimelineLabel(): String? = when {
    discontinued -> discontinuedAt?.let { "Discontinued ${displayDate(it)}" } ?: "Discontinued"
    renewals.isNotEmpty() -> latestRenewal()?.let { "Renewed ${displayDate(it)}" }
    else -> null
}

fun Student.renewalStatus(): String = when {
    discontinued -> "Tracking paused"
    isRenewalPending() -> overdueRenewalLabel()
    else -> remainingRenewalLabel()
}

fun Student.trackingCaption(): String = if (discontinued) {
    discontinuedAt?.let { "Discontinued on ${displayDate(it)}. Renewal tracking paused." }
        ?: "Removed from active renewal tracking."
} else {
    "Tracking from ${displayDate(referenceDate())}. ${renewals.size} renewal${if (renewals.size == 1) "" else "s"} recorded."
}

fun buildStats(students: List<Student>): DashboardStats {
    val active = students.filter { it.isActive() }
    return DashboardStats(
        joinedCount = students.size,
        activeCount = active.size,
        paidCount = active.count { it.feesPaid },
        returningCount = active.count { it.renewals.isNotEmpty() },
    )
}

fun buildSlotSummary(students: List<Student>, timeSlots: List<String>, selected: String): List<SlotSummary> {
    val active = students.filter { it.isActive() }
    val summaries = mutableListOf(
        SlotSummary(
            value = "all",
            label = "All",
            count = students.size,
        )
    )

    summaries += timeSlots.map { slot ->
        SlotSummary(
            value = slot,
            label = slot,
            count = active.count { it.timeSlot == slot },
        )
    }

    val notSetCount = active.count { it.timeSlot.isBlank() }
    if (notSetCount > 0 || selected == "not-set") {
        summaries += SlotSummary(
            value = "not-set",
            label = "Not set",
            count = notSetCount,
        )
    }

    return summaries
}

fun displayDate(value: String?): String {
    if (value.isNullOrBlank()) return "Not renewed"
    return try {
        val parsed = ISO_DATE_FORMAT.parse(value) ?: return value
        DISPLAY_DATE_FORMAT.format(parsed)
    } catch (_: Exception) {
        value
    }
}

fun daysSince(value: String): Int {
    return try {
        val parsed = ISO_DATE_FORMAT.parse(value) ?: return 0
        val diff = Date().time - parsed.time
        TimeUnit.MILLISECONDS.toDays(diff).toInt()
    } catch (_: Exception) {
        0
    }
}

fun todayIsoDate(): String = ISO_DATE_FORMAT.format(Date())

fun isFutureDate(value: String): Boolean {
    return try {
        val parsed = ISO_DATE_FORMAT.parse(value) ?: return false
        parsed.after(stripTime(Date()))
    } catch (_: Exception) {
        false
    }
}

fun currentDatePickerValues(existingIsoDate: String?): Triple<Int, Int, Int> {
    val calendar = Calendar.getInstance()
    if (!existingIsoDate.isNullOrBlank()) {
        try {
            ISO_DATE_FORMAT.parse(existingIsoDate)?.let { calendar.time = it }
        } catch (_: Exception) {
        }
    }

    return Triple(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    )
}

fun calculateAgeFromDate(dateOfBirth: String): Int? {
    return try {
        val birthDate = ISO_DATE_FORMAT.parse(dateOfBirth) ?: return null
        val birthCalendar = Calendar.getInstance().apply { time = birthDate }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        age
    } catch (_: Exception) {
        null
    }
}

private fun Student.remainingRenewalLabel(): String {
    val daysLeft = (30 - daysSinceReference()).coerceAtLeast(0)
    return when (daysLeft) {
        0 -> "Due today"
        1 -> "1 day left"
        else -> "$daysLeft days left"
    }
}

private fun addDays(value: String, days: Int): String {
    return try {
        val parsed = ISO_DATE_FORMAT.parse(value) ?: return value
        val calendar = Calendar.getInstance().apply { time = parsed }
        calendar.add(Calendar.DAY_OF_MONTH, days)
        ISO_DATE_FORMAT.format(calendar.time)
    } catch (_: Exception) {
        value
    }
}

private fun Student.overdueRenewalLabel(): String {
    val overdueDays = (daysSinceReference() - 30).coerceAtLeast(0)
    return when (overdueDays) {
        0 -> "Due today"
        1 -> "1 day overdue"
        else -> "$overdueDays days overdue"
    }
}

private fun stripTime(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val DISPLAY_DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale("en", "IN"))
