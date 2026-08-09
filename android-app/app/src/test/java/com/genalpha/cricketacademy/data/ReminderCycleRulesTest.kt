package com.genalpha.cricketacademy.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Drives the Kotlin rules from the same fixture file the web Node test uses, so the two
 * implementations cannot drift apart silently.
 */
class ReminderCycleRulesTest {

    private fun fixtures(): JSONObject {
        val stream = javaClass.classLoader!!.getResourceAsStream("reminder-cycle-fixtures.json")
            ?: error("reminder-cycle-fixtures.json missing from test resources")
        return JSONObject(stream.bufferedReader().use { it.readText() })
    }

    @Test
    fun `currentFeeCycleDate matches every shared fixture`() {
        val cases = fixtures().getJSONArray("currentFeeCycleDate")
        for (index in 0 until cases.length()) {
            val case = cases.getJSONObject(index)
            val input = case.getJSONObject("input")
            assertEquals(
                case.getString("name"),
                case.getString("expected"),
                ReminderCycleRules.currentFeeCycleDate(
                    feesPending = input.getBoolean("feesPending"),
                    joinDate = input.optString("joinDate"),
                    rejoinedAt = input.optString("rejoinedAt"),
                    paidThroughDate = input.optString("paidThroughDate"),
                ),
            )
        }
    }

    @Test
    fun `isFollowUpForCurrentCycle matches every shared fixture`() {
        val cases = fixtures().getJSONArray("isFollowUpForCurrentCycle")
        for (index in 0 until cases.length()) {
            val case = cases.getJSONObject(index)
            val input = case.getJSONObject("input")
            assertEquals(
                case.getString("name"),
                case.getBoolean("expected"),
                ReminderCycleRules.isFollowUpForCurrentCycle(
                    cycleDueDate = input.optString("cycleDueDate"),
                    followUpDueDate = input.optString("followUpDueDate"),
                    followUpCreatedAt = input.optString("followUpCreatedAt"),
                    rejoinedAt = input.optString("rejoinedAt"),
                ),
            )
        }
    }

    @Test
    fun `a rejoined player is not judged by the cycle they left behind`() {
        // Yuvaan Bejugam: renewed through 2026-07-06, discontinued 2026-07-13, back 2026-08-03.
        val cycle = ReminderCycleRules.currentFeeCycleDate(
            feesPending = false,
            joinDate = "2026-04-06",
            rejoinedAt = "2026-08-03",
            paidThroughDate = "2026-08-03",
        )
        assertEquals("2026-08-03", cycle)
        assertEquals(
            false,
            ReminderCycleRules.isFollowUpForCurrentCycle(
                cycleDueDate = cycle,
                followUpDueDate = "2026-07-06",
                followUpCreatedAt = "2026-07-11",
                rejoinedAt = "2026-08-03",
            ),
        )
    }
}
