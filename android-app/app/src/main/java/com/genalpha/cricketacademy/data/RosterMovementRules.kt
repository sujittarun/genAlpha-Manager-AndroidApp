package com.genalpha.cricketacademy.data

/**
 * Kotlin mirror of web-app-repo/roster-movement-rules.js.
 *
 * A player who left and came back inside the same reporting window never actually left it,
 * so they must not inflate that month's churn.
 */
object RosterMovementRules {

    private fun iso10(value: String?): String = value.orEmpty().take(10)

    fun isLeftDuringRange(
        discontinuedAt: String?,
        rejoinedAt: String?,
        rangeStart: String?,
        rangeEnd: String?,
    ): Boolean {
        val leftDate = iso10(discontinuedAt)
        val startDate = iso10(rangeStart)
        val endDate = iso10(rangeEnd)
        if (leftDate.isBlank() || startDate.isBlank() || endDate.isBlank()) return false
        if (leftDate < startDate || leftDate > endDate) return false

        val returnDate = iso10(rejoinedAt)
        // A return only cancels this exit if it happened after it. A player who came back once
        // and later left again still left — their older rejoin date must not erase the new exit.
        if (returnDate.isBlank() || returnDate < leftDate) return true
        return returnDate > endDate
    }
}
