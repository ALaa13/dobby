package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoastRequest(
    val channelId: String,
    val guildId: String,
    val messages: List<ChatMessage>,
    val persona: String?
)
