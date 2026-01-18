package org.example.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.example.services.ai.AIProvider
import org.example.utils.MessageFormatter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SummarizeCommand : ChatInputCommand(), KoinComponent {
    override val name = "summarize"
    override val description = "Summarize the messages in a channel"
    private val aiProvider by inject<AIProvider>()

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val deferredMessage = event.interaction.deferPublicResponse()

        val channel = event.interaction.channel
        val messages = channel.getMessagesBefore(event.interaction.id, 100)
            .filter { it.author?.isBot == false && it.content.isNotBlank() }
            .take(10)
            .toList()
            .reversed()

        // Format messages for AI model
        val formattedMessages = MessageFormatter.formatMessagesForAI(messages)
        val summarizedMessageText = aiProvider.generateSummary(formattedMessages)

        deferredMessage.respond {
            content = "Summary: $summarizedMessageText"
        }
    }
}