package org.example.service

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.embed
import dev.kord.rest.request.KtorRequestException
import org.example.util.DiscordStrings


class RoastDeliveryService(private val kord: Kord) {
    suspend fun editRoastMessage(channelId: String, content: String) {
        try {
            val channelSnowflake = Snowflake(channelId)
            kord.rest.channel.createMessage(channelSnowflake) {
                this.content = ""
                embed {
                    title = DiscordStrings.Commands.RoastChannel.REPLIED_MESSAGE_TITLE
                    description = content
                    color = dev.kord.common.Color(0x5865F2)
                }
            }
        } catch (e: KtorRequestException) {
            when (e.status.code) {
                404 -> LoggingService.logError("Channel not found — may have been deleted. channelId=$channelId")
                403 -> LoggingService.logError("Bot lacks permission to edit message. channelId=$channelId")
                else -> LoggingService.logError("Discord API error ${e.status.code}: ${e.message}")
            }
        }
    }
}
