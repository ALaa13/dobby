package org.example.route

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.config.BotConfig
import org.example.dto.RoastDeliveryResponse
import org.example.service.RoastDeliveryService
import org.example.util.DiscordStrings


fun Route.internalBotDeliveryRoute(
    config: BotConfig,
    roastDeliveryService: RoastDeliveryService
) {
    post(DiscordStrings.HttpEndPoints.InternalBotDelivery.PATH) {
        val incomingToken = call.request.headers[DiscordStrings.HttpEndPoints.InternalBotDelivery.HEADERS]
        if (incomingToken != config.securityToken) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid internal token.")
            return@post
        }

        val request = call.receive<RoastDeliveryResponse>()
        try {
            roastDeliveryService.editRoastMessage(request.channelId, request.content)
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to edit Discord message: ${e.message}")
        }
    }
}
