package com.example.sharedgroceryapp.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryItemTest {

    @Test
    fun testDefaultInstantiation() {
        val item = GroceryItem(listId = 2, name = "Bananas", quantity = 5)

        assertEquals(0, item.id)
        assertEquals(2, item.listId)
        assertEquals("Bananas", item.name)
        assertEquals(5, item.quantity)
        assertFalse(item.isBought)
    }

    @Test
    fun testCustomInstantiation() {
        val item = GroceryItem(id = 10, listId = 3, name = "Milk", quantity = 2, isBought = true)

        assertEquals(10, item.id)
        assertEquals(3, item.listId)
        assertEquals("Milk", item.name)
        assertEquals(2, item.quantity)
        assertTrue(item.isBought)
    }

    @Test
    fun testCopyMethod() {
        val item = GroceryItem(id = 1, listId = 2, name = "Cheese", quantity = 1, isBought = false)
        val updatedItem = item.copy(quantity = 3, isBought = true)

        assertEquals(1, updatedItem.id)
        assertEquals(2, updatedItem.listId)
        assertEquals("Cheese", updatedItem.name)
        assertEquals(3, updatedItem.quantity)
        assertTrue(updatedItem.isBought)
    }
}
