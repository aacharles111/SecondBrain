package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.OpenAiApiClient.Message

/**
 * Utility class for formatting prompts for the OpenAI API
 */
object OpenAiPromptFormatter {

    /**
     * Format a system prompt and user prompt for OpenAI
     *
     * OpenAI supports a dedicated system message with role="system" that sets the behavior
     * of the assistant. This is followed by the user's message with role="user".
     *
     * @param systemPrompt The system instructions (can be null)
     * @param userPrompt The user's query
     * @return A list of Message objects representing the conversation
     */
    fun formatPrompt(systemPrompt: String?, userPrompt: String): List<Message> {
        return if (systemPrompt.isNullOrEmpty()) {
            // If no system prompt, just include the user message
            listOf(
                Message(role = "user", content = userPrompt)
            )
        } else {
            // Include both system and user messages
            listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = userPrompt)
            )
        }
    }
}
