package org.example.di

import dev.kord.core.Kord
import org.example.DiscordBot
import org.example.command.ApplicationCommand
import org.example.command.PingCommand
import org.example.command.RoastCommand
import org.example.command.UserRoastCommand
import org.example.config.BotConfig
import org.example.config.EmbeddedServerManager
import org.example.service.DobbyCoreBackend
import org.example.service.RoastDeliveryService
import org.koin.dsl.module

fun appModule(config: BotConfig, kord: Kord) = module {
    single { config }
    single { kord }
    single { DiscordBot(get(), get(), get()) }
    single {
        EmbeddedServerManager(
            get(),
            get(),
        )
    }
    single { RoastDeliveryService(get()) }
    single { DobbyCoreBackend(get()) }
    single<List<ApplicationCommand<*>>> {
        listOf(
            PingCommand(),
            RoastCommand(get()),
            UserRoastCommand(get())
        )
    }
}