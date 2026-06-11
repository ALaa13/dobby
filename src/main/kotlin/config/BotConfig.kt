package org.example.config

import dev.kord.common.entity.Snowflake
import io.github.cdimascio.dotenv.dotenv

data class BotConfig(
    val token: String,
    val devGuildId: Snowflake? = null,
    val redisHost: String,
    val redisPort: Int,
    val backendUrl: String,
    val backendApiHeader: String,
    val backendApiKey: String,
) {
    companion object {
        fun load(): BotConfig {
            val env = dotenv()
            return BotConfig(
                token = env["DISCORD_TOKEN"]
                    ?: error("DISCORD_TOKEN not set"),
                devGuildId = env["DEV_GUILD_ID"]?.let { Snowflake(it) },
                redisHost = env["REDIS_HOST"] ?: error("REDIS_HOST not set"),
                redisPort = env["REDIS_PORT"]?.toIntOrNull() ?: error("REDIS_PORT not set or not an integer"),
                backendUrl = env["BACKEND_URL"] ?: error("BACKEND_URL not set"),
                backendApiHeader = env["BACKEND_API_HEADER"] ?: "",
                backendApiKey = env["BACKEND_API_KEY"] ?: ""
            )
        }
    }
}