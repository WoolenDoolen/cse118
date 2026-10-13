package edu.ucsc.cse118.assignment3.data

import java.io.Serializable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Member(val id: String, val name: String) : Serializable

data class LoginResponse(
  val id: String,
  val name: String,
  val accessToken: String
) : Serializable

data class Workspace(
  val id: String,
  val name: String,
  val owner: String?,
  val channels: Int
) : Serializable

data class Channel(val id: String, val name: String, val messages: Int) : Serializable

data class Message(
  val id: String,
  val member: String,
  val posted: String,
  val content: String
) : Serializable {
  fun canDelete(memberId: String?, workspaceOwner: String?): Boolean {
    return memberId != null && (member == memberId || workspaceOwner == memberId)
  }

  fun dateText(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a", Locale.US)
      .withZone(ZoneOffset.UTC)
    return formatter.format(Instant.parse(posted))
  }
}

fun sortedMessages(messages: List<Message>): List<Message> {
  return messages.sortedByDescending { Instant.parse(it.posted) }
}
