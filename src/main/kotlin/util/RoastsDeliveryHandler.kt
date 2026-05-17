package org.example.util

import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import org.example.dto.RoastDeliveryRequest
import org.example.service.DobbyCoreBackend


suspend fun deliverRoast(
    messages: List<Message>,
    channel: ChannelBehavior,
    deferredMessage: DeferredEphemeralMessageInteractionResponseBehavior,
    dobbyCoreBackend: DobbyCoreBackend
) {
    val formattedMessages = formatMessagesForAI(messages)
    val responseMessage = if (dobbyCoreBackend.sendDiscordMessages(
            RoastDeliveryRequest(
                channelId = channel.id.toString(),
                messages = formattedMessages
            )
        )
    ) {
        DiscordStrings.Commands.RoastChannel.DEFERRED_MESSAGE
    } else {
        DiscordStrings.HttpEndPoints.PostRoast.FAILED_MESSAGE
    }
    deferredMessage.respond { content = responseMessage }
}