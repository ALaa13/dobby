package org.example

import dev.kord.core.Kord
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.example.config.BotConfig
import org.example.config.KtorHttpClient
import org.example.di.appModule
import org.example.queue.RedisSubscriberManager
import org.example.util.Logging
import org.koin.core.context.GlobalContext.startKoin

suspend fun main() {
    val config = BotConfig.load()
    val kord = Kord(config.token)
    val httpClient = KtorHttpClient.create(config)

    val koin = startKoin {
        modules(appModule(config, kord, httpClient))
    }.koin

    Runtime.getRuntime().addShutdownHook(Thread {
        Logging.logInfo("Shutting down... releasing Redis connections.")
        koin.get<RedisSubscriberManager>().stop()
    })


    coroutineScope {
        launch { koin.get<RedisSubscriberManager>().start() }
        launch { koin.get<DiscordBot>().start() }
    }
}
