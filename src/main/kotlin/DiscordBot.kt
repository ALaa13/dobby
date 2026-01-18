package org.example

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildMessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import org.example.commands.ApplicationCommand
import org.example.commands.ChatInputCommand
import org.example.commands.MessageCommand
import org.example.commands.UserCommand
import org.example.services.LoggingService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DiscordBot : KoinComponent {
    private val config: BotConfig by inject()
    private val kord: Kord by inject()
    private val loggingService: LoggingService by inject()
    private val applicationCommands: List<ApplicationCommand<*>> by inject()


    suspend fun start() {
        // Register slash commands with Discord
        registerSlashCommands()
        // Listen for slash command interactions
        registerCommandHandlers()

        loggingService.logInfo("Bot is starting...")
        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.Guilds
        }
    }

    private suspend fun registerSlashCommands() {
        if (config.devGuildId != null) {
            // Register commands to a specific guild (INSTANT - great for development!)
            kord.createGuildApplicationCommands(guildId = config.devGuildId!!) {
                applicationCommands.forEach { it.register(this) }
            }.collect {
                loggingService.logInfo("Registered ${applicationCommands.size} slash commands to dev guild ${config.devGuildId} (instant)")
            }
        } else {
            // Register commands globally (takes up to 1 hour to propagate)
            kord.createGlobalApplicationCommands {
                applicationCommands.forEach { it.register(this) }
            }.collect {
                loggingService.logInfo("Registered ${applicationCommands.size} slash commands globally (may take up to 1 hour)")
            }
        }
    }

    private fun registerCommandHandlers() {
        // Build command maps once during initialization
        val chatCommands = applicationCommands.filterIsInstance<ChatInputCommand>().associateBy { it.name }
        val userCommands = applicationCommands.filterIsInstance<UserCommand>().associateBy { it.name }
        val messageCommands = applicationCommands.filterIsInstance<MessageCommand>().associateBy { it.name }

        // Handle Chat Input Commands (slash commands)
        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            chatCommands[interaction.invokedCommandName]?.execute(this)
                ?: loggingService.logError("Unknown chat command: ${interaction.invokedCommandName}")
        }

        // Handle User Commands (right-click on user)
        kord.on<GuildUserCommandInteractionCreateEvent> {
            userCommands[interaction.invokedCommandName]?.execute(this)
                ?: loggingService.logError("Unknown user command: ${interaction.invokedCommandName}")
        }

        // Handle Message Commands (right-click on a message)
        kord.on<GuildMessageCommandInteractionCreateEvent> {
            messageCommands[interaction.invokedCommandName]?.execute(this)
                ?: loggingService.logError("Unknown message command: ${interaction.invokedCommandName}")
        }
    }
}