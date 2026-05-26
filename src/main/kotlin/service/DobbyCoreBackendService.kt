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

    suspend fun sendFactRequest(factRequest: FactRequest): Boolean {
        return try {
            val urlString = config.dobbyBackendUrl + DiscordStrings.HttpEndPoints.PostGetDeleteFact.PATH
            val response: HttpResponse = client.post(urlString) {
                contentType(ContentType.Application.Json)
                setBody(factRequest)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            Logging.logError("Error sending remember request to Dobby Core Backend: ${e.message}")
            false
        }
    }

    suspend fun getFactsForUser(discordUserId: String, guildId: String): List<Fact> {
        return try {
            val urlString = config.dobbyBackendUrl + DiscordStrings.HttpEndPoints.PostGetDeleteFact.PATH
            val response: HttpResponse = client.get(urlString) {
                contentType(ContentType.Application.Json)
                parameter("discord_user_id", discordUserId)
                parameter("guild_id", guildId)
            }
            if (response.status.isSuccess()) {
                response.body<List<Fact>>()
            } else {
                Logging.logError("Failed to get facts for user $discordUserId: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            Logging.logError("Error getting facts for user $discordUserId from Dobby Core Backend: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteFactsForUser(discordUserId: String, guildId: String): Boolean {
        return try {
            val urlString = config.dobbyBackendUrl + DiscordStrings.HttpEndPoints.PostGetDeleteFact.PATH
            val response: HttpResponse = client.delete(urlString) {
                contentType(ContentType.Application.Json)
                parameter("discord_user_id", discordUserId)
                parameter("guild_id", guildId)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            Logging.logError("Error deleting facts for user $discordUserId from Dobby Core Backend: ${e.message}")
            false
        }
    }
}