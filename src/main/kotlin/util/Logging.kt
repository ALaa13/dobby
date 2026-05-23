package org.example.util

import java.time.LocalDateTime

object Logging {
    fun logCommand(
        userId: String,
        command: String,
        guildId: String?
    ) {
        val timestamp = LocalDateTime.now()
        println("[$timestamp] [CMD] User $userId used '/$command' in guild $guildId")
    }

    fun logInfo(message: String) {
        val timestamp = LocalDateTime.now()
        println("[$timestamp] [INFO] $message")
    }

    fun logError(message: String, error: Throwable? = null) {
        val timestamp = LocalDateTime.now()
        println("[$timestamp] [ERROR] $message")
        error?.printStackTrace()
    }
}