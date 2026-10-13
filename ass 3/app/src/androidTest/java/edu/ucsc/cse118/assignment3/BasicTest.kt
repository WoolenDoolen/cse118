package edu.ucsc.cse118.assignment3

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BasicTest {
  /**
   * Create and launch the activity under test before each test,
   * and close it after each test.
   */
  @get:Rule
  val composeRule = createAndroidComposeRule<MainActivity>()

  // Change these three lines ONLY
  private val cruzid = androidx.test.platform.app.InstrumentationRegistry.getArguments().getString("email", "your-cruz-id@ucsc.edu")!!
  private val name = androidx.test.platform.app.InstrumentationRegistry.getArguments().getString("name", "Your Name")!!
  private val password = androidx.test.platform.app.InstrumentationRegistry.getArguments().getString("password", "Your Student ID")!!

  private fun waitForText(text: String) {
    TestHelper.waitForText(composeRule, text)
  }

  private fun login() {
    composeRule.onNode(hasSetTextAction() and hasText("Email")).performTextInput( cruzid)
    composeRule.onNode(hasSetTextAction() and hasText("Password")).performTextInput( password)
    composeRule.onNode(hasText("Log in") and hasClickAction()).performClick()
  }
  private fun workspace(workspace: String) {
    login()
    waitForText("$workspace Workspace")
    waitForText("$workspace Workspace")
    composeRule.onNodeWithText("$workspace Workspace").performClick()
  }
  private fun channel(workspace: String, channel: String) {
    workspace(workspace)
    waitForText("$channel Channel")
    composeRule.onNodeWithText("$channel Channel").performClick()
  }

  @Test
  fun login_title() {
    composeRule.onNodeWithText("CSE118 Assignment 3").assertIsDisplayed()
  }
  @Test
  fun login_button() {
    composeRule.onNode(hasText("Log in") and hasClickAction()).assertIsDisplayed()
  }
  @Test
  fun login_email_hint() {
    composeRule.onNode(hasSetTextAction() and hasText("Email")).assertExists()
  }
  @Test
  fun login_password_hint() {
    composeRule.onNode(hasSetTextAction() and hasText("Password")).assertExists()
  }
  @Test
  fun login_button_disabled() {
    composeRule.onNode(hasText("Log in") and hasClickAction()).assertIsNotEnabled()
  }
  @Test
  fun login_short_email() {
    composeRule.onNode(hasSetTextAction() and hasText("Email")).performTextInput( "sh")
    composeRule.onNode(hasSetTextAction() and hasText("Password")).performTextInput( password)
    composeRule.onNode(hasText("Log in") and hasClickAction()).assertIsNotEnabled()
  }
  @Test
  fun login_short_password() {
    composeRule.onNode(hasSetTextAction() and hasText("Email")).performTextInput( cruzid)
    composeRule.onNode(hasSetTextAction() and hasText("Password")).performTextInput( "sh")
    composeRule.onNode(hasText("Log in") and hasClickAction()).assertIsNotEnabled()
  }
  @Test
  fun login_button_enabled() {
    composeRule.onNode(hasSetTextAction() and hasText("Email")).performTextInput( cruzid)
    composeRule.onNode(hasSetTextAction() and hasText("Password")).performTextInput( password)
    composeRule.onNode(hasText("Log in") and hasClickAction()).assertIsEnabled()
  }
  @Test
  fun login_error() {
    composeRule.onNode(hasSetTextAction() and hasText("Email")).performTextInput( cruzid)
    composeRule.onNode(hasSetTextAction() and hasText("Password")).performTextInput( "wrong")
    composeRule.onNode(hasText("Log in") and hasClickAction()).performClick()
    waitForText("Failed to login : HTTP 401")
  }
  @Test
  fun name_in_header() {
    login()
    waitForText(name)
  }

  @Test
  fun logout_returns_to_login() {
    login()
    TestHelper.waitForText(composeRule, "Logout").performClick()
    composeRule.onNodeWithText("Logout").assertDoesNotExist()
  }
  @Test
  fun workspaces() {
    login()
    waitForText("Molly Workspace")
    waitForText("Anna Workspace")
    waitForText("Student Workspace")
  }
  @Test
  fun workspace_counts() {
    login()
    waitForText("1 Channel")
    waitForText("2 Channels")
    waitForText("3 Channels")
  }
  @Test
  fun channel_counts() {
    workspace("Anna")
    waitForText("2 Messages")
    waitForText("0 Messages")
  }
  @Test
  fun message_member() {
    channel("Molly", "Molly")
    waitForText("William Shakespeare")
  }
  @Test
  fun message_title() {
    channel("Molly", "Molly")
    waitForText("Hello, and welcome to my channel!")
  }
  @Test
  fun message_date() {
    channel("Molly", "Molly")
    waitForText("May 1, 2023, 12:18:48 PM")
  }
}
