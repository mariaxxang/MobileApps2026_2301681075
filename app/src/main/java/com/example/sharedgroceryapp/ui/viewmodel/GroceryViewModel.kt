package com.example.sharedgroceryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.example.sharedgroceryapp.data.repository.GroceryRepository
import com.example.sharedgroceryapp.data.local.ShoppingList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroceryViewModel(private val repository: GroceryRepository) : ViewModel() {

    // Shopping List methods
    val allLists: StateFlow<List<ShoppingList>> = repository.allLists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allGroceryItems: StateFlow<List<GroceryItem>> = repository.allGroceryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertList(list: ShoppingList) = viewModelScope.launch {
        repository.insertList(list)
    }

    fun updateList(list: ShoppingList) = viewModelScope.launch {
        repository.updateList(list)
    }

    fun deleteList(list: ShoppingList) = viewModelScope.launch {
        repository.deleteList(list)
    }

    // Grocery Item methods
    fun getItemsForList(listId: Int): Flow<List<GroceryItem>> {
        return repository.getItemsForList(listId)
    }

    fun insertItem(item: GroceryItem) = viewModelScope.launch {
        repository.insertItem(item)
    }

    fun updateItem(item: GroceryItem) = viewModelScope.launch {
        repository.updateItem(item)
    }

    fun deleteItem(item: GroceryItem) = viewModelScope.launch {
        repository.deleteItem(item)
    }
}

class GroceryViewModelFactory(private val repository: GroceryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroceryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroceryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
