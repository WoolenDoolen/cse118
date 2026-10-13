package edu.ucsc.cse118.assignment3.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import edu.ucsc.cse118.assignment3.R
import edu.ucsc.cse118.assignment3.data.Message
import edu.ucsc.cse118.assignment3.model.Screen
import edu.ucsc.cse118.assignment3.model.SharedViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageApp(model: SharedViewModel) {
  val snackbarHost = remember { SnackbarHostState() }
  val notice = model.snackbar
  LaunchedEffect(notice) {
    if (notice != null) {
      snackbarHost.showSnackbar(notice)
      model.snackbarShown()
    }
  }
  val hasBack = model.screen != Screen.LOGIN && model.screen != Screen.WORKSPACES
  BackHandler(enabled = hasBack) { model.back() }
  val title = when (model.screen) {
    Screen.LOGIN -> "CSE118 Assignment 3"
    Screen.WORKSPACES -> model.member?.name.orEmpty()
    Screen.CHANNELS -> model.workspace?.name.orEmpty()
    Screen.MESSAGES -> model.channel?.name.orEmpty()
    Screen.DETAIL -> "Message"
    Screen.COMPOSE -> "New Message"
  }

  Scaffold(
    modifier = Modifier.semantics { testTagsAsResourceId = true },
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          if (hasBack) {
            IconButton(onClick = model::back, enabled = !model.saving) {
              Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
            }
          }
        },
        actions = {
          if (model.member != null) {
            TextButton(onClick = model::logout, enabled = !model.saving) { Text("Logout") }
          }
        }
      )
    },
    snackbarHost = { SnackbarHost(snackbarHost) },
    floatingActionButton = {
      if (model.screen == Screen.MESSAGES && !model.loading) {
        FloatingActionButton(onClick = model::composeMessage) {
          Icon(painterResource(R.drawable.ic_compose), contentDescription = "Compose message")
        }
      }
    }
  ) { padding ->
    Column(Modifier.fillMaxSize().padding(padding)) {
      if (model.loading) {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      model.error?.let { error ->
        Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        if (model.screen in listOf(Screen.WORKSPACES, Screen.CHANNELS, Screen.MESSAGES)) {
          TextButton(onClick = model::retry, enabled = !model.loading) { Text("Retry") }
        }
      }
      when (model.screen) {
        Screen.LOGIN -> LoginScreen(model)
        Screen.WORKSPACES -> WorkspaceScreen(model)
        Screen.CHANNELS -> ChannelScreen(model)
        Screen.MESSAGES -> MessagesScreen(model)
        Screen.DETAIL -> DetailScreen(model)
        Screen.COMPOSE -> ComposeScreen(model)
      }
    }
  }

  model.pendingDelete?.let { message ->
    AlertDialog(
      onDismissRequest = model::cancelDelete,
      text = { Text("Delete Message From ${model.memberName(message.member)}?") },
      confirmButton = { TextButton(onClick = model::deleteMessage) { Text("Yes") } },
      dismissButton = { TextButton(onClick = model::cancelDelete) { Text("No") } }
    )
  }
}

@Composable
private fun LoginScreen(model: SharedViewModel) {
  var email by rememberSaveable { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  Column(
    Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Image(
      painterResource(R.drawable.ic_uc_santa_cruz_banana_slug),
      contentDescription = "UCSC Slug Logo",
      modifier = Modifier.size(128.dp)
    )
    OutlinedTextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Email") },
      singleLine = true,
      enabled = !model.loading,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
      modifier = Modifier.fillMaxWidth().testTag("email")
    )
    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password") },
      singleLine = true,
      enabled = !model.loading,
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      modifier = Modifier.fillMaxWidth().testTag("password")
    )
    Button(
      onClick = { model.login(email, password) },
      enabled = email.length >= 4 && password.length >= 4 && !model.loading
    ) { Text("Log in") }
  }
}

@Composable
private fun WorkspaceScreen(model: SharedViewModel) {
  val workspaces by model.workspaces.observeAsState(emptyList())
  LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
    items(workspaces, key = { it.id }) { workspace ->
      ListCard(workspace.name, countText(workspace.channels, "Channel"), !model.loading) {
        model.selectWorkspace(workspace)
      }
    }
  }
}

@Composable
private fun ChannelScreen(model: SharedViewModel) {
  val channels by model.channels.observeAsState(emptyList())
  LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
    items(channels, key = { it.id }) { channel ->
      ListCard(channel.name, countText(channel.messages, "Message"), !model.loading) {
        model.selectChannel(channel)
      }
    }
  }
}

@Composable
private fun ListCard(title: String, subtitle: String, enabled: Boolean, onClick: () -> Unit) {
  Card(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(title, fontWeight = FontWeight.Bold)
      Text(subtitle)
    }
  }
}

private fun countText(count: Int, noun: String): String {
  return "$count $noun${if (count == 1) "" else "s"}"
}

@Composable
private fun MessagesScreen(model: SharedViewModel) {
  val messages by model.messages.observeAsState(emptyList())
  LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
    items(messages, key = { it.id }) { message ->
      MessageCard(message, model)
    }
    item { Spacer(Modifier.height(80.dp)) }
  }
}

@Composable
private fun MessageCard(message: Message, model: SharedViewModel) {
  var offset by remember(message.id) { mutableFloatStateOf(0f) }
  var width by remember { mutableIntStateOf(0) }
  val canSwipe = model.canDelete(message) && !model.loading
  val swipe = if (canSwipe) {
    Modifier.pointerInput(message.id, canSwipe) {
      detectHorizontalDragGestures(
        onDragCancel = { offset = 0f },
        onDragEnd = {
          if (width > 0 && offset >= width * 0.35f) model.askDelete(message)
          offset = 0f
        }
      ) { change, distance ->
        change.consume()
        offset = (offset + distance).coerceIn(0f, width.toFloat())
      }
    }
  } else Modifier
  Card(
    onClick = { model.selectMessage(message) },
    enabled = !model.loading,
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
      .onSizeChanged { width = it.width }
      .offset { IntOffset(offset.roundToInt(), 0) }
      .then(swipe)
  ) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(model.memberName(message.member), fontWeight = FontWeight.Bold)
      Text(message.dateText(), style = MaterialTheme.typography.bodySmall)
      Text(message.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun DetailScreen(model: SharedViewModel) {
  val message = model.message ?: return
  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(model.memberName(message.member), fontWeight = FontWeight.Bold)
    Text(message.dateText())
    Text(message.content)
  }
}

@Composable
private fun ComposeScreen(model: SharedViewModel) {
  Column(
    Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    OutlinedTextField(
      value = model.draft,
      onValueChange = { if (it.length <= 280) model.draft = it },
      label = { Text("Message") },
      minLines = 6,
      enabled = !model.loading,
      modifier = Modifier.fillMaxWidth().testTag("content")
    )
    Button(onClick = model::addMessage, enabled = model.draft.length >= 16 && !model.loading) {
      Text("Add")
    }
  }
}
