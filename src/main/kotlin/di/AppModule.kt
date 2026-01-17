package org.example.di

import org.example.BotConfig
import org.example.commands.ApplicationCommand
import org.example.commands.PingCommand
import org.example.commands.SummarizeCommand
import org.example.services.LoggingService
import org.koin.dsl.module

val appModule = module {
    // Config
    single { BotConfig.load() }

    // Services
    single { LoggingService() }

    // Commands - register all your commands here
    single<List<ApplicationCommand>> {
        listOf(
            PingCommand(get()),
            SummarizeCommand(get())
        )
    }
}