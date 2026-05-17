package org.example.command

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import org.example.service.DobbyCoreBackend
import org.example.util.DiscordStrings
import org.example.util.FetchMessagesConfig
import org.example.util.deliverRoast
import org.example.util.fetchMessages


class UserRoastCommand(
    private val dobbyCoreBackend: DobbyCoreBackend
) : UserCommand() {

    override val name: String = DiscordStrings.Commands.RoastUser.NAME

    override suspend fun run(event: GuildUserCommandInteractionCreateEvent) {
        val interaction = event.interaction

        val deferredMessage = interaction.deferEphemeralResponse()

        // Get command User
        val targetUser = interaction.getTarget().asUser()
        if (targetUser.isBot) {
            deferredMessage.respond { content = DiscordStrings.Commands.RoastChannel.Target.IS_BOT_REPLY }
            return
        }

        val channel = interaction.channel
        val messages = fetchMessages(
            channel,
            interaction.id,
            FetchMessagesConfig(authorId = targetUser.id)
        )
        deliverRoast(
            messages = messages,
            channel = channel,
            deferredMessage = deferredMessage,
            dobbyCoreBackend = dobbyCoreBackend
        )
    }
}