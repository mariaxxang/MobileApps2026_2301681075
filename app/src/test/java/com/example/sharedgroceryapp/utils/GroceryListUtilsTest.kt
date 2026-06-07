package com.example.sharedgroceryapp.utils

import com.example.sharedgroceryapp.data.local.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Test

class GroceryListUtilsTest {

    @Test
    fun sortItems_emptyList_returnsEmptyList() {
        val items = emptyList<GroceryItem>()
        val sorted = GroceryListUtils.sortItems(items)
        assertEquals(0, sorted.size)
    }

    @Test
    fun sortItems_mixedItems_movesBoughtToBottomAndOrdersByIdDesc() {
        val item1 = GroceryItem(id = 1, listId = 1, name = "Apple", quantity = 3, isBought = true)
        val item2 = GroceryItem(id = 2, listId = 1, name = "Banana", quantity = 5, isBought = false)
        val item3 = GroceryItem(id = 3, listId = 1, name = "Milk", quantity = 1, isBought = true)
        val item4 = GroceryItem(id = 4, listId = 1, name = "Bread", quantity = 2, isBought = false)

        val items = listOf(item1, item2, item3, item4)
        val sorted = GroceryListUtils.sortItems(items)

        // Expected order:
        // Item 4 (not bought, id 4)
        // Item 2 (not bought, id 2)
        // Item 3 (bought, id 3)
        // Item 1 (bought, id 1)
        assertEquals(4, sorted.size)
        assertEquals(item4, sorted[0])
        assertEquals(item2, sorted[1])
        assertEquals(item3, sorted[2])
        assertEquals(item1, sorted[3])
    }

    @Test
    fun calculateTotalQuantity_mixedQuantities_returnsSum() {
        val items = listOf(
            GroceryItem(id = 1, listId = 1, name = "Apple", quantity = 3),
            GroceryItem(id = 2, listId = 1, name = "Banana", quantity = 5),
            GroceryItem(id = 3, listId = 1, name = "Milk", quantity = 10)
        )
        val total = GroceryListUtils.calculateTotalQuantity(items)
        assertEquals(18, total)
    }

    @Test
    fun calculateBoughtCount_mixedStatus_returnsCorrectCount() {
        val items = listOf(
            GroceryItem(id = 1, listId = 1, name = "Apple", quantity = 3, isBought = true),
            GroceryItem(id = 2, listId = 1, name = "Banana", quantity = 5, isBought = false),
            GroceryItem(id = 3, listId = 1, name = "Milk", quantity = 10, isBought = true)
        )
        val boughtCount = GroceryListUtils.calculateBoughtCount(items)
        assertEquals(2, boughtCount)
    }
}
