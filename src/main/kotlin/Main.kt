package org.example

import dev.kord.core.Kord
import org.example.di.appModule
import org.koin.core.context.GlobalContext.startKoin

suspend fun main() {
    val config = BotConfig.load()
    val kord = Kord(config.token)

    val koin = startKoin {
        modules(appModule(config, kord))
    }.koin
    val serverManager = koin.get<EmbeddedServerManager>()
    val bot = koin.get<DiscordBot>()

    serverManager.start()
    bot.start()
}
