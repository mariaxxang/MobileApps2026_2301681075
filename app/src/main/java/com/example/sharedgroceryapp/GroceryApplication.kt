package com.example.sharedgroceryapp

import android.app.Application
import com.example.sharedgroceryapp.data.local.GroceryDatabase
import com.example.sharedgroceryapp.data.repository.GroceryRepository

class GroceryApplication : Application() {
    val database by lazy { GroceryDatabase.getDatabase(this) }
    val repository by lazy { GroceryRepository(database.groceryDao()) }
}
