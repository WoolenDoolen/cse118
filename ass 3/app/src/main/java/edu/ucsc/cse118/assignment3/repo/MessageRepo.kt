package edu.ucsc.cse118.assignment3.repo

import com.google.gson.Gson
import edu.ucsc.cse118.assignment3.data.Channel
import edu.ucsc.cse118.assignment3.data.LoginResponse
import edu.ucsc.cse118.assignment3.data.Member
import edu.ucsc.cse118.assignment3.data.Message
import edu.ucsc.cse118.assignment3.data.Workspace
import java.io.IOException
import java.net.URI
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class MessageRepo {
  private val baseUrl = "https://cse118.com/api/v2"
  private val gson = Gson()

  open suspend fun login(email: String, password: String): LoginResponse =
    withContext(Dispatchers.IO) {
      val body = gson.toJson(mapOf("email" to email, "password" to password))
      gson.fromJson(request("POST", "/login", body = body), LoginResponse::class.java)
    }

  open suspend fun members(token: String): List<Member> = withContext(Dispatchers.IO) {
    gson.fromJson(request("GET", "/member", token), Array<Member>::class.java).toList()
  }

  open suspend fun workspaces(token: String): List<Workspace> = withContext(Dispatchers.IO) {
    gson.fromJson(request("GET", "/workspace", token), Array<Workspace>::class.java).toList()
  }

  open suspend fun channels(token: String, workspaceId: String): List<Channel> =
    withContext(Dispatchers.IO) {
      gson.fromJson(
        request("GET", "/workspace/$workspaceId/channel", token),
        Array<Channel>::class.java
      ).toList()
    }

  open suspend fun messages(token: String, channelId: String): List<Message> =
    withContext(Dispatchers.IO) {
      gson.fromJson(
        request("GET", "/channel/$channelId/message", token),
        Array<Message>::class.java
      ).toList()
    }

  open suspend fun createMessage(token: String, channelId: String, content: String): Message =
    withContext(Dispatchers.IO) {
      val body = gson.toJson(mapOf("content" to content))
      gson.fromJson(
        request("POST", "/channel/$channelId/message", token, body),
        Message::class.java
      )
    }

  open suspend fun deleteMessage(token: String, messageId: String) = withContext(Dispatchers.IO) {
    request("DELETE", "/message/$messageId", token)
    Unit
  }

  private fun request(
    method: String,
    path: String,
    token: String? = null,
    body: String? = null
  ): String {
    val connection = URI(baseUrl + path).toURL().openConnection() as HttpsURLConnection
    try {
      connection.requestMethod = method
      connection.connectTimeout = 15000
      connection.readTimeout = 15000
      connection.setRequestProperty("Accept", "application/json")
      if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
      if (body != null) {
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body) }
      }
      val status = connection.responseCode
      if (status !in 200..299) throw IOException("HTTP $status")
      return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    } finally {
      connection.disconnect()
    }
  }
}
