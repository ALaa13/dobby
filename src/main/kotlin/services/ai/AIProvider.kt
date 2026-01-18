package org.example.services.ai

import org.example.utils.ChatMessage

interface AIProvider {
    suspend fun generateSummary(messages: List<ChatMessage>, customPrompt: String? = null): String
}