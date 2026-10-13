package edu.ucsc.cse118.assignment3

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule

object TestHelper {
  fun waitForText(rule: ComposeTestRule, text: String, timeoutMillis: Long = 5000): SemanticsNodeInteraction {
    rule.waitUntil(timeoutMillis) {
      rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }
    return rule.onAllNodes(hasText(text))[0].assertIsDisplayed()
  }
}
