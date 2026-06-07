package com.example.sharedgroceryapp.utils

import com.example.sharedgroceryapp.data.local.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Test

class QRCodeGeneratorTest {

    @Test
    fun formatGroceryList_emptyList_returnsEmptyMessage() {
        val formatted = QRCodeGenerator.formatGroceryList(emptyList())
        assertEquals("Your shopping list is empty!", formatted)
    }

    @Test
    fun formatGroceryList_withItems_returnsFormattedString() {
        val items = listOf(
            GroceryItem(id = 1, listId = 1, name = "Apple", quantity = 3, isBought = false),
            GroceryItem(id = 2, listId = 1, name = "Banana", quantity = 5, isBought = true)
        )
        val formatted = QRCodeGenerator.formatGroceryList(items)
        val expected = "[ ] Apple (Qty: 3)\n[x] Banana (Qty: 5)"
        assertEquals(expected, formatted)
    }
}
