package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoastProcessedDeliveryRequest(
    val channelId: String,
    val content: String,
    val success: Boolean
)
