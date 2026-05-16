package org.example.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.user
import org.example.dto.RoastDeliveryRequest
import org.example.services.DobbyCoreBackend
import org.example.utils.DiscordStrings
import org.example.utils.FetchMessagesConfig
import org.example.utils.fetchMessages
import org.example.utils.formatMessagesForAI
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RoastCommand : ChatInputCommand(), KoinComponent {
    override val name = DiscordStrings.Commands.RoastChannel.NAME
    override val description = DiscordStrings.Commands.RoastChannel.DESCRIPTION
    private val dobbyCoreBackend: DobbyCoreBackend by inject()


    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            integer(
                DiscordStrings.Commands.RoastChannel.Count.NAME,
                DiscordStrings.Commands.RoastChannel.Count.DESCRIPTION
            ) {
                required = false
                choice(DiscordStrings.Commands.RoastChannel.Count.CHOICE_10, 10)
                choice(DiscordStrings.Commands.RoastChannel.Count.CHOICE_50, 50)
                choice(DiscordStrings.Commands.RoastChannel.Count.CHOICE_250, 250)
                choice(DiscordStrings.Commands.RoastChannel.Count.CHOICE_500, 500)
            }
            integer(
                DiscordStrings.Commands.RoastChannel.Since.NAME,
                DiscordStrings.Commands.RoastChannel.Since.DESCRIPTION
            ) {
                required = false
                choice(DiscordStrings.Commands.RoastChannel.Since.CHOICE_30, 30)
                choice(DiscordStrings.Commands.RoastChannel.Since.CHOICE_60, 60)
                choice(DiscordStrings.Commands.RoastChannel.Since.CHOICE_180, 180)
                choice(DiscordStrings.Commands.RoastChannel.Since.CHOICE_360, 360)
                choice(DiscordStrings.Commands.RoastChannel.Since.CHOICE_720, 720)
                choice(DiscordStrings.Commands.RoastChannel.Since.CHOICE_1440, 1440)
            }
            user(
                DiscordStrings.Commands.RoastChannel.Target.NAME,
                DiscordStrings.Commands.RoastChannel.Target.DESCRIPTION
            ) {
                required = false
            }
        }
    }

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val deferredMessage = event.interaction.deferPublicResponse()
        val messageResponse =
            deferredMessage.respond { content = DiscordStrings.Commands.RoastChannel.DEFERRED_MESSAGE }

        // Get command options
        val messagesCount = event.interaction.command.integers[DiscordStrings.Commands.RoastChannel.Count.NAME]?.toInt()
        val sinceMessagesTime =
            event.interaction.command.integers[DiscordStrings.Commands.RoastChannel.Since.NAME]?.toInt()
        val target = event.interaction.command.users[DiscordStrings.Commands.RoastChannel.Target.NAME]

        // The selected target is Bot and not a user
        if (target?.isBot == true) {
            deferredMessage.respond { content = DiscordStrings.Commands.RoastChannel.Target.IS_BOT_REPLY }
            return
        }

        val channel = event.interaction.channel
        val messages = fetchMessages(
            channel,
            event.interaction.id,
            FetchMessagesConfig(
                messagesToFetch = messagesCount,
                sinceMinutes = sinceMessagesTime,
                authorId = target?.id
            ),
        )
        val formattedMessages = formatMessagesForAI(messages)
        dobbyCoreBackend.sendDiscordMessages(
            RoastDeliveryRequest(
                channelId = channel.id.toString(),
                messageId = messageResponse.message.id.toString(),
                messages = formattedMessages
            )
        )
    }
}