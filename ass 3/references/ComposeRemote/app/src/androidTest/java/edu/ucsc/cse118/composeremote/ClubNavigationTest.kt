/*
#######################################################################
#
# Copyright (C) 2026 David C. Harrison. All rights reserved.
#
# You may not use, distribute, publish, or modify this code without
# the express written permission of the copyright holder.
#
#######################################################################
*/
package edu.ucsc.cse118.composeremote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClubNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun waitForClubs() {
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodes(hasText("Arizona Diamondbacks"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun selectClub(name: String) {
        compose.onNodeWithText(name).performClick()
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodes(hasText("Built:", substring = true) and
                hasText("Built: Unknown").not())
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun homeDisplaysTitle() {
        compose.onNodeWithText("MLB Clubs").assertIsDisplayed()
    }

    @Test
    fun homeDoesNotHaveBack() {
        compose.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test
    fun homeDisplaysClub() {
        compose.onNodeWithText("Arizona Diamondbacks").assertIsDisplayed()
    }

    @Test
    fun homeDisplaysStadium() {
        compose.onNodeWithText("Chase Field", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun homeDoesNotDisplayHiddenClub() {
        compose.onNodeWithText("Washington Nationals").assertDoesNotExist()
    }

    @Test
    fun homeScrollsToHiddenClub() {
        compose.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("Washington Nationals"))
        compose.onNodeWithText("Washington Nationals").assertIsDisplayed()
    }

    @Test
    fun selectingClubShowsBack() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

// Bad test (multiple assertions) do not do this
//    @Test
//    fun selectingClubShowsDetails() {
//        selectClub("Arizona Diamondbacks")
//        compose.onNodeWithText("Arizona Diamondbacks").assertIsDisplayed()
//        compose.onNodeWithText("Stadium: Chase Field").assertIsDisplayed()
//        compose.onNodeWithText("Location: Phoenix, Arizona").assertIsDisplayed()
//        compose.onNodeWithText("Capacity: 48,686").assertIsDisplayed()
//        compose.onNodeWithText("Built: 1998").assertIsDisplayed()
//    }

    @Test
    fun selectingClubShowsName() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithText("Arizona Diamondbacks").assertIsDisplayed()
    }

    @Test
    fun selectingClubShowsStadium() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithText("Stadium: Chase Field").assertIsDisplayed()
    }

    @Test
    fun selectingClubShowsLocation() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithText("Location: Phoenix, Arizona").assertIsDisplayed()
    }

    @Test
    fun selectingClubShowsCapacity() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithText("Capacity: 48,686").assertIsDisplayed()
    }

    @Test
    fun selectingClubShowsBuilt() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithText("Built: 1998").assertIsDisplayed()
    }

    @Test
    fun backReturnsToList() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("MLB Clubs").assertIsDisplayed()
    }

    fun backRemovesBack() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test
    fun backReturnsToListAndSelectingAnotherClubUpdatesDetails() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithContentDescription("Back").performClick()
        selectClub("Atlanta Braves")
        compose.onNodeWithText("Stadium: SunTrust Park").assertIsDisplayed()
    }

    @Test
    fun backReturnsToListAndSelectingAnotherClubClearsDetails() {
        selectClub("Arizona Diamondbacks")
        compose.onNodeWithContentDescription("Back").performClick()
        selectClub("Atlanta Braves")
        compose.onNodeWithText("Stadium: Chase Field").assertDoesNotExist()
    }
}
