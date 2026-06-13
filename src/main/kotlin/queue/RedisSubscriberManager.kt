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
            Jedis(config.redisHost, config.redisPort).use { healthCheckClient ->
                if (healthCheckClient.ping() != "PONG") {
                    throw IllegalStateException("Redis responded, but ping failed!")
                }
            }
            Logging.logInfo("Redis Subscriber health check passed: Connected successfully.")
        } catch (e: Exception) {
            Logging.logError("CRITICAL: Redis container is offline! Subscriber cannot start: ${e.message}")
            exitProcess(1)
        }

        scope.launch(Dispatchers.IO) {
            val shouldReconnect = true
            val delayDuration = 5000L // 5-second recovery buffer

            while (shouldReconnect && isActive) { // isActive ensures it stops if the scope is canceled
                try {
                    jedis = Jedis(config.redisHost, config.redisPort)
                    Logging.logInfo("Connected to Redis! Subscribing to channel...")

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

                    // ⚠️ This blocks the while-loop here as long as the connection stays alive
                    jedis?.subscribe(pubSub, DiscordStrings.RedisChannels.ROAST_DELIVERY)

                } catch (e: Exception) {
                    Logging.logError("Redis connection lost or failed to subscribe: ${e.message}. Retrying in ${delayDuration / 1000}s...")
                    runCatching { jedis?.close() }
                    // Non-blocking coroutine delay before attempting a clean reconnect
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