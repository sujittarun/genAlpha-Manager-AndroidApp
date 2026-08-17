package com.genalpha.cricketacademy.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Kotlin mirror of GenAlpha/timeline-rules.js. Same rules, same order, same
 * outcomes — pinned by timeline-fixtures.json, which both suites load and
 * which timeline-rules.test.js asserts is byte-identical across the two
 * repos.
 *
 * The problem this exists to solve, measured on 2026-08-17 across 84
 * students: a median of 34 rendered rows per player, p90 64, worst 124 —
 * against a render cap of 30 here and 12 on this app, so the cap was doing
 * the editing, by recency, and useful rows fell off the end while noise
 * survived. One real-world fact was recorded by three subsystems and the
 * timeline printed all three.
 *
 * If you change a rule here, change it in timeline-rules.js in the same
 * commit. The fixtures will tell you if you did not.
 */
object TimelineRules {

    private val DELIVERY_ORDER = listOf("none", "queued", "sent", "delivered", "read")
    private val CONTACT_TOKENS = listOf("parent phone", "phone", "alternate phone", "whatsapp")
    private val MONEY_TOKENS = listOf("fee", "fees", "plan", "amount", "discount")
    private val IST = ZoneId.of("Asia/Kolkata")
    private val DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(IST)

    data class Classification(
        val kind: String,
        val rank: Int,
        val factKey: String,
        val runGroup: String,
        val keep: Boolean,
        val isProtected: Boolean,
    )

    private val DROP = Classification("drop", 999, "", "", keep = false, isProtected = false)

    /**
     * The Asia/Kolkata calendar day. Never a UTC string slice — that is why a
     * row at 19:41 IST printed yesterday's date beside today's clock.
     */
    fun istDay(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return try {
            DAY.format(Instant.parse(value))
        } catch (_: Exception) {
            value.take(10)
        }
    }

    /**
     * Derived from the *_at timestamps, never from the status column: the
     * webhook patches status unconditionally, so a late `delivered` callback
     * arriving after a `failed` one overwrites it and the row claims success.
     * A timestamp, once written, is not taken back.
     */
    fun deliveryState(row: StudentTimelineItem): String = when {
        row.failedAt.isNotBlank() -> "failed"
        row.readAt.isNotBlank() -> "read"
        row.deliveredAt.isNotBlank() -> "delivered"
        row.sentAt.isNotBlank() -> "sent"
        row.acceptedAt.isNotBlank() -> "queued"
        else -> "none"
    }

    fun bestDelivery(a: String, b: String): String {
        if (a == "failed" || b == "failed") return "failed"
        return if (DELIVERY_ORDER.indexOf(a) >= DELIVERY_ORDER.indexOf(b)) a else b
    }

    /** "Changed: join date, parent phone." — our own trigger's wording, read as a token list. */
    fun profileChangeTokens(details: String?): List<String> {
        val match = Regex("changed:\\s*([^.]*)", RegexOption.IGNORE_CASE).find(details.orEmpty())
            ?: return emptyList()
        return match.groupValues[1].split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }
    }

    private fun has(tokens: List<String>, needles: List<String>) =
        tokens.any { token -> needles.any { token.contains(it) } }

    fun classifyTimelineRow(row: StudentTimelineItem): Classification {
        val type = row.eventType
        val status = row.status
        val kind = row.messageKind
        val day = istDay(row.createdAt)

        if (row.source == "payment") {
            // Money is rendered from the typed payment row, never from a
            // timeline sentence about it: 122 of 143 payments had no same-day
            // timeline echo, so matching echoes to payments is not possible —
            // and does not need to be, once the payment itself is the row.
            return Classification("payment", 0, "pay:${row.id}", "", keep = true, isProtected = true)
        }
        if (row.source == "reminder") {
            return Classification("failure", 40, "fail:reminder:$day", "", keep = true, isProtected = true)
        }

        if (row.source == "flow") {
            return when {
                type == "reminder_created" ->
                    Classification("reminder", 10, "rem:$day", "reminder", keep = true, isProtected = false)
                // Not a second message — it travels inside the reminder.
                type == "payment_link_sent" ->
                    Classification("reminder", 20, "rem:$day", "reminder", keep = false, isProtected = false)
                type == "reminder_send_failed" ->
                    Classification("failure", 15, "fail:reminder:$day", "", keep = true, isProtected = true)
                type == "reminder_message_status" && status == "failed" ->
                    Classification("failure", 10, "fail:reminder:$day", "", keep = true, isProtected = true)
                type == "reminder_message_status" ->
                    Classification("delivery", 50, "rem:$day", "", keep = false, isProtected = false)
                // Whether a message to the OWNER reached the owner, on the
                // phone he is reading this on. Nothing about a parent, a
                // payment or a number is lost.
                type == "whatsapp_message_status" && kind.contains("manager_alert") -> DROP
                type == "whatsapp_message_status" && status == "failed" ->
                    Classification(
                        "failure", 12,
                        if (kind == "payment_link") "fail:reminder:$day"
                        else "fail:${kind.ifBlank { "whatsapp" }}:$day",
                        "", keep = true, isProtected = true,
                    )
                type == "whatsapp_message_status" && kind == "payment_link" ->
                    Classification("delivery", 50, "rem:$day", "", keep = false, isProtected = false)
                type == "whatsapp_message_status" -> DROP
                // A receipt that did NOT arrive is the parent not knowing they
                // are paid up, which is a call the owner has to make. One that
                // did is not news.
                type == "confirmation_message_status" && status == "failed" ->
                    Classification("failure", 12, "fail:receipt:$day", "", keep = true, isProtected = true)
                type == "confirmation_message_status" -> DROP
                type == "payment_pending_verification" ->
                    Classification("proof", 5, "proof:${row.id}", "", keep = true, isProtected = true)
                type == "parent_help_requested" ->
                    Classification("help", 5, "help:$day", "", keep = true, isProtected = false)
                // The parent got as far as the payment page and stopped. One
                // row for the whole stall, whichever signals fired.
                type == "upi_app_not_opened" ->
                    Classification("stall", 20, "stall:$day", "", keep = true, isProtected = false)
                type == "payment_attempted" ->
                    Classification("stall", 25, "stall:$day", "", keep = true, isProtected = false)
                type == "parent_plan_selected" ->
                    Classification("stall", 30, "stall:$day", "", keep = true, isProtected = false)
                else -> DROP
            }
        }

        // ---- source == "timeline" (genalpha.student_timeline) ----
        return when (type) {
            // A third copy of the reminder, and one that duplicates itself. It
            // renders only for students who predate the flow instrumentation.
            "whatsapp_reminder" ->
                Classification("reminder", 90, "rem:$day", "reminder", keep = false, isProtected = false)
            "whatsapp_reminder_failed" ->
                Classification("failure", 40, "fail:reminder:$day", "", keep = true, isProtected = true)
            // Every one of these is an echo of a payment or a message already
            // rendered from typed data. Safe to drop precisely BECAUSE the
            // payment row is built from student_payments and cannot go missing
            // when an echo does.
            "renewal_paid", "joining_fee_paid", "jersey_payment", "payment", "payment_updated",
            "renewal_updated", "renewal_whatsapp_confirmation", "whatsapp_reminder_retry_scheduled",
            "payment_pending_verification" -> DROP
            "payment_deleted", "student_discontinued", "discontinued", "student_rejoined",
            "whatsapp_contact_blocked", "note", "data_correction", "status_correction",
            "admission_payment_corrected" ->
                Classification("life", 10, "life:${row.id}", "", keep = true, isProtected = true)
            "admission", "admission_review", "student_created", "created", "creation" ->
                Classification("life", 10, "life:${row.id}", "", keep = true, isProtected = false)
            "fees_updated", "fee_status_updated", "jersey_updated" ->
                Classification("fee_change", 20, "fee:${row.id}", "", keep = true, isProtected = false)
            "profile_updated" -> {
                val tokens = profileChangeTokens(row.details)
                // A changed phone number is why reminders stop arriving, so it
                // is a record row, not a profile edit. A changed fee is money.
                when {
                    has(tokens, CONTACT_TOKENS) ->
                        Classification("life", 10, "life:${row.id}", "", keep = true, isProtected = true)
                    has(tokens, MONEY_TOKENS) ->
                        Classification("fee_change", 20, "fee:${row.id}", "", keep = true, isProtected = false)
                    else ->
                        Classification("profile", 60, "prof:${row.id}", "profile", keep = true, isProtected = false)
                }
            }
            else -> Classification("life", 30, "life:${row.id}", "", keep = true, isProtected = false)
        }
    }

    /**
     * Pure and idempotent: fold(fold(x)) == fold(x), asserted over every
     * fixture. Callers pass already-normalized rows.
     */
    fun foldTimeline(rows: List<StudentTimelineItem>): List<StudentTimelineItem> {
        val classified = rows.map { it to classifyTimelineRow(it) }.filter { it.second.kind != "drop" }

        val byKey = LinkedHashMap<String, MutableList<Pair<StudentTimelineItem, Classification>>>()
        classified.forEach { entry ->
            val key = entry.second.factKey.ifBlank { "solo:${entry.first.id}" }
            byKey.getOrPut(key) { mutableListOf() }.add(entry)
        }

        val folded = mutableListOf<StudentTimelineItem>()
        byKey.values.forEach { entries ->
            val sorted = entries.sortedBy { it.second.rank }
            // Nothing eligible to render claimed this key, so the strongest
            // attacher is promoted rather than lost.
            val winner = sorted.firstOrNull { it.second.keep } ?: sorted.first()
            // A promoted `delivery` row means Meta reported on a reminder
            // whose own reminder_created row we never stored — 26 real cases.
            // It is still a reminder as far as the owner is concerned, and
            // calling it "delivery" would render a row with no sentence.
            val winnerKind = if (winner.second.kind == "delivery") "reminder" else winner.second.kind

            var delivery = "none"
            var linkIncluded = false
            var proofPath = winner.first.proofPath
            var proofBucket = winner.first.proofBucket
            sorted.forEach { (row, cls) ->
                if (cls.kind == "delivery" || cls.kind == "reminder") {
                    delivery = bestDelivery(delivery, deliveryState(row))
                }
                if (row.eventType == "payment_link_sent") linkIncluded = true
                if (proofPath.isBlank() && row.proofPath.isNotBlank()) {
                    proofPath = row.proofPath
                    proofBucket = row.proofBucket
                }
            }

            folded.add(
                winner.first.copy(
                    kind = winnerKind,
                    runGroup = if (winnerKind == "reminder") "reminder" else winner.second.runGroup,
                    isProtected = sorted.any { it.second.isProtected },
                    deliveryState = if (winnerKind == "reminder") delivery else deliveryState(winner.first),
                    linkIncluded = linkIncluded,
                    proofPath = proofPath,
                    proofBucket = proofBucket,
                    // Carried through rather than reset, so folding an
                    // already-folded list is a no-op. Without this a re-render
                    // silently un-counts every run.
                    runCount = winner.first.runCount,
                    runDates = winner.first.runDates.ifEmpty {
                        listOfNotNull(istDay(winner.first.createdAt).ifBlank { null })
                    },
                ),
            )
        }

        val sortedRows = folded.sortedWith(
            compareByDescending<StudentTimelineItem> { it.createdAt.orEmpty() }
                .thenBy { classifyTimelineRow(it).rank }
                .thenBy { it.id },
        )

        // Only `reminder` and `profile` have a runGroup, so a failure, a
        // payment or a life event standing between two chase rungs breaks the
        // run by construction. A run may never swallow one.
        val out = mutableListOf<StudentTimelineItem>()
        sortedRows.forEach { row ->
            val previous = out.lastOrNull()
            if (row.runGroup.isNotBlank() && previous != null && previous.runGroup == row.runGroup) {
                out[out.size - 1] = previous.copy(
                    runCount = previous.runCount + row.runCount,
                    runDates = (previous.runDates + row.runDates).distinct(),
                    deliveryState = bestDelivery(previous.deliveryState, row.deliveryState),
                    linkIncluded = previous.linkIncluded || row.linkIncluded,
                )
            } else {
                out.add(row)
            }
        }
        return out.map { it.copy(runDates = it.runDates.sorted()) }
    }
}
