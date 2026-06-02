package com.genalpha.cricketacademy.data

import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val JERSEY_PAIR_REVENUE = 750.0

private fun chargeableJerseyPairCount(pairCount: Int): Int =
    pairCount.coerceAtLeast(0)

data class Student(
    val id: String,
    val regNo: Long?,
    val name: String,
    val age: Int,
    val timeSlot: String,
    val joinDate: String,
    val feesPaid: Boolean,
    val amountPaid: Double,
    val feePlan: String,
    val coachingFee: Double,
    val admissionFee: Double,
    val jerseyAmount: Double,
    val totalFeeAmount: Double,
    val jerseySize: String,
    val jerseyPairs: Int,
    val paymentMethod: String,
    val paymentUpiId: String,
    val paymentReference: String,
    val paymentStatus: String,
    val comments: String,
    val filledBy: String,
    val fatherGuardianName: String,
    val parentContactNo: String,
    val alternateContactNo: String,
    val schoolCollege: String,
    val grade: String,
    val address: String,
    val renewals: List<String>,
    val addedBy: String,
    val updatedBy: String,
    val discontinued: Boolean,
    val discontinuedAt: String?,
    val rejoinedAt: String?,
    val feePauseDays: Int,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class StudentDraft(
    val name: String = "",
    val age: String = "",
    val timeSlot: String = "",
    val joinDate: String = "",
    val feesPaid: Boolean = true,
    val amountPaid: String = "0",
    val feePlan: String = "monthly",
    val coachingFee: String = "0",
    val admissionFee: String = "0",
    val jerseyAmount: String = "0",
    val totalFeeAmount: String = "0",
    val jerseySize: String = "",
    val jerseyPairs: String = "0",
    val paymentMethod: String = "",
    val paymentUpiId: String = "",
    val paymentReference: String = "",
    val comments: String = "",
    val fatherGuardianName: String = "",
    val parentContactNo: String = "",
    val alternateContactNo: String = "",
    val schoolCollege: String = "",
    val grade: String = "",
    val address: String = "",
)

data class AdmissionDraft(
    val applicantName: String = "",
    val filledBy: String = "Parent / Guardian",
    val nationality: String = "Indian",
    val dateOfBirth: String = "",
    val gender: String = "",
    val fatherGuardianName: String = "",
    val alternateContactNo: String = "",
    val parentContactNo: String = "",
    val city: String = "",
    val grade: String = "",
    val address: String = "",
    val schoolCollege: String = "",
    val parentAadhaarNo: String = "",
    val timeSlot: String = "",
    val joinDate: String = todayIsoDate(),
    val feesPaid: Boolean = false,
    val amountPaid: String = "0",
    val feePlan: String = "monthly",
    val coachingFee: String = "0",
    val admissionFee: String = "0",
    val jerseyAmount: String = "0",
    val totalFeeAmount: String = "0",
    val jerseySize: String = "",
    val jerseyPairs: String = "0",
    val paymentMethod: String = "UPI",
    val paymentUpiId: String = "",
    val paymentReference: String = "",
    val paymentPendingVerification: Boolean = false,
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

data class ReminderSettings(
    val whatsappRemindersEnabled: Boolean = true,
    val paymentLinksEnabled: Boolean = true,
    val dryRunMode: Boolean = false,
    val managerPhone: String = "8143960950",
)

data class AdmissionInsertResult(
    @Json(name = "id") val id: String,
    @Json(name = "reg_no") val regNo: Long,
)

data class PendingAdmission(
    val id: String,
    @Json(name = "reg_no") val regNo: Long?,
    @Json(name = "applicant_name") val applicantName: String,
    val age: Int,
    val gender: String? = "",
    @Json(name = "father_guardian_name") val fatherGuardianName: String? = "",
    @Json(name = "parent_contact_no") val parentContactNo: String? = "",
    @Json(name = "emergency_contact_no") val alternateContactNo: String? = "",
    @Json(name = "school_college") val schoolCollege: String? = "",
    val grade: String? = "",
    val city: String? = "",
    val address: String? = "",
    @Json(name = "time_slot") val timeSlot: String? = "",
    @Json(name = "join_date") val joinDate: String,
    @Json(name = "fees_paid") val feesPaid: Boolean,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "fee_plan") val feePlan: String? = "monthly",
    @Json(name = "coaching_fee") val coachingFee: Double? = 0.0,
    @Json(name = "admission_fee") val admissionFee: Double? = 0.0,
    @Json(name = "jersey_amount") val jerseyAmount: Double? = 0.0,
    @Json(name = "total_fee_amount") val totalFeeAmount: Double? = 0.0,
    @Json(name = "payment_reference") val paymentReference: String? = "",
    @Json(name = "payment_status") val paymentStatus: String? = "",
    @Json(name = "jersey_size") val jerseySize: String? = "",
    @Json(name = "jersey_pairs") val jerseyPairs: Int? = 0,
    @Json(name = "filled_by") val filledBy: String? = "Parent / Guardian",
    val comments: String? = "",
    @Json(name = "created_at") val createdAt: String? = "",
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

data class RecentAttendanceRecord(
    @Json(name = "student_id") val studentId: String,
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
    val proofUrl: String = "",
)

data class DashboardStats(
    val joinedCount: Int,
    val activeCount: Int,
    val returningCount: Int,
)

data class SlotSummary(
    val value: String,
    val label: String,
    val count: Int,
)

data class AcademyExpense(
    val id: String,
    @Json(name = "expense_type") val expenseType: String,
    val amount: Double,
    @Json(name = "expense_date") val expenseDate: String,
    @Json(name = "paid_by") val paidBy: String,
    val comment: String? = null,
)

data class StudentPayment(
    val id: String,
    @Json(name = "student_id") val studentId: String,
    val amount: Double,
    @Json(name = "paid_on") val paidOn: String,
    @Json(name = "payment_type") val paymentType: String? = "renewal",
    @Json(name = "plan_type") val planType: String? = "monthly",
    @Json(name = "cycle_start_date") val cycleStartDate: String? = "",
    @Json(name = "months_covered") val monthsCovered: Int? = 1,
    @Json(name = "coaching_fee") val coachingFee: Double? = 0.0,
    @Json(name = "admission_fee") val admissionFee: Double? = 0.0,
    @Json(name = "jersey_amount") val jerseyAmount: Double? = 0.0,
    @Json(name = "total_fee_amount") val totalFeeAmount: Double? = 0.0,
    @Json(name = "jersey_size") val jerseySize: String? = "",
    @Json(name = "jersey_pairs") val jerseyPairs: Int? = 0,
)

data class PaymentFollowUp(
    val studentId: String,
    val reminderId: String = "",
    val reminderStatus: String = "",
    val linkStatus: String = "",
    val reminderType: String = "",
    val selectedPlan: String = "",
    val amount: Double = 0.0,
    val monthsCovered: Int = 0,
    val cycleStartDate: String = "",
    val createdAt: String = "",
    val overdueDays: Int = 0,
    val failureReason: String = "",
    val failedAt: String = "",
    val retryCount: Int = 0,
    val maxRetryCount: Int = 0,
    val nextRetryAt: String = "",
    val lastRetryAt: String = "",
    val retryReason: String = "",
    val manualFollowupRequired: Boolean = false,
) {
    fun isRetryScheduled(): Boolean =
        reminderStatus == "retry_scheduled" && nextRetryAt.isNotBlank()

    fun isReminderFailed(): Boolean {
        if (isRetryScheduled()) return false
        if (reminderStatus in setOf("queued", "accepted", "sent", "delivered", "read", "payment_link_sent", "payment_attempted", "help_requested")) {
            return false
        }
        return reminderStatus in setOf("failed", "send_failed", "delivery_failed", "undelivered") ||
            linkStatus in setOf("failed", "send_failed", "delivery_failed", "undelivered") ||
            manualFollowupRequired ||
            failureReason.isNotBlank() ||
            failedAt.isNotBlank()
    }

    fun isPendingVerification(): Boolean =
        reminderStatus in setOf("payment_pending_verification", "pending_verification") ||
            linkStatus in setOf("payment_pending_verification", "pending_verification")

    fun isReminderSent(): Boolean =
        !isReminderFailed() &&
            (reminderStatus in setOf("queued", "accepted", "sent", "delivered", "read", "payment_link_sent", "payment_attempted", "help_requested") ||
            linkStatus in setOf("awaiting_parent_choice", "payment_link_sent", "payment_attempted")
            )

    fun reminderFailureLabel(): String =
        if (failureReason.isNotBlank()) "Reminder failed: $failureReason" else "Reminder failed"
}

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
    @Json(name = "fee_plan") val feePlan: String? = "monthly",
    @Json(name = "coaching_fee") val coachingFee: Double? = 0.0,
    @Json(name = "admission_fee") val admissionFee: Double? = 0.0,
    @Json(name = "jersey_amount") val jerseyAmount: Double? = 0.0,
    @Json(name = "total_fee_amount") val totalFeeAmount: Double? = 0.0,
    @Json(name = "jersey_size") val jerseySize: String? = "",
    @Json(name = "jersey_pairs") val jerseyPairs: Int? = 0,
    @Json(name = "payment_method") val paymentMethod: String? = "",
    @Json(name = "payment_upi_id") val paymentUpiId: String? = "",
    @Json(name = "payment_reference") val paymentReference: String? = "",
    @Json(name = "payment_status") val paymentStatus: String? = "",
    val comments: String? = "",
    @Json(name = "filled_by") val filledBy: String? = "",
    @Json(name = "father_guardian_name") val fatherGuardianName: String? = "",
    @Json(name = "parent_contact_no") val parentContactNo: String? = "",
    @Json(name = "alternate_contact_no") val alternateContactNo: String? = "",
    @Json(name = "school_college") val schoolCollege: String? = "",
    val grade: String? = "",
    val address: String? = "",
    val renewals: List<String>? = emptyList(),
    @Json(name = "added_by") val addedBy: String? = "Unknown",
    @Json(name = "updated_by") val updatedBy: String? = "Unknown",
    val discontinued: Boolean? = false,
    @Json(name = "discontinued_at") val discontinuedAt: String? = null,
    @Json(name = "rejoined_at") val rejoinedAt: String? = null,
    @Json(name = "fee_pause_days") val feePauseDays: Int? = 0,
    @Json(name = "created_at") val createdAt: String? = "",
    @Json(name = "updated_at") val updatedAt: String? = "",
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
    feePlan = feePlan.orEmpty().ifBlank { "monthly" },
    coachingFee = coachingFee ?: 0.0,
    admissionFee = admissionFee ?: 0.0,
    jerseyAmount = jerseyAmount ?: 0.0,
    totalFeeAmount = totalFeeAmount ?: 0.0,
    jerseySize = jerseySize.orEmpty(),
    jerseyPairs = jerseyPairs ?: 0,
    paymentMethod = paymentMethod.orEmpty(),
    paymentUpiId = paymentUpiId.orEmpty(),
    paymentReference = paymentReference.orEmpty(),
    paymentStatus = derivePaymentStatus(paymentStatus, feesPaid, amountPaid, paymentReference.orEmpty()),
    comments = comments.orEmpty(),
    filledBy = filledBy.orEmpty(),
    fatherGuardianName = fatherGuardianName.orEmpty(),
    parentContactNo = parentContactNo.orEmpty(),
    alternateContactNo = alternateContactNo.orEmpty(),
    schoolCollege = schoolCollege.orEmpty(),
    grade = grade.orEmpty(),
    address = address.orEmpty(),
    renewals = renewals.orEmpty().filter { it.isNotBlank() },
    addedBy = addedBy ?: "Unknown",
    updatedBy = updatedBy ?: addedBy ?: "Unknown",
    discontinued = discontinued == true,
    discontinuedAt = discontinuedAt,
    rejoinedAt = rejoinedAt,
    feePauseDays = (feePauseDays ?: 0).coerceAtLeast(0),
    createdAt = createdAt.orEmpty(),
    updatedAt = updatedAt.orEmpty(),
)

fun Student.toDraft(): StudentDraft = StudentDraft(
    name = name,
    age = age.toString(),
    timeSlot = timeSlot,
    joinDate = joinDate,
    feesPaid = feesPaid,
    amountPaid = if (amountPaid % 1.0 == 0.0) amountPaid.toInt().toString() else amountPaid.toString(),
    feePlan = feePlan,
    coachingFee = if (coachingFee % 1.0 == 0.0) coachingFee.toInt().toString() else coachingFee.toString(),
    admissionFee = if (admissionFee % 1.0 == 0.0) admissionFee.toInt().toString() else admissionFee.toString(),
    jerseyAmount = if (jerseyAmount % 1.0 == 0.0) jerseyAmount.toInt().toString() else jerseyAmount.toString(),
    totalFeeAmount = if (totalFeeAmount % 1.0 == 0.0) totalFeeAmount.toInt().toString() else totalFeeAmount.toString(),
    jerseySize = jerseySize,
    jerseyPairs = jerseyPairs.toString(),
    paymentMethod = paymentMethod,
    paymentUpiId = paymentUpiId,
    paymentReference = paymentReference,
    comments = comments,
    fatherGuardianName = fatherGuardianName,
    parentContactNo = parentContactNo,
    alternateContactNo = alternateContactNo,
    schoolCollege = schoolCollege,
    grade = grade,
    address = address,
)

fun derivePaymentStatus(
    rawStatus: String?,
    feesPaid: Boolean,
    amountPaid: Double,
    paymentReference: String,
): String = when {
    feesPaid || rawStatus == "paid" -> "paid"
    rawStatus == "pending_verification" -> "pending_verification"
    amountPaid > 0.0 || paymentReference.isNotBlank() -> "pending_verification"
    else -> "unpaid"
}

fun Student.isPaymentPendingVerification(): Boolean =
    !feesPaid && paymentStatus == "pending_verification"

fun Student.feeStatusLabel(): String = when {
    feesPaid -> "Fees paid"
    isPaymentPendingVerification() -> "Pending verification"
    else -> "Fees pending"
}

fun Student.feeStatusLabel(followUp: PaymentFollowUp?, payments: List<StudentPayment>): String = when {
    followUp?.isPendingVerification() == true || isPaymentPendingVerification() -> "Pending verification"
    followUp?.isRetryScheduled() == true && (isFeesPending() || isRenewalPending(payments)) -> "Retry scheduled"
    followUp?.isReminderFailed() == true && (isFeesPending() || isRenewalPending(payments)) -> "Reminder failed"
    followUp?.isReminderSent() == true && (isFeesPending() || isRenewalPending(payments)) -> "Reminder sent"
    feesPaid -> "Fees paid"
    else -> "Fees pending"
}

fun PendingAdmission.isPaymentPendingVerification(): Boolean =
    !feesPaid && derivePaymentStatus(paymentStatus, feesPaid, amountPaid, paymentReference.orEmpty()) == "pending_verification"

fun Student.referenceDate(): String = renewals.lastOrNull() ?: joinDate

fun Student.isSpecialTraining(payments: List<StudentPayment>): Boolean {
    if (payments.any { it.studentId == id && it.planType == "special" }) {
        return true
    }
    val firstPaymentAmount = kotlin.math.round(
        (amountPaid - (chargeableJerseyPairCount(jerseyPairs) * JERSEY_PAIR_REVENUE)).coerceAtLeast(0.0)
    ).toInt()
    return feesPaid && firstPaymentAmount == 10000
}

fun Student.nextRenewalCycleDate(payments: List<StudentPayment>): String = paidThroughDate(payments)

fun Student.paidThroughDate(payments: List<StudentPayment>): String {
    var paidUntil = if (feesPaid) {
        addMonths(joinDate, initialMonthsCovered())
    } else {
        joinDate
    }

    renewals.forEach { renewalDate ->
        paidUntil = maxIsoDate(paidUntil, addMonths(renewalDate, 1))
    }

    payments
        .filter { it.studentId == id }
        .filter { it.paymentType == "joining" || it.paymentType == "renewal" }
        .forEach { payment ->
            val cycleStart = payment.cycleStartDate?.takeIf { it.isNotBlank() } ?: payment.paidOn
            val months = payment.monthsCoveredForDueDate()
            paidUntil = maxIsoDate(paidUntil, addMonths(cycleStart, months))
        }

    return if (feePauseDays > 0) addDays(paidUntil, feePauseDays) else paidUntil
}

private fun StudentPayment.monthsCoveredForDueDate(): Int {
    val explicitMonths = (monthsCovered ?: 1).coerceAtLeast(1)
    val planMonths = when (planType) {
        "quarterly" -> 3
        "halfyearly" -> 6
        else -> 1
    }
    val roundedAmount = kotlin.math.round(amount).toInt()
    val amountMonths = when (roundedAmount) {
        18900, 19400, 20000, 20500, 21000 -> 6
        9000, 9500, 9975, 10475, 10500, 11000 -> 3
        else -> 1
    }
    return maxOf(explicitMonths, planMonths, amountMonths)
}

fun Student.isRenewalPending(payments: List<StudentPayment>): Boolean {
    return isActive() && feesPaid && daysSince(paidThroughDate(payments)) >= 0
}

fun Student.renewalStatus(payments: List<StudentPayment>): String = when {
    discontinued -> "Tracking paused"
    isPaymentPendingVerification() -> "Payment pending verification"
    !feesPaid -> "Join fee pending"
    else -> renewalLabelForDueDate(paidThroughDate(payments))
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

fun Student.membershipDateLabel(): String {
    val joined = "Joined ${displayDate(joinDate)}"
    return rejoinedAt?.takeIf { it.isNotBlank() }?.let { "$joined • Rejoined ${displayDate(it)}" } ?: joined
}

fun Student.tenureBadgeLabel(): String {
    val days = daysSince(joinDate).coerceAtLeast(0)
    val months = days / 30
    return if (months <= 0) "${days}d" else "${months}m"
}

fun Student.studentType(): String = if (renewals.isNotEmpty()) "Returning" else "New"

fun Student.regNoLabel(): String? = regNo?.let { "Reg #$it" }

fun Student.isActive(): Boolean = !discontinued

fun Student.isFeesPending(): Boolean = isActive() && !feesPaid

fun Student.latestRenewal(payments: List<StudentPayment>): String? {
    val legacy = renewals.lastOrNull()
    val fromLedger = payments
        .filter { it.studentId == id && it.paymentType == "renewal" }
        .maxByOrNull { it.paidOn }?.paidOn
    return if (fromLedger != null && legacy != null) {
        if (fromLedger > legacy) fromLedger else legacy
    } else fromLedger ?: legacy
}


fun Student.cardTimelineLabel(payments: List<StudentPayment>): String? = when {
    discontinued -> discontinuedAt?.let { "Discontinued ${displayDate(it)}" } ?: "Discontinued"
    else -> latestRenewal(payments)?.let { "Renewed ${displayDate(it)}" }
}


fun Student.trackingCaption(payments: List<StudentPayment>): String = if (discontinued) {
    discontinuedAt?.let { "Discontinued on ${displayDate(it)}. Renewal tracking paused." }
        ?: "Removed from active renewal tracking."
} else {
    val ledgerCount = payments.count { it.studentId == id && it.paymentType == "renewal" }
    val totalRenewals = renewals.size + ledgerCount
    "Tracking from ${displayDate(referenceDate())}. $totalRenewals renewal${if (totalRenewals == 1) "" else "s"} recorded."
}


fun buildStats(students: List<Student>): DashboardStats {
    val active = students.filter { it.isActive() }
    return DashboardStats(
        joinedCount = students.size,
        activeCount = active.size,
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

fun displayTimelineStamp(createdAt: String?, eventDate: String?): String {
    val dateSource = eventDate?.takeIf { it.isNotBlank() }
        ?: createdAt?.takeIf { it.isNotBlank() }?.take(10)
    val dateLabel = dateSource?.let { displayDate(it) } ?: "No date"
    val timeLabel = parseTimelineDateTime(createdAt)?.let { DISPLAY_TIME_FORMAT.format(it) }.orEmpty()
    return if (timeLabel.isBlank()) dateLabel else "$dateLabel • $timeLabel"
}

private fun parseTimelineDateTime(value: String?): Date? {
    if (value.isNullOrBlank() || value.length < 19) return null
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd HH:mm:ss.SSSXXX",
        "yyyy-MM-dd HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        try {
            SimpleDateFormat(pattern, Locale.US).apply {
                if (pattern.endsWith("'Z'")) timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value)
        } catch (_: Exception) {
            null
        }
    }
}

fun daysBetweenIso(startValue: String?, endValue: String?): Int {
    return try {
        val start = ISO_DATE_FORMAT.parse(startValue?.take(10).orEmpty()) ?: return 0
        val end = ISO_DATE_FORMAT.parse(endValue?.take(10).orEmpty()) ?: return 0
        if (!end.after(start)) return 0
        TimeUnit.MILLISECONDS.toDays(end.time - start.time).toInt()
    } catch (_: Exception) {
        0
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

private fun Student.initialMonthsCovered(): Int {
    if (!feesPaid || amountPaid <= 0.0) return 0
    val feeOnlyAmount = (amountPaid - (chargeableJerseyPairCount(jerseyPairs) * JERSEY_PAIR_REVENUE)).coerceAtLeast(0.0)
    val withoutAdmissionFee = (feeOnlyAmount - 500.0).coerceAtLeast(0.0)
    val roundedAmount = kotlin.math.round(feeOnlyAmount).toInt()
    return when {
        roundedAmount == 10000 -> 1
        withoutAdmissionFee >= 18900.0 || roundedAmount in setOf(18900, 19400, 20000, 20500, 21000) -> 6
        roundedAmount in setOf(9000, 9500, 9975, 10475, 10500, 11000) ||
            withoutAdmissionFee in 9000.0..10500.0 -> 3
        else -> 1
    }
}

private fun renewalLabelForDueDate(dueDate: String): String {
    val daysPastDue = daysSince(dueDate)
    return when {
        daysPastDue > 1 -> "$daysPastDue days overdue"
        daysPastDue == 1 -> "1 day overdue"
        daysPastDue == 0 -> "Due today"
        daysPastDue == -1 -> "1 day left"
        else -> "${-daysPastDue} days left"
    }
}

private fun maxIsoDate(first: String, second: String): String {
    return try {
        val firstDate = ISO_DATE_FORMAT.parse(first)
        val secondDate = ISO_DATE_FORMAT.parse(second)
        if (firstDate != null && secondDate != null && secondDate.after(firstDate)) second else first
    } catch (_: Exception) {
        first
    }
}

private fun addMonths(value: String, months: Int): String {
    return try {
        val parsed = ISO_DATE_FORMAT.parse(value) ?: return value
        val calendar = Calendar.getInstance().apply { time = parsed }
        calendar.add(Calendar.MONTH, months)
        ISO_DATE_FORMAT.format(calendar.time)
    } catch (_: Exception) {
        value
    }
}

fun addMonthsForPlan(value: String, months: Int): String = addMonths(value, months)

fun addDays(value: String, days: Int): String {
    return try {
        val parsed = ISO_DATE_FORMAT.parse(value) ?: return value
        val calendar = Calendar.getInstance().apply { time = parsed }
        calendar.add(Calendar.DAY_OF_MONTH, days)
        ISO_DATE_FORMAT.format(calendar.time)
    } catch (_: Exception) {
        value
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
private val DISPLAY_TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale("en", "IN")).apply {
    timeZone = TimeZone.getTimeZone("Asia/Kolkata")
}
