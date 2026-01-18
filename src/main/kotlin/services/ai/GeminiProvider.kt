package org.example.services.ai

import com.google.genai.Client
import com.google.genai.types.GenerateContentResponse
import org.example.services.LoggingService
import org.example.utils.ChatMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeminiProvider(
    private val apiKey: String,
) : AIProvider, KoinComponent {
    private val loggingService: LoggingService by inject()
    private val defaultPrompt = """
        Summarize the following Discord chat messages in a concise and informative way.
        Focus on the main topics discussed and key points mentioned.
    """.trimIndent()


    private val client = Client.builder()
        .apiKey(apiKey)
        .build()

    override suspend fun generateSummary(messages: List<ChatMessage>, customPrompt: String?): String {
        loggingService.logInfo("Generating summary with Gemini for ${messages.size} messages")

        val fullPrompt = buildFullPrompt(customPrompt, messages)

        return try {
            val response: GenerateContentResponse =
                client.models.generateContent(
                    "gemini-3-flash-preview",
                    fullPrompt,
                    null
                )
            response.text() ?: "No summary generated"
        } catch (e: Exception) {
            loggingService.logError("Failed to generate summary with Gemini", e)
            throw Exception("AI Summary failed: ${e.message}")
        }
    }

    private fun buildFullPrompt(customPrompt: String?, messages: List<ChatMessage>): String {
        val prompt = customPrompt ?: defaultPrompt
        val messagesText = messages.joinToString("\n") {
            "${it.author} (${it.timestamp}): ${it.content}"
        }
        return "$prompt\n\nMessages:\n$messagesText"
    }
}