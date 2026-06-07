package com.example.sharedgroceryapp.utils

import com.example.sharedgroceryapp.data.local.GroceryItem

object GroceryListUtils {

    /**
     * Sorts the grocery items so that items not yet bought (isBought = false) are at the top,
     * and bought items (isBought = true) are at the bottom.
     * Within each group, items are sorted by their ID in descending order.
     */
    fun sortItems(items: List<GroceryItem>): List<GroceryItem> {
        return items.sortedWith(
            compareBy<GroceryItem> { it.isBought }
                .thenByDescending { it.id }
        )
    }

    /**
     * Calculates the total quantity of all items in the list.
     */
    fun calculateTotalQuantity(items: List<GroceryItem>): Int {
        return items.sumOf { it.quantity }
    }

    /**
     * Calculates the count of items that are bought.
     */
    fun calculateBoughtCount(items: List<GroceryItem>): Int {
        return items.count { it.isBought }
    }
}
