package org.example.util

import dev.kord.core.event.interaction.InteractionCreateEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logging {

    private val logger: Logger
        get() = LoggerFactory.getLogger(Thread.currentThread().stackTrace[3].className)

    fun logCommand(event: InteractionCreateEvent, commandName: String) {
        val userId = event.interaction.user.id.toString()
        val username = event.interaction.user.username
        val guildId = event.interaction.data.guildId.value?.toString() ?: "DM"

        logger.info("User {} (ID: {}) used '/{}' in guild {}", username, userId, commandName, guildId)
    }

    fun logInfo(message: String) {
        logger.info(message)
    }

    fun logError(message: String, error: Throwable? = null) {
        if (error != null) {
            logger.error(message, error)
        } else {
            logger.error(message)
        }
    }
}