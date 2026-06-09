package com.example.sharedgroceryapp.data.repository

import com.example.sharedgroceryapp.data.local.GroceryDao
import com.example.sharedgroceryapp.data.local.GroceryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

import com.example.sharedgroceryapp.data.local.ShoppingList

class GroceryRepository(private val groceryDao: GroceryDao) {

    // Shopping List methods
    val allLists: Flow<List<ShoppingList>> = groceryDao.getAllLists()

    suspend fun insertList(list: ShoppingList): Long = withContext(Dispatchers.IO) {
        groceryDao.insertList(list)
    }

    suspend fun updateList(list: ShoppingList) = withContext(Dispatchers.IO) {
        groceryDao.updateList(list)
    }

    suspend fun deleteList(list: ShoppingList) = withContext(Dispatchers.IO) {
        groceryDao.deleteList(list)
    }

    val allGroceryItems: Flow<List<GroceryItem>> = groceryDao.getAllItems()

    fun searchItems(query: String): Flow<List<GroceryItem>> {
        return groceryDao.searchItems(query)
    }

    // Grocery Item methods
    fun getItemsForList(listId: Int): Flow<List<GroceryItem>> {
        return groceryDao.getItemsForList(listId)
    }

    suspend fun insertItem(item: GroceryItem): Long = withContext(Dispatchers.IO) {
        groceryDao.insertItem(item)
    }

    suspend fun updateItem(item: GroceryItem) = withContext(Dispatchers.IO) {
        groceryDao.updateItem(item)
    }

    suspend fun deleteItem(item: GroceryItem) = withContext(Dispatchers.IO) {
        groceryDao.deleteItem(item)
    }

    suspend fun deleteCompletedItemsForList(listId: Int) = withContext(Dispatchers.IO) {
        groceryDao.deleteCompletedItemsForList(listId)
    }
}
