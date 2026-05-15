package org.example.di

import dev.kord.core.Kord
import kotlinx.coroutines.runBlocking
import org.example.BotConfig
import org.example.commands.ApplicationCommand
import org.example.commands.PingCommand
import org.example.commands.SummarizeCommand
import org.example.services.DobbyCoreBackend
import org.example.services.LoggingService
import org.koin.dsl.module

val appModule = module(createdAtStart = true) {
    single { BotConfig.load() }
    single {
        runBlocking {
            Kord(get<BotConfig>().token)
        }
    }
    single { LoggingService() }
    single { DobbyCoreBackend() }
    single<List<ApplicationCommand<*>>> {
        listOf(
            PingCommand(),
            SummarizeCommand()
        )
    }
}