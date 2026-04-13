package com.genalpha.cricketacademy.ui

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

fun buildUpiPayUri(
    upiId: String,
    payeeName: String,
    amount: Double?,
    note: String,
): String {
    val safeId = upiId.trim()
    val safeName = payeeName.trim()
    val safeNote = note.trim()
    val amountParam = amount?.takeIf { it > 0 }?.let { String.format(Locale.US, "%.2f", it) }

    fun enc(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

    val query = buildList {
        add("pa=${enc(safeId)}")
        if (safeName.isNotBlank()) add("pn=${enc(safeName)}")
        if (!amountParam.isNullOrBlank()) add("am=${enc(amountParam)}")
        add("cu=INR")
        if (safeNote.isNotBlank()) add("tn=${enc(safeNote)}")
    }.joinToString("&")

    return "upi://pay?$query"
}

fun generateQrBitmap(
    text: String,
    sizePx: Int,
): Bitmap {
    val writer = QRCodeWriter()
    val matrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)

    for (y in 0 until sizePx) {
        for (x in 0 until sizePx) {
            bitmap.setPixel(
                x,
                y,
                if (matrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            )
        }
    }

    return bitmap
}

