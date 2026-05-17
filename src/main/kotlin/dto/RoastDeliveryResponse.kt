package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoastDeliveryResponse(
    val channelId: String,
    val content: String
)
