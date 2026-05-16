package org.example

import dev.kord.common.entity.Snowflake
import io.github.cdimascio.dotenv.dotenv


data class BotConfig(
    val token: String,
    val devGuildId: Snowflake? = null,
    val securityToken: String,
    val dobbyBackendUrl: String,
    val embeddedServerPort: Int,
    val embeddedServerHost: String,
) {
    companion object {
        fun load(): BotConfig {
            val env = dotenv()
            return BotConfig(
                token = env["DISCORD_TOKEN"]
                    ?: error("DISCORD_TOKEN not set"),
                devGuildId = env["DEV_GUILD_ID"]?.let { Snowflake(it) },
                securityToken = env["INTERNAL_SECURITY_TOKEN"]
                    ?: error("INTERNAL_SECURITY_TOKEN not set"),
                dobbyBackendUrl = env["DOBBY_BACKEND_URL"] ?: error("DOBBY_BACKEND_URL not set"),
                embeddedServerPort = env["EMBEDDED_SERVER_PORT"]?.toIntOrNull() ?: 8080,
                embeddedServerHost = env["EMBEDDED_SERVER_HOST"] ?: "0.0.0.0"
            )
        }
    }
}
