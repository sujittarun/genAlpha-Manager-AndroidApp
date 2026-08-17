package com.genalpha.cricketacademy.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The web app and this one must read a proof key the same way, because a
 * screenshot that opens on one and not the other is the kind of gap that
 * only shows up when a parent is waiting for their payment to be
 * confirmed.
 *
 * The cases below are the same strings the JS helpers in script.js are
 * checked against. Two of them are the ones that actually bit:
 *
 *   - a key with a tenant prefix (three segments). The original pattern
 *     required exactly two, so it silently stopped matching the day
 *     2026-08-17a made new uploads tenant-scoped.
 *   - a details line that ends in the key, where dropping it must also
 *     drop the trailing separator rather than leaving "… From: 2026-08-13 •".
 */
class PaymentProofPathTest {

    @Test
    fun `reads a two-segment key from a stored sentence`() {
        val details = "Parent replied with image. Proof stored at payment-proofs/2811/wamidHBgM.jpg."
        assertEquals("2811/wamidHBgM.jpg", paymentProofPath(details))
        assertEquals("Parent replied with image.", stripPaymentProofPath(details))
    }

    @Test
    fun `reads a tenant-prefixed key`() {
        val details = "Parent replied with image. Proof stored at payment-proofs/genalpha/2811/wamidHBgM.jpg."
        assertEquals("genalpha/2811/wamidHBgM.jpg", paymentProofPath(details))
        assertEquals("Parent replied with image.", stripPaymentProofPath(details))
    }

    @Test
    fun `drops the trailing separator when the key ended the line`() {
        val details = "Plan: 1 Month • Amount: Rs 3,500 • From: 2026-08-13 • payment-proofs/2811/wamidHBgM.jpg"
        assertEquals("2811/wamidHBgM.jpg", paymentProofPath(details))
        assertEquals("Plan: 1 Month • Amount: Rs 3,500 • From: 2026-08-13", stripPaymentProofPath(details))
    }

    @Test
    fun `leaves a sentence with no key alone`() {
        val details = "Parent replied with image."
        assertEquals("", paymentProofPath(details))
        assertEquals("Parent replied with image.", stripPaymentProofPath(details))
    }

    @Test
    fun `a bare filename is not a key`() {
        // No folder segment means nothing to scope to a tenant, so it must
        // not be treated as an object key.
        assertEquals("", paymentProofPath("see payment-proofs/screenshot.jpg"))
    }
}
