package org.example.command

import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import org.example.util.DiscordStrings

class PingCommand : ChatInputCommand() {

    override val name = DiscordStrings.Commands.Ping.NAME
    override val description = DiscordStrings.Commands.Ping.DESCRIPTION

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val timeBefore = System.currentTimeMillis()

        interaction.respondPublic {
            content = DiscordStrings.Commands.Ping.DEFERRED_MESSAGE
        }

        val timeAfter = System.currentTimeMillis()
        val ping = timeAfter - timeBefore

        interaction.getOriginalInteractionResponse().edit {
            content = "🏓 Pong! Latency: ${ping}ms"
        }
    }
}