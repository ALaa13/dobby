package org.example.commands

import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import org.example.utils.DiscordStrings
import org.koin.core.component.KoinComponent

class PingCommand : ChatInputCommand(), KoinComponent {

    override val name = DiscordStrings.Commands.Ping.NAME
    override val description = DiscordStrings.Commands.Ping.DESCRIPTION

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val timeBefore = System.currentTimeMillis()

        event.interaction.respondPublic {
            content = DiscordStrings.Commands.Ping.DEFERRED_MESSAGE
        }

        val timeAfter = System.currentTimeMillis()
        val ping = timeAfter - timeBefore

        event.interaction.getOriginalInteractionResponse().edit {
            content = "🏓 Pong! Latency: ${ping}ms"
        }
    }
}