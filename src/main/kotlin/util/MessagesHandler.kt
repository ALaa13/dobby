package org.example.util

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.GuildMessageChannelBehavior
import dev.kord.core.entity.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.example.dto.ChatMessage
import kotlin.time.ExperimentalTime


data class FetchMessagesConfig(
    val messagesToFetch: Int? = 50,
    val authorId: Snowflake? = null
)

@OptIn(ExperimentalTime::class)
suspend fun fetchMessages(
    channel: GuildMessageChannelBehavior,
    interactionId: Snowflake,
    config: FetchMessagesConfig
): List<Message> {
    var messageFlow: Flow<Message> = channel.getMessagesBefore(interactionId, config.messagesToFetch)
    messageFlow = messageFlow.filter { message ->
        message.author?.isBot == false && message.content.isNotBlank()
    }
    if (config.authorId != null) {
        messageFlow = messageFlow.filter { it.author?.id == config.authorId }
    }
    return messageFlow.toList().asReversed()
}


@OptIn(ExperimentalTime::class)
fun formatMessagesForAI(messages: List<Message>): List<ChatMessage> {
    return messages.map { message ->
        ChatMessage(
            discordUserId = message.author?.id.toString(),
            displayName = message.author?.username ?: "Unknown",
            content = message.content,
            avatarHash = message.author?.data?.avatar,
            timestamp = message.timestamp.toString()
        )
    }
}
