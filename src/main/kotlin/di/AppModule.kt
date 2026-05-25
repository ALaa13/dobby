package org.example.di

import dev.kord.core.Kord
import io.ktor.client.*
import org.example.DiscordBot
import org.example.command.ApplicationCommand
import org.example.command.FactCommand
import org.example.command.PingCommand
import org.example.command.RoastCommand
import org.example.config.BotConfig
import org.example.config.EmbeddedServerManager
import org.example.service.DobbyCoreBackend
import org.example.service.RoastDeliveryService
import org.koin.dsl.module

fun appModule(config: BotConfig, kord: Kord, httpClient: HttpClient) = module {
    single { config }
    single { kord }
    single { DiscordBot(get(), get(), get()) }
    single { httpClient }
    single {
        EmbeddedServerManager(
            get(),
            get(),
        )
    }
    single { RoastDeliveryService(get()) }
    single { DobbyCoreBackend(get(), get()) }
    single<List<ApplicationCommand<*>>> {
        listOf(
            PingCommand(),
            RoastCommand(get()),
            FactCommand(get())
        )
    }
}