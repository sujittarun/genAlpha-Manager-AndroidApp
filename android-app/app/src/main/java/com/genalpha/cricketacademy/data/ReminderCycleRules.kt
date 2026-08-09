package com.genalpha.cricketacademy.data

/**
 * Kotlin mirror of web-app-repo/reminder-cycle-rules.js.
 *
 * Both apps must answer "does this reminder row still describe the player today?" the same
 * way. The shared cases live in app/src/test/resources/reminder-cycle-fixtures.json, which
 * is byte-identical to the web copy and drives ReminderCycleRulesTest — if the two
 * implementations drift, that test fails.
 */
object ReminderCycleRules {

    fun iso10(value: String?): String = value.orEmpty().take(10)

    fun maxIso(first: String?, second: String?): String {
        val a = iso10(first)
        val b = iso10(second)
        if (a.isBlank()) return b
        if (b.isBlank()) return a
        return if (b > a) b else a
    }

    /**
     * The date the player's current fee cycle became due.
     *
     * Joining fee: a rejoined player restarts from the return date, otherwise the original
     * join date. Renewals: the rejoin-aware paid-through date the caller already computed.
     * Every overdue count must be derived from this one value.
     */
    fun currentFeeCycleDate(
        feesPending: Boolean,
        joinDate: String?,
        rejoinedAt: String?,
        paidThroughDate: String?,
    ): String = if (feesPending) maxIso(joinDate, rejoinedAt) else iso10(paidThroughDate)

    /**
     * Whether a reminder_events row still describes the player's current fee cycle.
     *
     * Anything persisted on a reminder row (manual_followup_required, delivery status,
     * selected plan, amount) is a fact about ONE billing cycle and expires with it. Live
     * properties of the player (15+ days overdue, wrong_number, opted_out) are NOT
     * cycle-scoped and must be evaluated outside this gate.
     */
    fun isFollowUpForCurrentCycle(
        cycleDueDate: String?,
        followUpDueDate: String?,
        followUpCreatedAt: String?,
        rejoinedAt: String?,
    ): Boolean {
        val cycle = iso10(cycleDueDate)
        // Without a cycle date there is nothing to compare against; never suppress on a guess.
        if (cycle.isBlank()) return true

        val created = iso10(followUpCreatedAt)
        val rejoin = iso10(rejoinedAt)
        // A row raised before the player returned can never govern the post-rejoin cycle,
        // even if the dates happen to line up.
        if (rejoin.isNotBlank() && created.isNotBlank() && created < rejoin) return false

        val due = iso10(followUpDueDate)
        // A later due date means the row belongs to this cycle or a future one; only an
        // older due date proves the row was raised for a cycle that has since been replaced.
        if (due.isNotBlank()) return due >= cycle

        // Legacy rows written before due_date was recorded: fall back to when it was raised.
        return created.isBlank() || created >= cycle
    }
}
