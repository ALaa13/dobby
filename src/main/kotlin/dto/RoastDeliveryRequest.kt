package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoastDeliveryRequest(
    val channelId: String,
    val messageId: String,
    val messages: List<ChatMessage>
)
