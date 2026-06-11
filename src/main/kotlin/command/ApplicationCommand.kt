package org.example.command

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import org.example.util.DiscordStrings
import org.example.util.Logging

abstract class ApplicationCommand<T : InteractionCreateEvent> {
    abstract val name: String
    abstract suspend fun register(builder: MultiApplicationCommandBuilder)
    abstract suspend fun run(event: T)
    suspend fun execute(event: T) {
        try {
            Logging.logCommand(event, name)
            run(event)
        } catch (e: Exception) {
            Logging.logError("Error executing $name", e)
            throw e
        }
    }
}

abstract class ChatInputCommand() :
    ApplicationCommand<GuildChatInputCommandInteractionCreateEvent>() {
    abstract val description: String

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {}
    }

    abstract override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent)

    protected suspend fun respondEphemeral(
        event: GuildChatInputCommandInteractionCreateEvent,
        botGuardTarget: User? = null,
        errorLogMessage: String = "Error executing $name",
        responseContent: suspend InteractionResponseModifyBuilder.() -> Unit
    ) {
        val deferredMessage = event.interaction.deferEphemeralResponse()

        if (botGuardTarget?.isBot == true) {
            deferredMessage.respond { content = DiscordStrings.Commands.IS_BOT_REPLY }
            return
        }

        runCatching {
            val builder = InteractionResponseModifyBuilder()
            builder.responseContent()
            deferredMessage.respond {
                builder.content?.let { this.content = it }
                if (!builder.embeds.isNullOrEmpty()) {
                    this.embeds = builder.embeds
                }
            }
        }.onFailure { error ->
            Logging.logError(errorLogMessage, error)
            deferredMessage.respond { content = DiscordStrings.Commands.DISCORD_INTERACTION_FAILED }
        }
    }
}