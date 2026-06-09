package com.example.sharedgroceryapp.data.local

import com.example.sharedgroceryapp.R

enum class Category(val displayNameResId: Int, val iconResId: Int, val colorHex: String) {
    FRUITS(R.string.category_display_fruits, R.drawable.ic_nutrition, "#4CAF50"),
    DAIRY(R.string.category_display_dairy, R.drawable.ic_egg, "#2196F3"),
    MEAT(R.string.category_display_meat, R.drawable.ic_kebab, "#F44336"),
    DRINKS(R.string.category_display_drinks, R.drawable.ic_local_drink, "#9C27B0"),
    BAKERY(R.string.category_display_bakery, R.drawable.ic_bakery, "#FF9800"),
    OTHER(R.string.category_display_other, R.drawable.ic_shopping_bag, "#607D8B");

    companion object {
        fun fromString(value: String): Category {
            return values().firstOrNull { 
                it.name.equals(value, ignoreCase = true) ||
                (it == FRUITS && (value.equals("Fruits & Veggies", ignoreCase = true) || value.equals("Fruits", ignoreCase = true))) ||
                (it == DAIRY && value.equals("Dairy", ignoreCase = true)) ||
                (it == MEAT && (value.equals("Meat & Fish", ignoreCase = true) || value.equals("Meat", ignoreCase = true))) ||
                (it == DRINKS && value.equals("Drinks", ignoreCase = true)) ||
                (it == BAKERY && value.equals("Bakery", ignoreCase = true)) ||
                (it == OTHER && value.equals("Other", ignoreCase = true))
            } ?: OTHER
        }
    }
}
