package com.example.sharedgroceryapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    // Shopping Lists CRUD
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<ShoppingList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(list: ShoppingList): Long

    @Update
    fun updateList(list: ShoppingList): Int

    @Delete
    fun deleteList(list: ShoppingList): Int

    // Grocery Items CRUD (Filtered by listId)
    @Query("SELECT * FROM grocery_items WHERE listId = :listId ORDER BY id DESC")
    fun getItemsForList(listId: Int): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: GroceryItem): Long

    @Update
    fun updateItem(item: GroceryItem): Int

    @Delete
    fun deleteItem(item: GroceryItem): Int
}
