package org.example

import dev.kord.common.entity.Snowflake
import io.github.cdimascio.dotenv.dotenv


data class BotConfig(
    val token: String,
    val devGuildId: Snowflake? = null,
    val aiProvider: AIProviderType = AIProviderType.GEMINI,
    val geminiApiKey: String? = null,
) {
    companion object {
        fun load(): BotConfig {
            val env = dotenv()
            return BotConfig(
                token = env["DISCORD_TOKEN"]
                    ?: error("DISCORD_TOKEN not set"),
                devGuildId = env["DEV_GUILD_ID"]?.let { Snowflake(it) },
                aiProvider = env["AI_PROVIDER"]?.let {
                    AIProviderType.valueOf(it.uppercase())
                } ?: AIProviderType.GEMINI,
                geminiApiKey = env["GEMINI_API_KEY"],
            )
        }
    }
}

enum class AIProviderType {
    GEMINI,
    OPENAI,
    CLAUDE
}