package org.example.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.example.config.BotConfig
import org.example.dto.RoastDeliveryRequest
import org.example.util.DiscordStrings

class DobbyCoreBackend(
    private val config: BotConfig,
    private val client: HttpClient
) {
    suspend fun sendDiscordMessages(requestBody: RoastDeliveryRequest): Boolean {
        return try {
            val urlString = config.dobbyBackendUrl + DiscordStrings.HttpEndPoints.PostRoast.PATH
            val response: HttpResponse = client.post(urlString) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            Logging.logError("Error sending roast delivery request to Dobby Core Backend: ${e.message}")
            false
        }
    }
}