package org.example.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import org.example.dto.RoastDeliveryRequest
import org.example.services.DobbyCoreBackend
import org.example.utils.DiscordStrings
import org.example.utils.FetchMessagesConfig
import org.example.utils.fetchMessages
import org.example.utils.formatMessagesForAI
import org.koin.core.component.inject


class UserRoastCommand : UserCommand() {
    private val dobbyCoreClient: DobbyCoreBackend by inject()
    override val name: String = DiscordStrings.Commands.RoastUser.NAME

    override suspend fun run(event: GuildUserCommandInteractionCreateEvent) {
        val interaction = event.interaction

        val deferredMessage = interaction.deferPublicResponse()
        val messageResponse = deferredMessage.respond {
            content = DiscordStrings.Commands.RoastUser.DEFERRED_MESSAGE
        }

        val targetUser = interaction.getTarget().asUser()
        if (targetUser.isBot) {
            deferredMessage.respond { content = DiscordStrings.Commands.RoastChannel.Target.IS_BOT_REPLY }
            return
        }

        val channel = event.interaction.channel
        val messages = fetchMessages(
            channel,
            event.interaction.id,
            FetchMessagesConfig(authorId = targetUser.id)
        )

        val formatedMessages = formatMessagesForAI(messages)
        dobbyCoreClient.sendDiscordMessages(
            RoastDeliveryRequest(
                channelId = channel.id.toString(),
                messageId = messageResponse.message.id.toString(),
                messages = formatedMessages
            )
        )
    }
}