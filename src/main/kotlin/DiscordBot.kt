package org.example

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildApplicationCommandInteractionCreateEvent
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DiscordBot : KoinComponent {
    private val config: BotConfig by inject()
    private val commands: List<ApplicationCommand> by inject()

    suspend fun start() {
        val kord = Kord(config.token)

        // Register slash commands with Discord
        registerSlashCommands(kord)

        // Listen for slash command interactions
        registerCommandHandlers(kord)

        println("Bot is starting...")
        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.Guilds
        }
    }

    private suspend fun registerSlashCommands(kord: Kord) {
        if (config.devGuildId != null) {
            val myCommands = commands
            // Register commands to a specific guild (INSTANT - great for development!)
            kord.createGuildApplicationCommands(guildId = config.devGuildId!!) {
                myCommands.forEach { it.register(this) }
            }.collect { registerSlashCommands ->
                println(registerSlashCommands)
            }
            println("Registered ${commands.size} slash commands to dev guild ${config.devGuildId} (instant)")
        } else {
            // Register commands globally (takes up to 1 hour to propagate)
            val myCommands = commands
            kord.createGlobalApplicationCommands {
                myCommands.forEach { it.register(this) }
            }.collect { registerSlashCommands ->
                println(registerSlashCommands)
            }
            println("Registered ${commands.size} slash commands globally (may take up to 1 hour)")
        }
    }

    private fun registerCommandHandlers(kord: Kord) {
        // Handle Chat Input Commands (slash commands)
        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            handleCommand<ChatInputCommand> { it.execute(this) }
        }

        // Handle User Commands (right-click on user)
        kord.on<GuildUserCommandInteractionCreateEvent> {
            handleCommand<UserCommand> { it.execute(this) }
        }

        // Handle Message Commands (right-click on a message)
        kord.on<GuildMessageCommandInteractionCreateEvent> {
            handleCommand<MessageCommand> { it.execute(this) }
        }
    }

    // Complex Function
    private suspend inline fun <reified T : ApplicationCommand> GuildApplicationCommandInteractionCreateEvent.handleCommand(
        crossinline executor: suspend (T) -> Unit
    ) {
        val commandName = interaction.invokedCommandName

        commands.filterIsInstance<T>()
            .find { it.name == commandName }
            ?.let { executor(it) }
    }
}