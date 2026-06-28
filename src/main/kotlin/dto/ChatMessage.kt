package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val displayName: String,
    val discordUserId: String,
    val avatarHash: String?,
    val content: String,
    val timestamp: String
)