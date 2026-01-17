package org.example.commands

import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import org.example.services.LoggingService

class PingCommand(
    private val loggingService: LoggingService
) : ChatInputCommand {

    override val name = "ping"
    override val description = "Check bot latency"

    override suspend fun execute(event: GuildChatInputCommandInteractionCreateEvent) {
        val timeBefore = System.currentTimeMillis()

        // Respond to the interaction
        event.interaction.respondPublic {
            content = "Calculating ping..."
        }

        val timeAfter = System.currentTimeMillis()
        val ping = timeAfter - timeBefore

        // Edit the response with the actual ping
        event.interaction.getOriginalInteractionResponse().edit {
            content = "🏓 Pong! Latency: ${ping}ms"
        }

        // Log the command usage
        loggingService.logCommand(
            userId = event.interaction.user.id.toString(),
            command = name,
            guildId = event.interaction.data.guildId.value?.toString()
        )
    }
}