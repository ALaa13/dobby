package org.example.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent

class SummarizeCommand : ChatInputCommand(), KoinComponent {
    override val name = "summarize"
    override val description = "Summarize the messages in a channel"

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val deferredMessage = event.interaction.deferPublicResponse()
        delay(6000)

        // Access user data from the event
        val userId = event.interaction.user.id.toString()
        val userName = event.interaction.user.username
        val guildId = event.interaction.data.guildId.value?.toString()


        deferredMessage.respond {
            content = "Summarized for $userName (ID: $userId) in guild $guildId"
        }
    }
}