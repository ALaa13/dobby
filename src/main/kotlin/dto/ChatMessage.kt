package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val author: String,
    val content: String,
    val timestamp: String
)