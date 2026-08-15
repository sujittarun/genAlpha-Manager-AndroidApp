package com.genalpha.cricketacademy.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.genalpha.cricketacademy.data.AcademyExpense
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Runs the real composable on the JVM under Robolectric — no emulator, no device.
 *
 * Deleting an expense is immediate and cannot be undone, and it used to happen on a
 * single tap. This pins the confirmation so nobody can quietly remove it again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExpenseDeleteConfirmationTest {

    @get:Rule
    val compose = createComposeRule()

    private val expense = AcademyExpense(
        id = "1",
        expenseType = "Transport",
        amount = 1234.0,
        expenseDate = "2026-08-15",
        paidBy = "Sujit",
        comment = "Ground travel",
    )

    private fun renderCard(onDelete: () -> Unit) {
        compose.setContent {
            FinanceExpenseCard(
                expense = expense,
                isDeleting = false,
                formatCurrency = { value -> "Rs ${value.toInt()}" },
                onDelete = onDelete,
            )
        }
    }

    /** The card's own button, before any dialog exists. */
    private fun tapDeleteOnCard() = compose.onNodeWithText("Delete").performClick()

    /** Once the dialog is open both it and the card say "Delete"; the dialog's is last. */
    private fun tapDeleteInDialog() {
        val nodes = compose.onAllNodesWithText("Delete")
        nodes[nodes.fetchSemanticsNodes().size - 1].performClick()
    }

    @Test
    fun `tapping delete asks first and deletes nothing yet`() {
        var deletes = 0
        renderCard { deletes += 1 }

        tapDeleteOnCard()

        compose.onNodeWithText("Delete expense?").assertIsDisplayed()
        assertEquals("nothing may be deleted before the manager confirms", 0, deletes)
    }

    @Test
    fun `the prompt names the expense being removed`() {
        renderCard { }
        tapDeleteOnCard()
        // Naming the row is what lets a misclick be caught before it is gone.
        compose.onNodeWithText("Delete Transport Rs 1234 on 15 Aug 2026? This cannot be undone.")
            .assertIsDisplayed()
    }

    @Test
    fun `cancelling leaves the expense alone`() {
        var deletes = 0
        renderCard { deletes += 1 }

        tapDeleteOnCard()
        compose.onNodeWithText("Cancel").performClick()

        assertEquals("cancel must not delete", 0, deletes)
        compose.onAllNodesWithText("Delete expense?").fetchSemanticsNodes().let {
            assertEquals("the dialog must close on cancel", 0, it.size)
        }
    }

    @Test
    fun `confirming deletes exactly once`() {
        var deletes = 0
        renderCard { deletes += 1 }

        tapDeleteOnCard()
        tapDeleteInDialog()

        assertEquals("confirming must delete once", 1, deletes)
    }
}
