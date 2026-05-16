package org.example.commands

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.GuildMessageChannelBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.integer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import org.example.dto.RoastDeliveryRequest
import org.example.services.DobbyCoreBackend
import org.example.utils.DiscordStrings
import org.example.utils.FetchMessagesConfig
import org.example.utils.MessageFormatter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class RoastCommand : ChatInputCommand(), KoinComponent {
    override val name = DiscordStrings.Commands.Roast.NAME
    override val description = DiscordStrings.Commands.Roast.DESCRIPTION
    private val dobbyCoreBackend: DobbyCoreBackend by inject()


    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            integer(DiscordStrings.Commands.Roast.Count.NAME, DiscordStrings.Commands.Roast.Count.DESCRIPTION) {
                required = false
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_10, 10)
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_50, 50)
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_250, 250)
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_500, 500)
            }
            integer(DiscordStrings.Commands.Roast.Since.NAME, DiscordStrings.Commands.Roast.Since.DESCRIPTION) {
                required = false
                choice(DiscordStrings.Commands.Roast.Since.CHOICE_30, 30)
                choice(DiscordStrings.Commands.Roast.Since.CHOICE_60, 60)
                choice(DiscordStrings.Commands.Roast.Since.CHOICE_180, 180)
                choice(DiscordStrings.Commands.Roast.Since.CHOICE_360, 360)
                choice(DiscordStrings.Commands.Roast.Since.CHOICE_720, 720)
                choice(DiscordStrings.Commands.Roast.Since.CHOICE_1440, 1440)
            }
        }
    }

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val deferredMessage = event.interaction.deferPublicResponse()
        val messageResponse = deferredMessage.respond { content = DiscordStrings.Commands.Roast.DEFERRED_MESSAGE }

        // Get command options
        val messagesCount = event.interaction.command.integers[DiscordStrings.Commands.Roast.Count.NAME]?.toInt()
        val sinceMessagesTime = event.interaction.command.integers[DiscordStrings.Commands.Roast.Since.NAME]?.toInt()

        val channel = event.interaction.channel
        val messages = fetchMessages(
            channel, event.interaction.id,
            FetchMessagesConfig(maxMessagesToFetch = messagesCount, sinceTimestamp = sinceMessagesTime)
        )
        val formattedMessages = MessageFormatter.formatMessagesForAI(messages)
        dobbyCoreBackend.sendDiscordMessages(
            RoastDeliveryRequest(
                channelId = channel.id.toString(),
                messageId = messageResponse.message.id.toString(),
                messages = formattedMessages
            )
        )
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