package edu.ucsc.cse118.assignment3.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ucsc.cse118.assignment3.data.Channel
import edu.ucsc.cse118.assignment3.data.LoginResponse
import edu.ucsc.cse118.assignment3.data.Member
import edu.ucsc.cse118.assignment3.data.Message
import edu.ucsc.cse118.assignment3.data.Workspace
import edu.ucsc.cse118.assignment3.data.sortedMessages
import edu.ucsc.cse118.assignment3.repo.MessageRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen { LOGIN, WORKSPACES, CHANNELS, MESSAGES, DETAIL, COMPOSE }

class SharedViewModel(private val repo: MessageRepo = MessageRepo()) : ViewModel() {
  private var requestJob: Job? = null
  private val workspaceData = MutableLiveData<List<Workspace>>(emptyList())
  private val channelData = MutableLiveData<List<Channel>>(emptyList())
  private val messageData = MutableLiveData<List<Message>>(emptyList())

  val workspaces: LiveData<List<Workspace>> = workspaceData
  val channels: LiveData<List<Channel>> = channelData
  val messages: LiveData<List<Message>> = messageData

  var screen by mutableStateOf(Screen.LOGIN)
    private set
  var member by mutableStateOf<LoginResponse?>(null)
    private set
  var members by mutableStateOf<List<Member>>(emptyList())
    private set
  var workspace by mutableStateOf<Workspace?>(null)
    private set
  var channel by mutableStateOf<Channel?>(null)
    private set
  var message by mutableStateOf<Message?>(null)
    private set
  var pendingDelete by mutableStateOf<Message?>(null)
    private set
  var loading by mutableStateOf(false)
    private set
  var saving by mutableStateOf(false)
    private set
  var error by mutableStateOf<String?>(null)
    private set
  var snackbar by mutableStateOf<String?>(null)
    private set
  var draft by mutableStateOf("")

  fun login(email: String, password: String) {
    if (loading || email.length < 4 || password.length < 4) return
    runRequest("Failed to login") {
      val loggedIn = repo.login(email.trim(), password)
      val people = repo.members(loggedIn.accessToken)
      val spaces = repo.workspaces(loggedIn.accessToken)
      member = loggedIn
      members = people
      workspaceData.value = spaces
      screen = Screen.WORKSPACES
    }
  }

  fun selectWorkspace(selected: Workspace) {
    workspace = selected
    channel = null
    channelData.value = emptyList()
    screen = Screen.CHANNELS
    loadChannels()
  }

  fun selectChannel(selected: Channel) {
    channel = selected
    messageData.value = emptyList()
    screen = Screen.MESSAGES
    loadMessages()
  }

  fun selectMessage(selected: Message) {
    message = selected
    error = null
    screen = Screen.DETAIL
  }

  fun composeMessage() {
    draft = ""
    error = null
    screen = Screen.COMPOSE
  }

  fun addMessage() {
    val loggedIn = member ?: return
    val selected = channel ?: return
    if (loading || draft.length !in 16..280) return
    runRequest("Failed to create message", mutation = true) {
      val created = repo.createMessage(loggedIn.accessToken, selected.id, draft)
      messageData.value = sortedMessages(messageData.value.orEmpty() + created)
      updateMessageCount()
      draft = ""
      screen = Screen.MESSAGES
      snackbar = "Message Created"
    }
  }

  fun canDelete(selected: Message): Boolean {
    return selected.canDelete(member?.id, workspace?.owner)
  }

  fun askDelete(selected: Message) {
    if (!loading && canDelete(selected)) pendingDelete = selected
  }

  fun cancelDelete() {
    pendingDelete = null
  }

  fun deleteMessage() {
    val loggedIn = member ?: return
    val selected = pendingDelete ?: return
    if (loading || !canDelete(selected)) return
    pendingDelete = null
    runRequest("Failed to delete message", mutation = true) {
      repo.deleteMessage(loggedIn.accessToken, selected.id)
      messageData.value = messageData.value.orEmpty().filter { it.id != selected.id }
      updateMessageCount()
      snackbar = "Message Deleted"
    }
  }

  private fun updateMessageCount() {
    val selected = channel ?: return
    val updated = selected.copy(messages = messageData.value.orEmpty().size)
    channel = updated
    channelData.value = channelData.value.orEmpty().map {
      if (it.id == updated.id) updated else it
    }
  }

  fun memberName(memberId: String): String {
    return members.find { it.id == memberId }?.name
      ?: member?.takeIf { it.id == memberId }?.name
      ?: "Unknown member"
  }

  fun back() {
    if (saving) return
    requestJob?.cancel()
    loading = false
    error = null
    pendingDelete = null
    screen = when (screen) {
      Screen.CHANNELS -> Screen.WORKSPACES
      Screen.MESSAGES -> Screen.CHANNELS
      Screen.DETAIL, Screen.COMPOSE -> Screen.MESSAGES
      else -> screen
    }
  }

  fun logout() {
    if (saving) return
    requestJob?.cancel()
    member = null
    members = emptyList()
    workspace = null
    channel = null
    message = null
    pendingDelete = null
    workspaceData.value = emptyList()
    channelData.value = emptyList()
    messageData.value = emptyList()
    draft = ""
    error = null
    snackbar = null
    loading = false
    screen = Screen.LOGIN
  }

  fun retry() {
    when (screen) {
      Screen.WORKSPACES -> runRequest("Failed to load workspaces") {
        workspaceData.value = repo.workspaces(member!!.accessToken)
      }
      Screen.CHANNELS -> loadChannels()
      Screen.MESSAGES -> loadMessages()
      else -> Unit
    }
  }

  fun snackbarShown() {
    snackbar = null
  }

  private fun loadChannels() {
    val loggedIn = member ?: return
    val selected = workspace ?: return
    runRequest("Failed to load channels") {
      channelData.value = repo.channels(loggedIn.accessToken, selected.id)
    }
  }

  private fun loadMessages() {
    val loggedIn = member ?: return
    val selected = channel ?: return
    runRequest("Failed to load messages") {
      messageData.value = sortedMessages(repo.messages(loggedIn.accessToken, selected.id))
      updateMessageCount()
    }
  }

  private fun runRequest(
    failure: String,
    mutation: Boolean = false,
    action: suspend () -> Unit
  ) {
    requestJob?.cancel()
    loading = true
    saving = mutation
    error = null
    requestJob = viewModelScope.launch {
      try {
        action()
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (exception: Exception) {
        error = "$failure : ${exception.message ?: "Network error"}"
      } finally {
        if (isActive) {
          loading = false
          saving = false
        }
      }
    }
  }
}
