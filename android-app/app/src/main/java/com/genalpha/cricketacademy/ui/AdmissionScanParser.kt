package com.genalpha.cricketacademy.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.genalpha.cricketacademy.data.AdmissionDraft
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val SLOT_PATTERNS = listOf(
    "6:00" to "6AM",
    "06:00" to "6AM",
    "7:30" to "7:30AM",
    "07:30" to "7:30AM",
    "4:00" to "4PM",
    "04:00" to "4PM",
    "5:30" to "5:30PM",
    "05:30" to "5:30PM",
    "7:00" to "7PM",
    "07:00" to "7PM",
)

private val MONTH_MAP = mapOf(
    "jan" to "01",
    "feb" to "02",
    "mar" to "03",
    "apr" to "04",
    "may" to "05",
    "jun" to "06",
    "jul" to "07",
    "aug" to "08",
    "sep" to "09",
    "oct" to "10",
    "nov" to "11",
    "dec" to "12",
)

private data class ParsedDate(
    val iso: String,
    val day: String,
    val monthLabel: String,
    val year: String,
)

suspend fun extractAdmissionDraftFromImage(
    context: Context,
    uri: Uri,
): AdmissionDraft {
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val text = suspendCancellableCoroutine<String> { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { result -> continuation.resume(result.text) }
            .addOnFailureListener { error -> continuation.resumeWithException(error) }
    }
    recognizer.close()
    return parseAdmissionText(text)
}

suspend fun extractAdmissionDraftFromBitmap(
    bitmap: Bitmap,
): AdmissionDraft {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val text = suspendCancellableCoroutine<String> { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { result -> continuation.resume(result.text) }
            .addOnFailureListener { error -> continuation.resumeWithException(error) }
    }
    recognizer.close()
    return parseAdmissionText(text)
}

fun parseAdmissionText(rawText: String): AdmissionDraft {
    val normalized = rawText
        .replace('\r', '\n')
        .replace(Regex("[ \t]+"), " ")
        .replace(Regex("\\n+"), "\n")
        .trim()
    val lines = normalized.lines().map { it.trim() }.filter { it.isNotBlank() }
    val full = lines.joinToString("\n")
    val lower = full.lowercase(Locale.getDefault())

    val applicantName = extractLabeledValue(
        full,
        listOf("Applicant's Full Name", "Applicants Full Name", "Applicant Full Name", "Student Name", "Player Name", "Name of the Student"),
        listOf("Nationality", "Date of Birth", "Gender", "Father", "Guardian")
    )
    val nationality = extractLabeledValue(
        full,
        listOf("Nationality"),
        listOf("Date of Birth", "Gender")
    ).ifBlank { "Indian" }
    val gender = listOf("Male", "Female", "Other").firstOrNull {
        Regex("\\b$it\\b", RegexOption.IGNORE_CASE).containsMatchIn(full)
    }.orEmpty()
    val fatherGuardianName = extractLabeledValue(
        full,
        listOf("Father's / Guardian's Name", "Fathers / Guardians Name", "Father / Guardian Name", "Parent Name", "Guardian Name", "Father Name"),
        listOf("Alternate Contact No", "Emergency Contact No", "Parent's Contact No", "Parents Contact No", "Contact No", "Phone")
    )
    val alternateContact = firstPhoneNearLabels(
        full,
        listOf("Alternate Contact No", "Emergency Contact No")
    )
    val parentContact = firstPhoneNearLabels(
        full,
        listOf("Parent's Contact No", "Parents Contact No", "Parent Contact No", "Contact No", "Phone", "Mobile")
    )
    val city = extractLabeledValue(
        full,
        listOf("City"),
        listOf("Address", "School / College", "School/College")
    )
    val address = extractLabeledValue(
        full,
        listOf("Address", "Residential Address"),
        listOf("School / College", "School/College", "Parent's NIDA / Aadhaar No", "Parent's NIDA/Aadhaar No")
    )
    val schoolCollege = extractLabeledValue(
        full,
        listOf("School / College", "School/College", "School", "College"),
        listOf("Parent's NIDA / Aadhaar No", "Parent's NIDA/Aadhaar No", "Skills", "Batsman")
    )
    val aadhaar = firstNumberNearLabels(
        full,
        listOf("Parent's NIDA / Aadhaar No", "Parent's NIDA/Aadhaar No", "Parent Aadhaar No", "Parent Aadhaar Number")
    )
    val batsmanStyle = when {
        lower.contains("left handed batsman") || lower.contains("left-handed batsman") -> "Left-handed batsman"
        lower.contains("right handed batsman") || lower.contains("right-handed batsman") -> "Right-handed batsman"
        else -> ""
    }
    val bowlingStyles = listOfNotNull(
        parseStyle(lower, "right arm fast bowler", "Right-arm fast bowler"),
        parseStyle(lower, "left arm fast bowler", "Left-arm fast bowler"),
        parseStyle(lower, "right arm off spinner", "Right-arm off spinner"),
        parseStyle(lower, "left arm leg spinner", "Left-arm leg spinner"),
    )
    val timeSlot = SLOT_PATTERNS.firstOrNull { lower.contains(it.first.lowercase(Locale.getDefault())) }?.second.orEmpty()
    val feesPaid = Regex("""fee\s*paid\s*[:\-]?\s*(yes|paid|done)""", RegexOption.IGNORE_CASE).containsMatchIn(full)
    val amountPaid = Regex("""(?:fee\s*paid|amount)\D{0,8}(\d{2,6})""", RegexOption.IGNORE_CASE)
        .find(full)
        ?.groupValues
        ?.getOrNull(1)
        ?: "0"

    val dob = findDateNearLabels(
        full,
        listOf("Date of Birth", "Date of Birth / Age", "DOB")
    )

    return AdmissionDraft(
        applicantName = applicantName,
        nationality = nationality,
        dateOfBirth = dob?.iso.orEmpty(),
        gender = gender,
        fatherGuardianName = fatherGuardianName,
        emergencyContactNo = alternateContact,
        parentContactNo = parentContact,
        city = city,
        address = address,
        schoolCollege = schoolCollege,
        parentAadhaarNo = aadhaar,
        timeSlot = timeSlot,
        feesPaid = feesPaid,
        amountPaid = if (feesPaid) amountPaid else "0",
        batsmanStyle = batsmanStyle,
        bowlingStyles = bowlingStyles,
        readyToStartNow = false,
        consentAccepted = false,
        termsAccepted = false,
    )
}

fun splitAdmissionDob(isoDate: String): Triple<String, String, String> {
    val match = Regex("""(\d{4})-(\d{2})-(\d{2})""").find(isoDate) ?: return Triple("", "", "")
    val year = match.groupValues[1]
    val month = match.groupValues[2]
    val day = match.groupValues[3]
    val monthLabel = MONTH_MAP.entries.firstOrNull { it.value == month }?.key
        ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        .orEmpty()
    return Triple(day.toIntOrNull()?.toString().orEmpty(), monthLabel, year)
}

private fun parseStyle(source: String, phrase: String, mapped: String): String? {
    return if (source.contains(phrase)) mapped else null
}

private fun firstPhoneNearLabels(full: String, labels: List<String>): String {
    labels.forEach { label ->
        val regex = Regex("""${Regex.escape(label)}[^0-9]{0,12}([0-9 ]{8,15})""", RegexOption.IGNORE_CASE)
        val value = regex.find(full)?.groupValues?.getOrNull(1)?.filter(Char::isDigit).orEmpty()
        if (value.length >= 8) return value
    }
    return ""
}

private fun firstNumberNearLabels(full: String, labels: List<String>): String {
    labels.forEach { label ->
        val regex = Regex("""${Regex.escape(label)}[^0-9]{0,12}([0-9 ]{4,16})""", RegexOption.IGNORE_CASE)
        val value = regex.find(full)?.groupValues?.getOrNull(1)?.filter(Char::isDigit).orEmpty()
        if (value.isNotBlank()) return value
    }
    return ""
}

private fun extractLabeledValue(
    full: String,
    labels: List<String>,
    stopLabels: List<String>,
): String {
    labels.forEach { label ->
        val pattern = buildString {
            append(Regex.escape(label))
            append("""\s*[:\-]?\s*(.*?)""")
            if (stopLabels.isNotEmpty()) {
                append("""(?=\s*(?:${stopLabels.joinToString("|") { Regex.escape(it) }})|$)""")
            } else {
                append("$")
            }
        }
        val match = Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)).find(full)
        val value = match?.groupValues?.getOrNull(1)?.trim(' ', '.', ':', '-', '\n').orEmpty()
        if (value.isNotBlank()) return value.lineSequence().first().trim()
    }
    return ""
}

private fun findDateNearLabels(full: String, labels: List<String>): ParsedDate? {
    labels.forEach { label ->
        val afterLabel = Regex("""${Regex.escape(label)}\s*[:\-]?\s*([A-Za-z0-9/\- ]{6,24})""", RegexOption.IGNORE_CASE)
            .find(full)
            ?.groupValues
            ?.getOrNull(1)
            .orEmpty()
        parseDate(afterLabel)?.let { return it }
    }
    return parseDate(full)
}

private fun parseDate(text: String): ParsedDate? {
    val slashMatch = Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})""").find(text)
    if (slashMatch != null) {
        val day = slashMatch.groupValues[1].padStart(2, '0')
        val month = slashMatch.groupValues[2].padStart(2, '0')
        val yearRaw = slashMatch.groupValues[3]
        val year = if (yearRaw.length == 2) "20$yearRaw" else yearRaw
        return ParsedDate(
            iso = "$year-$month-$day",
            day = day.toInt().toString(),
            monthLabel = MONTH_MAP.entries.firstOrNull { it.value == month }?.key
                ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                .orEmpty(),
            year = year,
        )
    }

    val monthMatch = Regex("""(\d{1,2})\s+([A-Za-z]{3,9})\s+(\d{4})""", RegexOption.IGNORE_CASE).find(text)
    if (monthMatch != null) {
        val day = monthMatch.groupValues[1].padStart(2, '0')
        val monthName = monthMatch.groupValues[2].take(3).lowercase(Locale.getDefault())
        val month = MONTH_MAP[monthName] ?: return null
        val year = monthMatch.groupValues[3]
        return ParsedDate(
            iso = "$year-$month-$day",
            day = day.toInt().toString(),
            monthLabel = monthName.replaceFirstChar { it.titlecase(Locale.getDefault()) },
            year = year,
        )
    }
    return null
}
