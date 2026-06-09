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

    @Query("SELECT * FROM grocery_items WHERE listId = :listId ORDER BY id DESC")
    fun getItemsForList(listId: Int): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items")
    fun getAllItems(): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE name LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: GroceryItem): Long

    @Update
    fun updateItem(item: GroceryItem): Int

    @Delete
    fun deleteItem(item: GroceryItem): Int

    @Query("DELETE FROM grocery_items WHERE listId = :listId AND isBought = 1")
    fun deleteCompletedItemsForList(listId: Int): Int
}
