package com.genalpha.cricketacademy.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Drives the Kotlin fold from the same fixture file the web Node test uses,
 * so the two implementations cannot drift apart silently. The per-case
 * expectations pin the shape; the invariants below are what actually protect
 * the owner, because they hold over every case including ones nobody thought
 * to write.
 */
class TimelineRulesTest {

    private fun fixtures(): JSONObject {
        val stream = javaClass.classLoader!!.getResourceAsStream("timeline-fixtures.json")
            ?: error("timeline-fixtures.json missing from test resources")
        return JSONObject(stream.bufferedReader().use { it.readText() })
    }

    /** The fixture's `occurredAt` is this model's `createdAt`. */
    private fun row(json: JSONObject) = StudentTimelineItem(
        id = json.getString("id"),
        studentId = "s1",
        eventType = json.optString("eventType"),
        eventDate = json.optString("occurredAt").take(10),
        title = json.optString("title"),
        details = json.optString("details"),
        createdAt = json.optString("occurredAt"),
        source = json.optString("source", "timeline"),
        status = json.optString("status"),
        messageKind = json.optString("messageKind"),
        sentAt = json.optString("sentAt"),
        acceptedAt = json.optString("acceptedAt"),
        deliveredAt = json.optString("deliveredAt"),
        readAt = json.optString("readAt"),
        failedAt = json.optString("failedAt"),
        errorMessage = json.optString("errorMessage"),
        amount = json.optDouble("amount", 0.0),
        proofBucket = json.optString("proofBucket"),
        proofPath = json.optString("proofPath"),
    )

    private fun casesWithRows(): List<Pair<JSONObject, List<StudentTimelineItem>>> {
        val cases = fixtures().getJSONArray("cases")
        return (0 until cases.length()).map { index ->
            val case = cases.getJSONObject(index)
            val rows = case.getJSONArray("rows")
            case to (0 until rows.length()).map { row(rows.getJSONObject(it)) }
        }
    }

    @Test
    fun `the fold matches every shared fixture`() {
        for ((case, rows) in casesWithRows()) {
            val name = case.getString("name")
            val expected = case.getJSONArray("expect")
            val folded = TimelineRules.foldTimeline(rows)
            assertEquals(
                "$name — ${case.getString("why")}\ngot: ${folded.map { it.kind }}",
                expected.length(),
                folded.size,
            )
            for (index in 0 until expected.length()) {
                val want = expected.getJSONObject(index)
                val got = folded[index]
                assertEquals("$name row $index kind", want.getString("kind"), got.kind)
                assertEquals("$name row $index runCount", want.getInt("runCount"), got.runCount)
                if (want.has("deliveryState")) {
                    assertEquals("$name row $index deliveryState", want.getString("deliveryState"), got.deliveryState)
                }
                if (want.has("linkIncluded")) {
                    assertEquals("$name row $index linkIncluded", want.getBoolean("linkIncluded"), got.linkIncluded)
                }
                if (want.has("proofPath")) {
                    assertEquals("$name row $index proofPath", want.getString("proofPath"), got.proofPath)
                }
                if (want.has("runDates")) {
                    val dates = want.getJSONArray("runDates")
                    assertEquals(
                        "$name row $index runDates",
                        (0 until dates.length()).map { dates.getString(it) },
                        got.runDates,
                    )
                }
            }
        }
    }

    @Test
    fun `protected rows always survive the fold`() {
        for ((case, rows) in casesWithRows()) {
            val folded = TimelineRules.foldTimeline(rows)
            for (input in rows) {
                val cls = TimelineRules.classifyTimelineRow(input)
                if (!cls.isProtected) continue
                assertTrue(
                    "${case.getString("name")}: protected ${cls.kind} row ${input.id} vanished. " +
                        "Money, delivery failures, wrong-number facts, roster moves and proof " +
                        "must always reach the output.",
                    folded.any { it.id == input.id || (it.isProtected && it.kind == cls.kind) },
                )
            }
        }
    }

    @Test
    fun `folding a folded list changes nothing`() {
        for ((case, rows) in casesWithRows()) {
            val once = TimelineRules.foldTimeline(rows)
            val twice = TimelineRules.foldTimeline(once)
            assertEquals(
                "${case.getString("name")}: fold is not idempotent, so re-rendering would keep changing the list",
                once.map { it.kind to it.runCount },
                twice.map { it.kind to it.runCount },
            )
        }
    }

    @Test
    fun `a payment never removes a delivery row`() {
        for ((case, rows) in casesWithRows()) {
            val withoutPayments = rows.filter { it.source != "payment" }
            if (withoutPayments.size == rows.size) continue
            val before = TimelineRules.foldTimeline(withoutPayments).count { it.kind == "failure" || it.kind == "reminder" }
            val after = TimelineRules.foldTimeline(rows).count { it.kind == "failure" || it.kind == "reminder" }
            assertEquals(
                "${case.getString("name")}: a payment changed the delivery side of the timeline",
                before,
                after,
            )
        }
    }

    @Test
    fun `a non-failed delivery transition is never a row`() {
        // The single largest rule: 911 reminder_message_status rows, 41% of
        // the whole corpus, of which only the failures are worth a line.
        for (status in listOf("sent", "delivered", "read", "accepted")) {
            val cls = TimelineRules.classifyTimelineRow(
                StudentTimelineItem(
                    id = "x", studentId = "s", eventType = "reminder_message_status",
                    eventDate = "2026-08-16", title = "", createdAt = "2026-08-16T09:30:00Z",
                    source = "flow", status = status,
                ),
            )
            assertEquals("$status must not render as its own row", false, cls.keep)
        }
    }

    @Test
    fun `delivery is read from timestamps, never from the status column`() {
        // The webhook patches status unconditionally, so a late `delivered`
        // callback can land on top of a `failed` one.
        val row = StudentTimelineItem(
            id = "x", studentId = "s", eventType = "reminder_message_status", eventDate = "2026-08-16",
            title = "", createdAt = "2026-08-16T09:36:00Z", source = "flow", status = "delivered",
            failedAt = "2026-08-16T09:31:00Z", deliveredAt = "2026-08-16T09:36:00Z",
        )
        assertEquals("failed", TimelineRules.deliveryState(row))
    }

    @Test
    fun `istDay is the Asia Kolkata day, not a UTC string slice`() {
        assertEquals("2026-08-17", TimelineRules.istDay("2026-08-16T19:30:00Z"))
        assertEquals("2026-08-16", TimelineRules.istDay("2026-08-16T18:29:00Z"))
    }
}
