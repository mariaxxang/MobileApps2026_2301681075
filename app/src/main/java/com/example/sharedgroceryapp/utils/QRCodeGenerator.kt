package com.example.sharedgroceryapp.utils

import android.graphics.Bitmap
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

object QRCodeGenerator {

    fun formatGroceryList(items: List<GroceryItem>): String {
        if (items.isEmpty()) {
            return "Your shopping list is empty!"
        }
        return items.joinToString(separator = "\n") { item ->
            val status = if (item.isBought) "[x]" else "[ ]"
            "$status ${item.name} (Qty: ${item.quantity})"
        }
    }

    fun generateQRCode(content: String, width: Int = 512, height: Int = 512): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
            val encoder = BarcodeEncoder()
            encoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
