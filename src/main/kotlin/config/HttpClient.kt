package org.example.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

object KtorHttpClient {
    val instance: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000L
            connectTimeoutMillis = 10_000L
            socketTimeoutMillis = 120_000L
        }
        install(ContentNegotiation) {
            json()
        }
    }
}