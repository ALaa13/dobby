package org.example.commands

import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildMessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.message
import dev.kord.rest.builder.interaction.user
import org.example.services.LoggingService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Base interface for all command types
abstract class ApplicationCommand<T : InteractionCreateEvent> : KoinComponent {
    abstract val name: String
    val loggingService: LoggingService by inject()
    abstract suspend fun register(builder: MultiApplicationCommandBuilder)
    abstract suspend fun run(event: T)
    suspend fun execute(event: T) {
        try {
            run(event)
            loggingService.logCommand(
                userId = event.interaction.user.id.toString(),
                command = name,
                guildId = event.interaction.data.guildId.value?.toString()
            )
        } catch (e: Exception) {
            loggingService.logError("Error executing $name", e)
            throw e
        }
    }
}

// Chat Input Commands (Slash commands like /ping)
abstract class ChatInputCommand : ApplicationCommand<GuildChatInputCommandInteractionCreateEvent>() {
    abstract val description: String

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            // Subclasses can override to add options
        }
    }

    abstract override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent)
}

// User Commands (Right-click on user)
abstract class UserCommand : ApplicationCommand<GuildUserCommandInteractionCreateEvent>() {
    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.user(name)
    }

    abstract override suspend fun run(event: GuildUserCommandInteractionCreateEvent)
}

// Message Commands (Right-click on a message)
abstract class MessageCommand : ApplicationCommand<GuildMessageCommandInteractionCreateEvent>() {
    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.message(name)
    }

    abstract override suspend fun run(event: GuildMessageCommandInteractionCreateEvent)
}