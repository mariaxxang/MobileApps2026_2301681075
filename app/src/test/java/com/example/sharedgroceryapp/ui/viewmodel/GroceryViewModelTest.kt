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
import kotlinx.coroutines.launch
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

    @Test
    fun statisticsState_computesCorrectStatistics() = kotlinx.coroutines.test.runTest(testDispatcher) {
        val lists = listOf(
            ShoppingList(id = 1, title = "List 1", createdAt = System.currentTimeMillis()),
            ShoppingList(id = 2, title = "List 2", createdAt = System.currentTimeMillis())
        )
        val items = listOf(
            GroceryItem(id = 1, listId = 1, name = "Apples", quantity = 5, isBought = true, category = "FRUITS"),
            GroceryItem(id = 2, listId = 1, name = "Milk", quantity = 1, isBought = false, category = "DAIRY"),
            GroceryItem(id = 3, listId = 2, name = "Bread", quantity = 2, isBought = false, category = "BAKERY"),
            GroceryItem(id = 4, listId = 2, name = "Beef", quantity = 1, isBought = true, category = "MEAT")
        )

        every { repository.allLists } returns flowOf(lists)
        every { repository.allGroceryItems } returns flowOf(items)

        // Create viewModel again to observe the mocked flows
        val testViewModel = GroceryViewModel(repository)
        
        val states = mutableListOf<StatisticsState>()
        val collectJob = launch {
            testViewModel.statisticsState.collect { states.add(it) }
        }
        
        val stats = states.last()

        org.junit.Assert.assertEquals(2, stats.totalListsCount)
        org.junit.Assert.assertEquals(4, stats.totalItemsCount)
        org.junit.Assert.assertEquals(2, stats.activeItemsCount)
        org.junit.Assert.assertEquals(2, stats.completedItemsCount)
        org.junit.Assert.assertEquals(1, stats.categoryCounts["FRUITS"])
        org.junit.Assert.assertEquals(1, stats.categoryCounts["DAIRY"])
        org.junit.Assert.assertEquals(1, stats.categoryCounts["BAKERY"])
        org.junit.Assert.assertEquals(1, stats.categoryCounts["MEAT"])
        org.junit.Assert.assertTrue(stats.listsPerMonth.isNotEmpty())
        
        collectJob.cancel()
    }
}
