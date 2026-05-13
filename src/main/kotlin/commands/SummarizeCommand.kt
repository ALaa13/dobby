package org.example.commands

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.GuildMessageChannelBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import org.example.utils.FetchMessagesConfig
import org.example.utils.MessageFormatter
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class SummarizeCommand : ChatInputCommand(), KoinComponent {
    override val name = "summarize"
    override val description = "Summarize the messages in a channel"

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            integer("count", "How many messages to summarize") {
                required = false
                choice("Last 10 messages", 10)
                choice("Last 50 messages", 50)
                choice("Last 250 messages", 250)
                choice("Last 500 messages", 500)
            }
            integer("since", "How far back to read messages") {
                required = false
                choice("Last 30 minutes", 30)
                choice("Last 1 hour", 60)
                choice("Last 3 hours", 180)
                choice("Last 6 hours", 360)
                choice("Last 12 hours", 720)
                choice("Last 24 hours", 1440)
            }
            string("prompt", "Custom prompt for the AI") {
                required = false
            }
        }
    }

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val deferredMessage = event.interaction.deferPublicResponse()
        val user = event.interaction.user
        val response = deferredMessage.respond { content = "Generating summary..." }

        // Get command options
        val customPrompt = event.interaction.command.strings["prompt"]
        val messagesCount = event.interaction.command.integers["count"]?.toInt()
        val sinceMessagesTime = event.interaction.command.integers["since"]?.toInt()

        val channel = event.interaction.channel
        val messages = fetchMessages(
            channel, event.interaction.id,
            FetchMessagesConfig(maxMessagesToFetch = messagesCount, sinceTimestamp = sinceMessagesTime)
        )
        // Format messages for Backend end point
        val formattedMessages = MessageFormatter.formatMessagesForAI(messages)
        val summarizedMessageText = "Working on it lol" //TODO: Implement Backend
        response.edit {
            content = "${user.mention} Summary Generated!"
            embed {
                title = "Channel Summary"
                description = summarizedMessageText
                color = Color(0x5865F2)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun fetchMessages(
        channel: GuildMessageChannelBehavior,
        interactionId: Snowflake,
        config: FetchMessagesConfig
    ): List<Message> {
        val maxCount = config.maxMessagesToFetch ?: FetchMessagesConfig().maxMessagesToFetch

        val messages = channel.getMessagesBefore(interactionId, maxCount)
            .let {
                if (config.sinceTimestamp != null) {
                    val sinceTime = Clock.System.now().minus(config.sinceTimestamp.minutes)
                    it.takeWhile { message -> message.timestamp >= sinceTime }
                } else {
                    it
                }
            }

        return messages
            .filter { it.author?.isBot == false && it.content.isNotBlank() }
            .toList()
            .asReversed()
    }
}