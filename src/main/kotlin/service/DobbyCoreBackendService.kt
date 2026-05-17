package org.example.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.example.config.BotConfig
import org.example.dto.RoastDeliveryRequest
import org.example.util.DiscordStrings

class DobbyCoreBackend(
    private val config: BotConfig,
) {
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000L
            connectTimeoutMillis = 10_000L
            socketTimeoutMillis = 120_000L
        }
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun sendDiscordMessages(requestBody: RoastDeliveryRequest): Boolean {
        return try {
            val urlString = config.dobbyBackendUrl + DiscordStrings.HttpEndPoints.PostRoast.PATH
            val response: HttpResponse = client.post(urlString) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            LoggingService.logError("Error sending roast delivery request to Dobby Core Backend: ${e.message}")
            false
        }
    }
}