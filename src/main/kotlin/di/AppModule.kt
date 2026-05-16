package org.example.di

import dev.kord.core.Kord
import org.example.BotConfig
import org.example.DiscordBot
import org.example.EmbeddedServerManager
import org.example.commands.ApplicationCommand
import org.example.commands.PingCommand
import org.example.commands.RoastCommand
import org.example.commands.UserRoastCommand
import org.example.services.DobbyCoreBackend
import org.example.services.LoggingService
import org.koin.dsl.module

fun appModule(config: BotConfig, kord: Kord) = module {
    single { config }
    single { kord }
    single { DiscordBot() }
    single {
        EmbeddedServerManager(
            get(),
            get(),
            get<BotConfig>().securityToken
        )
    }
    single { LoggingService() }
    single { DobbyCoreBackend(get()) }
    single<List<ApplicationCommand<*>>> {
        listOf(
            PingCommand(),
            RoastCommand(),
            UserRoastCommand()
        )
    }
}