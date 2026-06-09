package com.example.sharedgroceryapp.data.local

import com.example.sharedgroceryapp.R

enum class Category(val displayName: String, val iconResId: Int, val colorHex: String) {
    FRUITS("Fruits & Veggies", R.drawable.ic_nutrition, "#4CAF50"),
    DAIRY("Dairy", R.drawable.ic_egg, "#2196F3"),
    MEAT("Meat & Fish", R.drawable.ic_kebab, "#F44336"),
    DRINKS("Drinks", R.drawable.ic_local_drink, "#9C27B0"),
    BAKERY("Bakery", R.drawable.ic_bakery, "#FF9800"),
    OTHER("Other", R.drawable.ic_shopping_bag, "#607D8B");

    companion object {
        fun fromString(value: String): Category {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
