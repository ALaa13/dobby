package org.example.commands

import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildMessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.message
import dev.kord.rest.builder.interaction.user

// Base interface for all command types
interface ApplicationCommand {
    val name: String
    suspend fun register(builder: MultiApplicationCommandBuilder)
}

// Chat Input Commands (Slash commands like /ping)
interface ChatInputCommand : ApplicationCommand {
    val description: String

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            // Subclasses can override to add options
        }
    }

    suspend fun execute(event: GuildChatInputCommandInteractionCreateEvent)
}

// User Commands (Right-click on user)
interface UserCommand : ApplicationCommand {
    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.user(name)
    }

    suspend fun execute(event: GuildUserCommandInteractionCreateEvent)
}

// Message Commands (Right-click on a message)
interface MessageCommand : ApplicationCommand {
    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.message(name)
    }

    suspend fun execute(event: GuildMessageCommandInteractionCreateEvent)
}