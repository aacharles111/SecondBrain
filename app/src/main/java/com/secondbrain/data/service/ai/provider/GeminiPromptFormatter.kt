package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.GeminiApiClient.GeminiContent
import com.secondbrain.data.service.ai.api.GeminiApiClient.GeminiPart

/**
 * Utility class for formatting prompts for the Gemini API
 */
object GeminiPromptFormatter {

    /**
     * Format a system prompt and user prompt for Gemini
     *
     * Gemini doesn't support a dedicated system prompt field, so we implement it as a
     * conversation with the system instructions as the first message, followed by a model
     * acknowledgment, and then the actual user query.
     *
     * @param systemPrompt The system instructions (can be null)
     * @param userPrompt The user's query
     * @return A list of GeminiContent objects representing the conversation
     */
    fun formatPrompt(systemPrompt: String?, userPrompt: String): List<GeminiContent> {
        return if (systemPrompt.isNullOrEmpty()) {
            // If no system prompt, just include the user message
            listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = userPrompt)
                    )
                )
            )
        } else {
            // Include system instructions as a conversation
            listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = "You are an AI assistant with the following instructions: $systemPrompt")
                    )
                ),
                GeminiContent(
                    role = "model",
                    parts = listOf(
                        GeminiPart(text = "I understand and will follow these instructions.")
                    )
                ),
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = userPrompt)
                    )
                )
            )
        }
    }
}
