package org.example

import org.example.di.appModule
import org.koin.core.context.GlobalContext.startKoin

suspend fun main() {
    startKoin {
        modules(appModule)
    }
    DiscordBot().start()
}
