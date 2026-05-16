package org.example

import dev.kord.core.Kord
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.example.services.internalBotDeliveryRoute

class EmbeddedServerManager(
    private val config: BotConfig,
    private val kord: Kord,
    private val expectedToken: String,
) {
    fun start() {
        embeddedServer(
            Netty,
            port = config.embeddedServerPort,
            host = config.embeddedServerHost
        ) {
            install(ContentNegotiation) {
                json()
            }
            routing {
                internalBotDeliveryRoute(kord, expectedToken)
            }
        }.start(wait = false)
    }
}