package com.sangeetmind.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangeetmind.core.ui.theme.SangeetMindTheme
import com.sangeetmind.features.raaglibrary.ui.RaagListScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic Compose UI test to verify the Home screen displays the app title
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun raagListScreen_displaysTopAppBar() {
        // Start the app
        composeTestRule.setContent {
            SangeetMindTheme {
                RaagListScreen()
            }
        }

        // Verify that the top app bar shows "Raag Library"
        composeTestRule
            .onNodeWithText("Raag Library")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun raagListScreen_displaysSearchBar() {
        composeTestRule.setContent {
            SangeetMindTheme {
                RaagListScreen()
            }
        }

        // Verify that the search bar is displayed
        composeTestRule
            .onNodeWithText("Search raags...")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun raagListScreen_searchBarIsInteractive() {
        composeTestRule.setContent {
            SangeetMindTheme {
                RaagListScreen()
            }
        }

        // Type in search bar
        composeTestRule
            .onNodeWithText("Search raags...")
            .performTextInput("Bhairav")

        // Verify text was entered (search functionality would be tested with mocked data)
        composeTestRule
            .onNodeWithText("Bhairav")
            .assertExists()
    }
}

