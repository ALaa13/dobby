package org.example.util

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.GuildMessageChannelBehavior
import dev.kord.core.entity.Message
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import org.example.dto.ChatMessage
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime


data class FetchMessagesConfig(
    val messagesToFetch: Int? = 100,
    val sinceMinutes: Int? = null,
    val authorId: Snowflake? = null
)

@OptIn(ExperimentalTime::class)
suspend fun fetchMessages(
    channel: GuildMessageChannelBehavior,
    interactionId: Snowflake,
    config: FetchMessagesConfig
): List<Message> {
    val maxCount = config.messagesToFetch ?: FetchMessagesConfig().messagesToFetch!!

    return channel.getMessagesBefore(interactionId, maxCount)
        .let { flow ->
            if (config.sinceMinutes != null) {
                val sinceTime = Clock.System.now().minus(config.sinceMinutes.minutes)
                flow.takeWhile { it.timestamp >= sinceTime }
            } else {
                flow
            }
        }
        .filter { it.author?.isBot == false && it.content.isNotBlank() }
        .let { flow ->
            if (config.authorId != null) {
                flow.filter { it.author?.id == config.authorId }
            } else {
                flow
            }
        }
        .toList()
        .asReversed()
}


@OptIn(ExperimentalTime::class)
fun formatMessagesForAI(messages: List<Message>): List<ChatMessage> {
    return messages.map { message ->
        ChatMessage(
            author = message.author?.id.toString(),
            content = message.content,
            timestamp = message.timestamp.toString()
        )
    }
}
