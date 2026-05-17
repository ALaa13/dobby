package org.example.config

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.example.route.internalBotDeliveryRoute
import org.example.service.RoastDeliveryService

class EmbeddedServerManager(
    private val config: BotConfig,
    private val roastDeliveryService: RoastDeliveryService,
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
                internalBotDeliveryRoute(config, roastDeliveryService)
            }
        }.start(wait = false)
    }
}