package edu.ucsc.cse118.assignment3

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.ucsc.cse118.assignment3.data.*
import edu.ucsc.cse118.assignment3.model.SharedViewModel
import edu.ucsc.cse118.assignment3.repo.MessageRepo
import edu.ucsc.cse118.assignment3.screen.MessageApp
import java.io.IOException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppUiTest {
  @get:Rule val compose = createComposeRule()
  private val repo = FakeMessageRepo()

  @Before fun setup() {
    compose.setContent {
      val model = androidx.compose.runtime.remember { SharedViewModel(repo) }
      MaterialTheme { MessageApp(model) }
    }
  }

  private fun waitFor(text: String) = TestHelper.waitForText(compose, text)

  private fun login() {
    compose.onNode(hasSetTextAction() and hasText("Email")).performTextInput("test@ucsc.edu")
    compose.onNode(hasSetTextAction() and hasText("Password")).performTextInput("password")
    compose.onNodeWithText("Log in").performClick()
    waitFor("Test User")
  }

  private fun openChannel(shared: Boolean = false) {
    login()
    waitFor(if (shared) "Shared Workspace" else "Owner Workspace").performClick()
    waitFor(if (shared) "Shared Channel" else "Owner Channel").performClick()
    waitFor(if (shared) "A message I cannot delete." else "Other person's message.")
  }

  private fun composeMessage() {
    compose.onNodeWithContentDescription("Compose message").performClick()
  }

  private fun swipe(text: String) {
    compose.onNodeWithText(text).performTouchInput { swipeRight() }
  }

  @Test fun loginFailureShowsHttpStatus() {
    repo.failLogin = true
    compose.onNode(hasSetTextAction() and hasText("Email")).performTextInput("test@ucsc.edu")
    compose.onNode(hasSetTextAction() and hasText("Password")).performTextInput("wrong")
    compose.onNodeWithText("Log in").performClick()
    waitFor("Failed to login : HTTP 401")
  }

  @Test fun loginShowsChannelCounts() {
    login()
    compose.onAllNodesWithText("1 Channel").assertCountEquals(2)
  }

  @Test fun channelShowsMessageCount() {
    login()
    waitFor("Owner Workspace").performClick()
    waitFor("2 Messages")
  }

  @Test fun detailShowsAllLines() {
    openChannel()
    compose.onNodeWithText("My original message.\nSecond line.").performClick()
    compose.onNodeWithText("My original message.\nSecond line.").assertIsDisplayed()
  }

  @Test fun messagesShowMemberNames() {
    openChannel()
    compose.onNodeWithText("Other User").assertIsDisplayed()
  }

  @Test fun addDisabledForFifteenCharacters() {
    openChannel()
    composeMessage()
    compose.onNode(hasSetTextAction()).performTextInput("123456789012345")
    compose.onNodeWithText("Add").assertIsNotEnabled()
  }

  @Test fun addEnabledForSixteenCharacters() {
    openChannel()
    composeMessage()
    compose.onNode(hasSetTextAction()).performTextInput("1234567890123456")
    compose.onNodeWithText("Add").assertIsEnabled()
  }

  @Test fun creationShowsSnackbar() {
    openChannel()
    composeMessage()
    compose.onNode(hasSetTextAction()).performTextInput("A new test message.")
    compose.onNodeWithText("Add").performClick()
    waitFor("Message Created")
  }

  @Test fun creationUpdatesChannelCount() {
    openChannel()
    composeMessage()
    compose.onNode(hasSetTextAction()).performTextInput("A new test message.")
    compose.onNodeWithText("Add").performClick()
    waitFor("A new test message.")
    pressBack()
    waitFor("3 Messages")
  }

  @Test fun cancellingComposeKeepsCount() {
    openChannel()
    composeMessage()
    compose.onNode(hasSetTextAction()).performTextInput("An unsent message.")
    pressBack()
    // The first Back can close the keyboard.
    if (compose.onAllNodesWithText("Add").fetchSemanticsNodes().isNotEmpty()) pressBack()
    pressBack()
    waitFor("2 Messages")
  }

  @Test fun creationFailureKeepsDraft() {
    openChannel()
    repo.failCreate = true
    composeMessage()
    compose.onNode(hasSetTextAction()).performTextInput("A draft worth keeping.")
    compose.onNodeWithText("Add").performClick()
    waitFor("Failed to create message : HTTP 500")
    compose.onNode(hasSetTextAction() and hasText("A draft worth keeping.")).assertExists()
  }

  @Test fun authorCanDeleteInSharedWorkspace() {
    openChannel(shared = true)
    swipe("My own shared channel message.")
    waitFor("Yes").performClick()
    waitFor("Message Deleted")
    compose.onNodeWithText("My own shared channel message.").assertDoesNotExist()
  }

  @Test fun workspaceOwnerCanDeleteOtherMembersMessage() {
    openChannel()
    swipe("Other person's message.")
    waitFor("Yes").performClick()
    waitFor("Message Deleted")
    compose.onNodeWithText("Other person's message.").assertDoesNotExist()
  }

  @Test fun anotherMembersMessageCannotBeDeletedInSharedWorkspace() {
    openChannel(shared = true)
    swipe("A message I cannot delete.")
    compose.onNodeWithText("Yes").assertDoesNotExist()
  }

  @Test fun cancellingDeleteRestoresMessage() {
    openChannel()
    swipe("Other person's message.")
    waitFor("No").performClick()
    compose.onNodeWithText("Other person's message.").assertIsDisplayed()
  }

  @Test fun leftSwipeDoesNotDelete() {
    openChannel()
    compose.onNodeWithText("Other person's message.").performTouchInput { swipeLeft() }
    compose.onNodeWithText("Yes").assertDoesNotExist()
  }

  @Test fun deleteFailureKeepsMessage() {
    openChannel()
    repo.failDelete = true
    swipe("Other person's message.")
    waitFor("Yes").performClick()
    waitFor("Failed to delete message : HTTP 500")
    compose.onNodeWithText("Other person's message.").assertIsDisplayed()
  }

  @Test fun deletionUpdatesChannelCount() {
    openChannel()
    swipe("Other person's message.")
    waitFor("Yes").performClick()
    waitFor("Message Deleted")
    pressBack()
    waitFor("1 Message")
  }

  @Test fun logoutReturnsToEmptyLogin() {
    openChannel()
    compose.onNodeWithText("Logout").performClick()
    compose.onNode(hasText("Log in") and hasClickAction()).assertIsNotEnabled()
  }
}

private class FakeMessageRepo : MessageRepo() {
  var failLogin = false
  var failCreate = false
  var failDelete = false
  private var nextId = 0
  private val channelMessages = mutableMapOf(
    "owner-channel" to listOf(
      Message("one", "other", "2023-05-01T12:18:48Z", "Other person's message."),
      Message("two", "me", "2023-04-01T12:00:00Z", "My original message.\nSecond line.")
    ),
    "shared-channel" to listOf(
      Message("three", "other", "2023-05-01T12:18:48Z", "A message I cannot delete."),
      Message("four", "me", "2023-04-01T12:00:00Z", "My own shared channel message.")
    )
  )

  override suspend fun login(email: String, password: String): LoginResponse {
    if (failLogin) throw IOException("HTTP 401")
    return LoginResponse("me", "Test User", "test-token")
  }

  override suspend fun members(token: String) = listOf(Member("me", "Test User"), Member("other", "Other User"))

  override suspend fun workspaces(token: String) = listOf(
    Workspace("owner", "Owner Workspace", "me", 1),
    Workspace("shared", "Shared Workspace", "other", 1)
  )

  override suspend fun channels(token: String, workspaceId: String): List<Channel> {
    val id = "$workspaceId-channel"
    return listOf(Channel(id, if (workspaceId == "owner") "Owner Channel" else "Shared Channel", channelMessages[id]!!.size))
  }

  override suspend fun messages(token: String, channelId: String) = channelMessages[channelId].orEmpty()

  override suspend fun createMessage(token: String, channelId: String, content: String): Message {
    if (failCreate) throw IOException("HTTP 500")
    val message = Message("created-${nextId++}", "me", "2026-10-12T12:00:00Z", content)
    channelMessages[channelId] = channelMessages[channelId].orEmpty() + message
    return message
  }

  override suspend fun deleteMessage(token: String, messageId: String) {
    if (failDelete) throw IOException("HTTP 500")
    channelMessages.keys.toList().forEach { id ->
      channelMessages[id] = channelMessages[id].orEmpty().filter { it.id != messageId }
    }
  }
}
