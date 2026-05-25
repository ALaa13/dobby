package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoastDeliveryRequest(
    val channelId: String,
    val guildId: String,
    val messages: List<ChatMessage>,
    val persona: String?
)
