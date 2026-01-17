package org.example.services

class LoggingService {
    fun logCommand(
        userId: String,
        command: String,
        guildId: String?
    ) {
        val timestamp = java.time.LocalDateTime.now()
        println("[$timestamp] [CMD] User $userId used '/$command' in guild $guildId")
    }

    fun logInfo(message: String) {
        val timestamp = java.time.LocalDateTime.now()
        println("[$timestamp] [INFO] $message")
    }

    fun logError(message: String, error: Throwable? = null) {
        val timestamp = java.time.LocalDateTime.now()
        println("[$timestamp] [ERROR] $message")
        error?.printStackTrace()
    }
}