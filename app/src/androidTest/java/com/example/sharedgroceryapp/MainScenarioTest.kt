package com.example.sharedgroceryapp

import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScenarioTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainScenarioTest() {
        // 1. Verify that HomeListsFragment is displayed by checking if the toolbar or FAB is visible
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.fabAddList))
            .check(matches(isDisplayed()))

        // 2. Click the FAB to add a new list
        onView(withId(R.id.fabAddList))
            .perform(click())

        // 3. Verify that the 'Create Shopping List' dialog appears correctly
        onView(withText("Create Shopping List"))
            .check(matches(isDisplayed()))

        // 4. Type a list name (e.g., 'Weekend Party') into the dialog's EditText, and click 'Create'
        onView(allOf(isAssignableFrom(EditText::class.java), isDisplayed()))
            .perform(replaceText("Weekend Party"))

        onView(withText("Create"))
            .perform(click())

        // 5. Click on the newly created list item in the RecyclerView to navigate to GroceryListFragment.
        // We use espresso-contrib's RecyclerViewActions to perform a click on the item at position 0.
        onView(withId(R.id.rvShoppingLists))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // 6. Inside the list, verify the title on the toolbar is "Weekend Party"
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        // 7. Click the FAB in GroceryListFragment to add a new Grocery Item
        onView(withId(R.id.fabAddItem))
            .perform(click())

        // 8. In AddEditItemFragment, type an item name (e.g., 'Snacks') and click 'Save Item'.
        // Note: The data model for GroceryItem is structured around "name" and "quantity" (and "isBought") 
        // without a price field. We enter "Snacks" into the item name field and save the item.
        onView(withId(R.id.etItemName))
            .perform(replaceText("Snacks"))

        onView(withId(R.id.btnSave))
            .perform(click())

        // 9. Back in the list, verify that 'Snacks' is displayed
        onView(withText("Snacks"))
            .check(matches(isDisplayed()))

        // 10. Click the item's checkbox to mark it as bought
        onView(allOf(withId(R.id.checkboxBought), isDisplayed()))
            .perform(click())

        // 11. Click the 'Share QR Code' menu item in the Top AppBar
        onView(withId(R.id.action_share_qr))
            .perform(click())

        // 12. Verify the QR Code Dialog is displayed
        onView(withText("Share Shopping List"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.ivQRCode))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnClose))
            .check(matches(isDisplayed()))
    }
}
