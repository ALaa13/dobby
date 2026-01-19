package org.example.utils

import dev.kord.core.entity.Message
import kotlin.time.ExperimentalTime

data class ChatMessage(
    val author: String,
    val content: String,
    val timestamp: String
)

/**
 * Formats messages for AI model consumption
 */
object MessageFormatter {
    /**
     * Converts a list of messages to a formatted string suitable for AI processing
     * @param messages List of messages to format
     * @return Formatted string ready for AI model input
     */
    @OptIn(ExperimentalTime::class)
    fun formatMessagesForAI(messages: List<Message>): List<ChatMessage> {
        return messages.map { message ->
            ChatMessage(
                author = message.author?.username ?: "Unknown",
                content = message.content,
                timestamp = message.timestamp.toString()
            )
        }
    }
}