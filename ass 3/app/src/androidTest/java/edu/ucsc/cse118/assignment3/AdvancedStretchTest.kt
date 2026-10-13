package edu.ucsc.cse118.assignment3

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.net.URI
import java.util.UUID
import javax.net.ssl.HttpsURLConnection
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// These tests use only visible text and the specified Compose message description.
@RunWith(AndroidJUnit4::class)
class AdvancedStretchTest {
  @get:Rule val compose = createAndroidComposeRule<MainActivity>()
  private val args = InstrumentationRegistry.getArguments()
  private val email = args.getString("email", "")
  private val password = args.getString("password", "")
  private val createdContent = mutableListOf<String>()
  private val workspaceName = "Molly Workspace"
  private val channelName = "Molly Channel"

  private fun waitFor(text: String): SemanticsNodeInteraction {
    compose.waitUntil(15000) {
      compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
    return compose.onNodeWithText(text)
  }

  @Before fun openMessages() {
    assumeTrue("Pass email and password as instrumentation arguments", !email.isNullOrBlank() && !password.isNullOrBlank())
    compose.onNode(hasSetTextAction() and hasText("Email")).performTextInput(email!!)
    compose.onNode(hasSetTextAction() and hasText("Password")).performTextInput(password!!)
    compose.onNode(hasText("Log in", ignoreCase = true) and hasClickAction()).performClick()
    waitFor(workspaceName).performClick()
    waitFor(channelName).performClick()
    waitFor("Hello, and welcome to my channel!")
  }

  private fun openComposer() {
    compose.waitUntil(15000) {
      compose.onAllNodesWithContentDescription("Compose message").fetchSemanticsNodes().isNotEmpty()
    }
    compose.onNodeWithContentDescription("Compose message").performClick()
  }

  private fun addButton() = compose.onNode(hasText("Add", ignoreCase = true) and hasClickAction())

  private fun createMessage(): String {
    val content = "Test ${UUID.randomUUID().toString().take(8)} message"
    createdContent.add(content)
    openComposer()
    compose.onNode(hasSetTextAction()).performTextInput(content)
    addButton().performClick()
    waitFor(content)
    return content
  }

  private fun swipe(content: String) {
    compose.onNodeWithText(content).performTouchInput { swipeRight() }
    waitFor("No")
  }

  @Test fun composeButtonIsDisplayed() {
    compose.onNodeWithContentDescription("Compose message").assertIsDisplayed()
  }

  @Test fun emptyMessageCannotBeAdded() {
    openComposer()
    addButton().assertIsNotEnabled()
  }

  @Test fun fifteenCharactersCannotBeAdded() {
    openComposer()
    compose.onNode(hasSetTextAction()).performTextInput("123456789012345")
    addButton().assertIsNotEnabled()
  }

  @Test fun sixteenCharactersCanBeAdded() {
    openComposer()
    compose.onNode(hasSetTextAction()).performTextInput("1234567890123456")
    addButton().assertIsEnabled()
  }

  @Test fun creationShowsSnackbar() {
    createMessage()
    waitFor("Message Created").assertIsDisplayed()
  }

  @Test fun createdMessageAppearsInList() {
    val content = createMessage()
    compose.onNodeWithText(content).assertIsDisplayed()
  }

  @Test fun cancellingComposerDoesNotCreateMessage() {
    val content = "Test ${UUID.randomUUID().toString().take(8)} unsent"
    openComposer()
    compose.onNode(hasSetTextAction()).performTextInput(content)
    pressBack()
    if (compose.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()) pressBack()
    waitFor("Hello, and welcome to my channel!")
    compose.onNodeWithText(content).assertDoesNotExist()
  }

  @Test fun ownMessageSwipeShowsConfirmation() {
    swipe(createMessage())
    compose.onNodeWithText("Yes").assertIsDisplayed()
  }

  @Test fun noKeepsOwnMessage() {
    val content = createMessage()
    swipe(content)
    compose.onNodeWithText("No").performClick()
    compose.onNodeWithText(content).assertIsDisplayed()
  }

  @Test fun yesDeletesOwnMessage() {
    val content = createMessage()
    swipe(content)
    compose.onNodeWithText("Yes").performClick()
    waitFor("Message Deleted")
    compose.onNodeWithText(content).assertDoesNotExist()
  }

  @Test fun deletionShowsSnackbar() {
    swipe(createMessage())
    compose.onNodeWithText("Yes").performClick()
    waitFor("Message Deleted").assertIsDisplayed()
  }

  @Test fun otherMembersMessageCannotBeDeleted() {
    compose.onNodeWithText("Hello, and welcome to my channel!").performTouchInput { swipeRight() }
    compose.onNodeWithText("Yes").assertDoesNotExist()
  }

  @Test fun workspaceOwnerCanRequestDeletionOfAnotherMembersMessage() {
    val login = JSONObject(request("POST", "/login", body = JSONObject()
      .put("email", email).put("password", password).toString()))
    val token = login.getString("accessToken")
    val workspaces = JSONArray(request("GET", "/workspace", token))
    val owned = (0 until workspaces.length()).map { workspaces.getJSONObject(it) }
      .firstOrNull { it.optString("owner") == login.getString("id") }
    assumeTrue("An owned workspace is needed for this test", owned != null)
    val channels = JSONArray(request("GET", "/workspace/${owned!!.getString("id")}/channel", token))
    var selectedChannel: JSONObject? = null
    var selectedMessage: JSONObject? = null
    for (index in 0 until channels.length()) {
      val candidate = channels.getJSONObject(index)
      val messages = JSONArray(request("GET", "/channel/${candidate.getString("id")}/message", token))
      val foreignMessage = (0 until messages.length()).map { messages.getJSONObject(it) }
        .firstOrNull { it.getString("member") != login.getString("id") }
      if (foreignMessage != null) {
        selectedChannel = candidate
        selectedMessage = foreignMessage
        break
      }
    }
    assumeTrue("An owned workspace containing another member's message is needed", selectedMessage != null)
    pressBack()
    pressBack()
    waitFor(owned.getString("name")).performClick()
    waitFor(selectedChannel!!.getString("name")).performClick()
    val content = selectedMessage!!.getString("content")
    waitFor(content)
    swipe(content)
    // Cancel the dialog afterward so this test preserves existing messages.
    compose.onNodeWithText("Yes").assertIsDisplayed()
    compose.onNodeWithText("No").performClick()
  }

  @Test fun leftSwipeDoesNotRequestDeletion() {
    val content = createMessage()
    compose.onNodeWithText(content).performTouchInput { swipeLeft() }
    compose.onNodeWithText("Yes").assertDoesNotExist()
  }

  @After fun removeTestMessages() {
    if (createdContent.isEmpty()) return
    val login = JSONObject(request("POST", "/login", body = JSONObject()
      .put("email", email).put("password", password).toString()))
    val token = login.getString("accessToken")
    val workspaces = JSONArray(request("GET", "/workspace", token))
    val workspace = (0 until workspaces.length()).map { workspaces.getJSONObject(it) }
      .first { it.getString("name") == workspaceName }
    val channels = JSONArray(request("GET", "/workspace/${workspace.getString("id")}/channel", token))
    val channel = (0 until channels.length()).map { channels.getJSONObject(it) }
      .first { it.getString("name") == channelName }
    val messages = JSONArray(request("GET", "/channel/${channel.getString("id")}/message", token))
    for (index in 0 until messages.length()) {
      val message = messages.getJSONObject(index)
      if (message.getString("content") in createdContent && message.getString("member") == login.getString("id")) {
        request("DELETE", "/message/${message.getString("id")}", token)
      }
    }
  }

  private fun request(method: String, path: String, token: String? = null, body: String? = null): String {
    val connection = URI("https://cse118.com/api/v2$path").toURL().openConnection() as HttpsURLConnection
    try {
      connection.requestMethod = method
      connection.connectTimeout = 15000
      connection.readTimeout = 15000
      if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
      if (body != null) {
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.bufferedWriter().use { it.write(body) }
      }
      check(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
      return connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
      connection.disconnect()
    }
  }
}
