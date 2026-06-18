package org.example.queue

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.example.config.BotConfig
import org.example.dto.RoastResult
import org.example.service.RoastDeliveryService
import org.example.util.DiscordStrings
import org.example.util.Logging
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds


class RedisSubscriberManager(
    private val config: BotConfig,
    private val roastDeliveryService: RoastDeliveryService
) {
    private var jedis: Jedis? = null
    private var pubSub: JedisPubSub? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        try {
            // Add authentication to the initial health check client
            Jedis(config.redisHost, config.redisPort).use { healthCheckClient ->
                if (!config.redisPassword.isNullOrBlank()) {
                    healthCheckClient.auth(config.redisPassword)
                }
                if (healthCheckClient.ping() != "PONG") {
                    throw IllegalStateException("Redis responded, but ping failed!")
                }
            }
            Logging.logInfo("Redis Subscriber health check passed: Connected and authenticated successfully.")
        } catch (e: Exception) {
            Logging.logError("CRITICAL: Redis container is offline or auth failed! Subscriber cannot start: ${e.message}")
            exitProcess(1)
        }

        scope.launch(Dispatchers.IO) {
            val shouldReconnect = true
            val delayDuration = 5000L

            while (shouldReconnect && isActive) {
                try {
                    // Initialize and authenticate the main subscription worker
                    jedis = Jedis(config.redisHost, config.redisPort)
                    if (!config.redisPassword.isNullOrBlank()) {
                        jedis?.auth(config.redisPassword)
                    }

                    Logging.logInfo("Connected and authenticated to Redis! Subscribing to channel...")

                    pubSub = object : JedisPubSub() {
                        override fun onMessage(channel: String, message: String) {
                            scope.launch {
                                try {
                                    val requestBody = json.decodeFromString<RoastResult>(message)
                                    roastDeliveryService.editRoastMessage(requestBody)
                                } catch (e: Exception) {
                                    Logging.logError("Error parsing Redis payload: ${e.message}")
                                }
                            }
                        }

                        override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
                            Logging.logInfo("Unsubscribed from channel: $channel")
                        }
                    }

                    // This blocks the loop while connected
                    jedis?.subscribe(pubSub, DiscordStrings.RedisChannels.ROAST_DELIVERY)

                } catch (e: Exception) {
                    Logging.logError("Redis connection lost or failed to subscribe: ${e.message}. Retrying in ${delayDuration / 1000}s...")
                    runCatching { jedis?.close() }
                    delay(delayDuration.milliseconds)
                }
            }
        }
    }

    fun stop() {
        pubSub?.unsubscribe()
        jedis?.close()
        scope.cancel()
        Logging.logInfo("Redis subscriber gracefully stopped.")
    }
}