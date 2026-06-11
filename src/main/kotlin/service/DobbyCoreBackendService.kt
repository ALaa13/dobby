package org.example.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.example.config.BotConfig
import org.example.dto.Fact
import org.example.dto.FactRequest
import org.example.dto.RoastRequest
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
        client.get(config.backendUrl + path) {
            contentType(ContentType.Application.Json)
            block()
        }

    private suspend fun post(path: String, block: HttpRequestBuilder.() -> Unit): HttpResponse =
        client.post(config.backendUrl + path) {
            contentType(ContentType.Application.Json)
            block()
        }

    private suspend fun delete(path: String, block: HttpRequestBuilder.() -> Unit): HttpResponse =
        client.delete(config.backendUrl + path) {
            contentType(ContentType.Application.Json)
            block()
        }

    private suspend inline fun <reified T> HttpResponse.bodyOrFallback(fallback: T, errorMessage: String): T =
        if (status.isSuccess()) body() else {
            Logging.logError("$errorMessage: ${status.value} ${status.description}")
            fallback
        }

    private suspend fun HttpResponse.isSuccessOrLog(errorMessage: String): Boolean {
        return if (status.isSuccess()) {
            true
        } else {
            val errorBody = runCatching { bodyAsText() }.getOrElse { "Could not read response body" }

            Logging.logError("$errorMessage: ${status.value} ${status.description} | Details: $errorBody")
            false
        }
    }

    suspend fun sendDiscordMessages(requestBody: RoastRequest): Boolean =
        safeRequest("Error sending roast delivery request to Dobby Core Backend", false) {
            post(DiscordStrings.HttpEndPoints.PostRoast.PATH) { setBody(requestBody) }
                .isSuccessOrLog("Error sending roast delivery request to Dobby Core Backend")
        }

    suspend fun sendFactRequest(factRequest: FactRequest): Boolean =
        safeRequest("Error sending remember request to Dobby Core Backend", false) {
            post(DiscordStrings.HttpEndPoints.PostGetDeleteFact.PATH) { setBody(factRequest) }
                .isSuccessOrLog("Error sending remember request to Dobby Core Backend")
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
            }.isSuccessOrLog("Error deleting facts for user $discordUserId from Dobby Core Backend")
        }
}