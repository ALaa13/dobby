package org.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import dev.kord.core.Kord
import org.example.services.internalBotDeliveryRoute

class EmbeddedServerManager(
    private val kord: Kord,
    private val expectedToken: String
) {
    fun start() {
        embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json()
            }
            routing {
                internalBotDeliveryRoute(kord, expectedToken)
            }
        }.start(wait = false)
    }
}