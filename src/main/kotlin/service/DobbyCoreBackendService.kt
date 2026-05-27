package org.example.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.example.config.BotConfig
import org.example.dto.Fact
import org.example.dto.FactRequest
import org.example.dto.RoastDeliveryRequest
import org.example.util.DiscordStrings
import org.example.util.Logging

class DobbyCoreBackend(
    private val config: BotConfig,
    private val client: HttpClient
) {
    private suspend fun <T> safeRequest(
        errorMessage: String,
        fallback: T,
        block: suspend () -> T
    ): T = try {
        block()
    } catch (e: Exception) {
        Logging.logError("$errorMessage: ${e.message}")
        fallback
    }

    private suspend fun get(path: String, block: HttpRequestBuilder.() -> Unit): HttpResponse =
        client.get(config.dobbyBackendUrl + path) {
            contentType(ContentType.Application.Json)
            block()
        }

    private suspend fun post(path: String, block: HttpRequestBuilder.() -> Unit): HttpResponse =
        client.post(config.dobbyBackendUrl + path) {
            contentType(ContentType.Application.Json)
            block()
        }

    private suspend fun delete(path: String, block: HttpRequestBuilder.() -> Unit): HttpResponse =
        client.delete(config.dobbyBackendUrl + path) {
            contentType(ContentType.Application.Json)
            block()
        }

    private suspend inline fun <reified T> HttpResponse.bodyOrFallback(fallback: T, errorMessage: String): T =
        if (status.isSuccess()) body() else {
            Logging.logError(errorMessage)
            fallback
        }

    suspend fun sendDiscordMessages(requestBody: RoastDeliveryRequest): Boolean =
        safeRequest("Error sending roast delivery request to Dobby Core Backend", false) {
            post(DiscordStrings.HttpEndPoints.PostRoast.PATH) { setBody(requestBody) }.status.isSuccess()
        }

    suspend fun sendFactRequest(factRequest: FactRequest): Boolean =
        safeRequest("Error sending remember request to Dobby Core Backend", false) {
            post(DiscordStrings.HttpEndPoints.PostGetDeleteFact.PATH) { setBody(factRequest) }.status.isSuccess()
        }

    suspend fun getFactsForUser(discordUserId: String, guildId: String): List<Fact> =
        safeRequest("Error getting facts for user $discordUserId from Dobby Core Backend", emptyList()) {
            get(DiscordStrings.HttpEndPoints.PostGetDeleteFact.PATH) {
                parameter("discord_user_id", discordUserId)
                parameter("guild_id", guildId)
            }.bodyOrFallback(emptyList(), "Failed to get facts for user $discordUserId")
        }

    suspend fun deleteFactsForUser(discordUserId: String, guildId: String): Boolean =
        safeRequest("Error deleting facts for user $discordUserId from Dobby Core Backend", false) {
            delete(DiscordStrings.HttpEndPoints.PostGetDeleteFact.PATH) {
                parameter("discord_user_id", discordUserId)
                parameter("guild_id", guildId)
            }.status.isSuccess()
        }
}