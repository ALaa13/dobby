package org.example.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.embed
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.dto.RoastDeliveryResponse
import org.example.utils.DiscordStrings


fun Route.internalBotDeliveryRoute(kord: Kord, expectedToken: String) {
    post(DiscordStrings.HttpEndPoints.InternalBotDelivery.PATH) {
        val incomingToken = call.request.headers[DiscordStrings.HttpEndPoints.InternalBotDelivery.HEADERS]
        if (incomingToken != expectedToken) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid internal token.")
            return@post
        }

        val request = call.receive<RoastDeliveryResponse>()
        try {
            val channelSnowflake = Snowflake(request.channelId)
            val messageSnowflake = Snowflake(request.messageId)
            kord.rest.channel.editMessage(channelSnowflake, messageSnowflake) {
                content = ""
                embed {
                    title = DiscordStrings.Commands.Roast.REPLIED_MESSAGE_TITLE
                    description = request.content
                    color = dev.kord.common.Color(0x5865F2)
                }
            }
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to edit Discord message: ${e.message}")
        }
    }
}
