package org.example.commands

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import kotlinx.coroutines.delay
import org.example.services.LoggingService

class SummarizeCommand(
    private val loggingService: LoggingService
) : ChatInputCommand {
    override val name = "summarize"
    override val description = "Summarize the messages in a channel"

    override suspend fun execute(event: GuildChatInputCommandInteractionCreateEvent) {
        // Respond to the interaction
        val deferredMessage = event.interaction.deferPublicResponse()
        delay(5000)
        deferredMessage.respond { content = "Summarizing..." }
    }
}