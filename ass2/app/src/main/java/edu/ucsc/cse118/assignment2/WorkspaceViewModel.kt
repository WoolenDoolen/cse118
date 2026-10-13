package edu.ucsc.cse118.assignment2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.serialization.json.Json
import java.time.Instant

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {
  val workspaces: List<Workspace> = application.resources
    .openRawResource(R.raw.workspaces)
    .bufferedReader()
    .use { Json.decodeFromString<List<Workspace>>(it.readText()) }
    .map { workspace ->
      workspace.copy(channels = workspace.channels.map { channel ->
        channel.copy(messages = channel.messages.sortedByDescending { Instant.parse(it.date) })
      })
    }
}
