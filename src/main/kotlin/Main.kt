package org.example

import dev.kord.core.Kord
import org.example.config.BotConfig
import org.example.config.EmbeddedServerManager
import org.example.di.appModule
import org.koin.core.context.GlobalContext.startKoin

suspend fun main() {
    val config = BotConfig.load()
    val kord = Kord(config.token)

    val koin = startKoin {
        modules(appModule(config, kord))
    }.koin

    koin.get<EmbeddedServerManager>().start()
    koin.get<DiscordBot>().start()
}
