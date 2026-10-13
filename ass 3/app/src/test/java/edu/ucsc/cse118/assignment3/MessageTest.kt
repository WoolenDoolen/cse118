package edu.ucsc.cse118.assignment3

import com.google.gson.Gson
import edu.ucsc.cse118.assignment3.data.Message
import edu.ucsc.cse118.assignment3.data.sortedMessages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageTest {
  private val message = Message("message", "author", "2023-05-01T12:18:48Z", "Hello")

  @Test fun authorCanDelete() {
    assertTrue(message.canDelete("author", "owner"))
  }

  @Test fun workspaceOwnerCanDelete() {
    assertTrue(message.canDelete("owner", "owner"))
  }

  @Test fun otherMemberCannotDelete() {
    assertFalse(message.canDelete("other", "owner"))
  }

  @Test fun loggedOutMemberCannotDelete() {
    assertFalse(message.canDelete(null, null))
  }

  @Test fun datesUseExpectedFormat() {
    assertEquals("May 1, 2023, 12:18:48 PM", message.dateText())
  }

  @Test fun messagesAreSortedByInstantWithNewestFirst() {
    val later = message.copy(id = "later", posted = "2023-05-01T08:00:00-07:00")
    assertEquals(listOf("later", "message"), sortedMessages(listOf(message, later)).map { it.id })
  }

  @Test fun apiMessageKeepsMemberIdAndFullContent() {
    val json = """{"id":"message","member":"author","posted":"2023-05-01T12:18:48Z","content":"Line one\nLine two"}"""
    val parsed = Gson().fromJson(json, Message::class.java)
    assertEquals(message.copy(content = "Line one\nLine two"), parsed)
  }
}
