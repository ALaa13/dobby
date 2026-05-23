package org.example.service

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.embed
import dev.kord.rest.request.KtorRequestException
import org.example.dto.RoastProcessedDeliveryRequest
import org.example.util.DiscordStrings
import org.example.util.Logging


class RoastDeliveryService(private val kord: Kord) {
    suspend fun editRoastMessage(roastProcessedDeliveryRequest: RoastProcessedDeliveryRequest) {
        try {
            val channelSnowflake = Snowflake(roastProcessedDeliveryRequest.channelId)
            var embedTitle: String
            var embedDescription: String

            when (roastProcessedDeliveryRequest.success) {
                true -> {
                    embedTitle = DiscordStrings.Commands.Roast.SUCCESS_REPLIED_MESSAGE_TITLE
                    embedDescription = roastProcessedDeliveryRequest.content
                }

                false -> {
                    embedTitle = DiscordStrings.Commands.Roast.FAILURE_REPLIED_MESSAGE_TITLE
                    embedDescription = DiscordStrings.HttpEndPoints.PostRoast.FAILED_MESSAGE
                }
            }
            kord.rest.channel.createMessage(channelSnowflake) {
                embed {
                    title = embedTitle
                    description = embedDescription
                    color = dev.kord.common.Color(0x5865F2)
                }
            }
        } catch (e: KtorRequestException) {
            when (e.status.code) {
                404 -> Logging.logError("Channel not found — may have been deleted. channelId=$roastProcessedDeliveryRequest.channelId")
                403 -> Logging.logError("Bot lacks permission to edit message. channelId=$roastProcessedDeliveryRequest.channelId")
                else -> Logging.logError("Discord API error ${e.status.code}: ${e.message}")
            }
        }
    }
}
