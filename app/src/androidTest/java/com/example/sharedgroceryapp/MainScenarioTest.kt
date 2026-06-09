package com.example.sharedgroceryapp

import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sharedgroceryapp.data.local.GroceryDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScenarioTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        GroceryDatabase.getDatabase(context).clearAllTables()
    }

    // Helper to slow down test execution so actions are clearly visible
    private fun sleep(ms: Long = 1500) {
        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Test
    fun mainScenarioTest() {
        // --- 1. HOME SCREEN STARTUP ---
        // Verify that the Home Screen is displayed
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
        onView(withId(R.id.fabAddList))
            .check(matches(isDisplayed()))
        sleep()

        // --- 2. LIST CREATION BOTTOM SHEET ---
        // Click the FAB to add a new list
        onView(withId(R.id.fabAddList))
            .perform(click())
        sleep()

        // Verify that the 'New Shopping List' bottom sheet dialog appears correctly
        onView(withId(R.id.tvTitle))
            .check(matches(withText("New Shopping List")))
        sleep()

        // Type the list name 'Weekend Party' and click 'Create'
        onView(withId(R.id.etListName))
            .perform(replaceText("Weekend Party"))
        sleep()
        onView(withId(R.id.btnCreate))
            .perform(click())
        sleep()

        // Verify list has been successfully added to the screen
        onView(withText("Weekend Party"))
            .check(matches(isDisplayed()))
        sleep()

        // --- 2.5 TOGGLE THEME (DARK MODE / LIGHT MODE) ---
        // Click the theme toggle button in the toolbar
        onView(withId(R.id.action_toggle_theme))
            .perform(click())
        sleep()

        // --- 3. LIST DETAIL HERO SCREEN ---
        // Click on the newly created list item in the RecyclerView to navigate to ListDetailFragment
        // We match by text "Weekend Party" so that it works regardless of other existing lists
        onView(withText("Weekend Party"))
            .perform(click())
        sleep()

        // Verify we are on the List Details screen
        onView(withId(R.id.tvDetailListName))
            .check(matches(withText("Weekend Party")))
        onView(withId(R.id.btnViewItems))
            .check(matches(isDisplayed()))
        sleep()

        // --- 4. WEEKLY ITEMS LIST SCREEN ---
        // Click 'View Items List' to navigate to GroceryListFragment
        onView(withId(R.id.btnViewItems))
            .perform(click())
        sleep()

        // --- 5. ADD ITEM WITH CATEGORY ---
        // Click the FAB in GroceryListFragment to add a new Grocery Item
        onView(withId(R.id.fabAddItem))
            .perform(click())
        sleep()

        // In AddEditItemFragment, type the item name 'Snacks' and click 'Save Item'
        onView(withId(R.id.etItemName))
            .perform(replaceText("Snacks"))
        sleep()
        onView(withId(R.id.btnSave))
            .perform(click())
        sleep()

        // Verify that 'Snacks' is displayed in the list
        onView(allOf(withId(R.id.tvItemName), withText("Snacks"), isDisplayed()))
            .check(matches(isDisplayed()))
        sleep()

        // --- 6. CHECKBOX COMPLETION UPDATE ---
        // Click the item's checkbox to mark it as bought
        onView(allOf(withId(R.id.checkboxBought), isDisplayed()))
            .perform(click())
        sleep()

        // --- 7. SHARE QR DIALOG ---
        // Click the 'Share QR Code' menu item in the Top AppBar
        onView(withId(R.id.action_share_qr))
            .perform(click())
        sleep()

        // Verify the QR Code Dialog is displayed
        onView(withText("Share Shopping List"))
            .check(matches(isDisplayed()))
        onView(withId(R.id.ivQRCode))
            .check(matches(isDisplayed()))
        sleep()

        // Close the QR Code Dialog
        onView(withId(R.id.btnClose))
            .perform(click())
        sleep()

        // --- 8. BACKWARD NAVIGATION ---
        // Navigate back to List Details
        pressBack()
        sleep()

        // Navigate back to Home Lists
        pressBack()
        sleep()

        // --- 9. SEARCH TAB FUNCTIONALITY ---
        // Switch to the Search screen via bottom nav bar
        onView(withId(R.id.searchFragment))
            .perform(click())
        sleep()

        // Verify Search layout is displayed
        onView(withId(R.id.etSearch))
            .check(matches(isDisplayed()))
        sleep()

        // Type query "Snacks" to run search across all lists
        onView(withId(R.id.etSearch))
            .perform(replaceText("Snacks"))
        sleep()

        // Verify search result is displayed with correct list mapping
        onView(allOf(withId(R.id.tvItemName), withText("Snacks"), isDisplayed()))
            .check(matches(isDisplayed()))
        onView(withText("In list: Weekend Party"))
            .check(matches(isDisplayed()))
        sleep()

        // Click search result to navigate straight to List Details
        onView(allOf(withId(R.id.tvItemName), withText("Snacks"), isDisplayed()))
            .perform(click())
        sleep()

        // Verify we are back on the List Detail screen
        onView(withId(R.id.tvDetailListName))
            .check(matches(withText("Weekend Party")))
        sleep()

        // --- 10. STATISTICS TAB FUNCTIONALITY ---
        // Switch to the Statistics tab via bottom nav bar
        onView(withId(R.id.statisticsFragment))
            .perform(click())
        sleep()

        // Verify custom charts and calculated counts are visible
        onView(withId(R.id.donutChartView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.categoryBarChartView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.monthlyListsChartView))
            .check(matches(isDisplayed()))
        sleep()

        // Verify list count and item totals reflect our additions are displayed
        onView(withId(R.id.tvTotalListsCount))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tvTotalItemsCount))
            .check(matches(isDisplayed()))
        sleep(2000)
    }
}
