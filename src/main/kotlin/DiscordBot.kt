package org.example

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import org.example.command.ApplicationCommand
import org.example.command.ChatInputCommand
import org.example.config.BotConfig
import org.example.util.Logging

class DiscordBot(
    private val config: BotConfig,
    private val kord: Kord,
    private val applicationCommands: List<ApplicationCommand<*>>
) {
    suspend fun start() {
        registerSlashCommands()
        registerCommandHandlers()
        Logging.logInfo("Bot is starting...")
        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.Guilds
        }
    }

    private suspend fun registerSlashCommands() {
        if (config.devGuildId != null) {
            // Register commands to a specific guild (INSTANT - great for development!)
            kord.createGuildApplicationCommands(guildId = config.devGuildId) {
                applicationCommands.forEach { it.register(this) }
            }.collect {
                Logging.logInfo("Registered ${it.name} ${it.type} command to guild ${config.devGuildId}")
            }
        } else {
            // Register commands globally (takes up to 1 hour to propagate)
            kord.createGlobalApplicationCommands {
                applicationCommands.forEach { it.register(this) }
            }.collect {
                Logging.logInfo("Registered ${applicationCommands.size} slash commands globally")
            }
        }
    }

    private fun registerCommandHandlers() {
        val chatCommands = applicationCommands.filterIsInstance<ChatInputCommand>().associateBy { it.name }
        // Handle Chat Input Commands (slash commands)
        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            chatCommands[interaction.invokedCommandName]?.execute(this)
                ?: Logging.logError("Unknown chat command: ${interaction.invokedCommandName}")
        }
    }
}