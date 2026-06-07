package com.example.sharedgroceryapp.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListTest {

    @Test
    fun testDefaultInstantiation() {
        val before = System.currentTimeMillis()
        val list = ShoppingList(title = "Weekly Groceries")
        val after = System.currentTimeMillis()

        assertEquals(0, list.id)
        assertEquals("Weekly Groceries", list.title)
        assertTrue(list.createdAt in before..after)
    }

    @Test
    fun testCustomInstantiation() {
        val customTime = 123456789L
        val list = ShoppingList(id = 5, title = "Custom List", createdAt = customTime)

        assertEquals(5, list.id)
        assertEquals("Custom List", list.title)
        assertEquals(customTime, list.createdAt)
    }

    @Test
    fun testCopyMethod() {
        val list = ShoppingList(id = 1, title = "Original Title", createdAt = 1000L)
        val copied = list.copy(title = "Updated Title")

        assertEquals(1, copied.id)
        assertEquals("Updated Title", copied.title)
        assertEquals(1000L, copied.createdAt)
    }
}
