package org.example.services

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import org.example.dto.RoastDeliveryRequest

private const val BACKEND_URL = "http://localhost:8080/api/v1/roasts"


class DobbyCoreBackend {
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

    suspend fun sendDiscordMessages(requestBody: RoastDeliveryRequest): String {
        val response: HttpResponse = client.post(BACKEND_URL) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        val responseBody = response.bodyAsText()
        return responseBody
    }
}