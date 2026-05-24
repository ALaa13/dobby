package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RememberFactRequest(
    val fact: String,
    val discordUserId: String,
    val guildId: String,
    val displayName: String?
)