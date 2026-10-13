package edu.ucsc.cse118.assignment2

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class Workspace(val name: String, val channels: List<Channel>)

@Serializable
data class Channel(val name: String, val messages: List<Message>)

@Serializable
data class Message(val user: User, val date: String, val content: String)

@Serializable
data class User(val name: String, val email: String)

private val dateFormatter = DateTimeFormatter
  .ofPattern("MMM d, yyyy, h:mm:ss a", Locale.US)
  .withZone(ZoneOffset.UTC)

fun Message.formattedDate(): String = dateFormatter.format(Instant.parse(date))
