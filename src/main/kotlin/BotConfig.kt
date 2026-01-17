package org.example

import dev.kord.common.entity.Snowflake
import io.github.cdimascio.dotenv.dotenv


data class BotConfig(
    val token: String,
    val ownerId: String? = null,
    val devGuildId: Snowflake? = null
) {
    companion object {
        fun load(): BotConfig {
            val env = dotenv()
            return BotConfig(
                token = env["DISCORD_TOKEN"]
                    ?: error("DISCORD_TOKEN not set"),
                ownerId = env["OWNER_ID"],
                devGuildId = env["DEV_GUILD_ID"]?.let { Snowflake(it) }
            )
        }
    }
}