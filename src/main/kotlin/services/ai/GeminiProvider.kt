package org.example.services.ai

import com.google.genai.Client
import com.google.genai.types.GenerateContentResponse
import com.google.genai.types.HttpOptions
import com.google.genai.types.HttpRetryOptions
import org.example.services.LoggingService
import org.example.utils.ChatMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val MAX_CHARACTER = 4096

class GeminiProvider(
    private val apiKey: String,
) : AIProvider, KoinComponent {
    private val loggingService: LoggingService by inject()
    private val defaultPrompt = """
        Summarize the following Discord chat messages in a concise and informative way.
        Focus on the main topics discussed and key points mentioned.
        Keep the summary under $MAX_CHARACTER characters.
    """.trimIndent()


    private fun getClient(): Client {
        val retryOptions = HttpRetryOptions.builder()
            .attempts(3)
            .httpStatusCodes(408, 429)
            .build()
        val httpOptions = HttpOptions.builder()
            .retryOptions(retryOptions)
            .build()
        return Client.builder()
            .apiKey(apiKey)
            .httpOptions(httpOptions)
            .build()
    }

    override suspend fun generateSummary(messages: List<ChatMessage>, customPrompt: String?): String {
        loggingService.logInfo("Generating summary with Gemini for ${messages.size} messages")
        val fullPrompt = buildFullPrompt(customPrompt, messages)
        val client = getClient()

        return try {
            val response: GenerateContentResponse =
                client.models.generateContent(
                    "gemini-2.5-flash",
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