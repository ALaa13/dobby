package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoastResult(
    val channelId: String,
    val content: String,
    val success: Boolean
)
