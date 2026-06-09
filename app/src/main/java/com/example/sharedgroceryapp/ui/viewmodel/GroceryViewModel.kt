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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class StatisticsState(
    val totalListsCount: Int = 0,
    val totalItemsCount: Int = 0,
    val activeItemsCount: Int = 0,
    val completedItemsCount: Int = 0,
    val categoryCounts: Map<String, Int> = emptyMap(),
    val listsPerMonth: List<Pair<String, Int>> = emptyList()
)

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

    // Category filtering state and functions
    private val _selectedCategories = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    fun setSelectedCategories(categories: Set<String>) {
        _selectedCategories.value = categories
    }

    fun getFilteredItemsForList(listId: Int): Flow<List<GroceryItem>> {
        return repository.getItemsForList(listId).combine(selectedCategories) { items, selected ->
            if (selected.isEmpty()) {
                items
            } else {
                items.filter { selected.contains(it.category) }
            }
        }
    }

    fun searchItems(query: String): Flow<List<GroceryItem>> {
        return repository.searchItems(query)
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

    fun duplicateList(newTitle: String, originalItems: List<GroceryItem>) = viewModelScope.launch {
        val newListId = repository.insertList(ShoppingList(title = newTitle))
        originalItems.forEach { item ->
            repository.insertItem(item.copy(id = 0, listId = newListId.toInt(), isBought = false))
        }
    }

    fun deleteCompletedItems(listId: Int) = viewModelScope.launch {
        repository.deleteCompletedItemsForList(listId)
    }

    val statisticsState: StateFlow<StatisticsState> = combine(allLists, allGroceryItems) { lists, items ->
        val active = items.count { !it.isBought }
        val completed = items.count { it.isBought }
        val categories = items.groupBy { it.category }.mapValues { it.value.size }
        
        StatisticsState(
            totalListsCount = lists.size,
            totalItemsCount = items.size,
            activeItemsCount = active,
            completedItemsCount = completed,
            categoryCounts = categories,
            listsPerMonth = getListsPerMonth(lists)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsState()
    )

    private fun getListsPerMonth(lists: List<ShoppingList>): List<Pair<String, Int>> {
        val result = mutableListOf<Pair<String, Int>>()
        val monthFormats = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        
        val currentMonthList = mutableListOf<java.util.Calendar>()
        for (i in 5 downTo 0) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.MONTH, -i)
            currentMonthList.add(cal)
        }
        
        for (cal in currentMonthList) {
            val year = cal.get(java.util.Calendar.YEAR)
            val month = cal.get(java.util.Calendar.MONTH)
            
            val count = lists.count { list ->
                val listCal = java.util.Calendar.getInstance().apply { timeInMillis = list.createdAt }
                listCal.get(java.util.Calendar.YEAR) == year && listCal.get(java.util.Calendar.MONTH) == month
            }
            
            val label = "${monthFormats[month]} ${year % 100}"
            result.add(label to count)
        }
        
        return result
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
