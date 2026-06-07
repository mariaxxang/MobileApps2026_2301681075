package com.example.sharedgroceryapp.ui.viewmodel

import com.example.sharedgroceryapp.data.local.ShoppingList
import com.example.sharedgroceryapp.data.local.GroceryItem
import com.example.sharedgroceryapp.data.repository.GroceryRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroceryViewModelTest {

    private val repository: GroceryRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: GroceryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup mock default values for Flow-based properties
        every { repository.allLists } returns flowOf(emptyList())
        
        viewModel = GroceryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun insertList_callsRepository() {
        val list = ShoppingList(title = "Monday Shopping")
        viewModel.insertList(list)
        coVerify(exactly = 1) { repository.insertList(list) }
    }

    @Test
    fun updateList_callsRepository() {
        val list = ShoppingList(id = 1, title = "Tuesday Shopping")
        viewModel.updateList(list)
        coVerify(exactly = 1) { repository.updateList(list) }
    }

    @Test
    fun deleteList_callsRepository() {
        val list = ShoppingList(id = 1, title = "Tuesday Shopping")
        viewModel.deleteList(list)
        coVerify(exactly = 1) { repository.deleteList(list) }
    }

    @Test
    fun insertItem_callsRepository() {
        val item = GroceryItem(listId = 1, name = "Eggs", quantity = 12)
        viewModel.insertItem(item)
        coVerify(exactly = 1) { repository.insertItem(item) }
    }

    @Test
    fun updateItem_callsRepository() {
        val item = GroceryItem(id = 5, listId = 1, name = "Eggs", quantity = 12, isBought = true)
        viewModel.updateItem(item)
        coVerify(exactly = 1) { repository.updateItem(item) }
    }

    @Test
    fun deleteItem_callsRepository() {
        val item = GroceryItem(id = 5, listId = 1, name = "Eggs", quantity = 12)
        viewModel.deleteItem(item)
        coVerify(exactly = 1) { repository.deleteItem(item) }
    }
}
