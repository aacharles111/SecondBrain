package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.DeepSeekApiClient.DeepSeekMessage

/**
 * Utility class for formatting prompts for the DeepSeek API
 */
object DeepSeekPromptFormatter {

    /**
     * Format a system prompt and user prompt for DeepSeek
     *
     * DeepSeek uses a similar format to OpenAI with a dedicated system message
     * with role="system" followed by the user's message with role="user".
     *
     * @param systemPrompt The system instructions (can be null)
     * @param userPrompt The user's query
     * @return A list of DeepSeekMessage objects representing the conversation
     */
    fun formatPrompt(systemPrompt: String?, userPrompt: String): List<DeepSeekMessage> {
        return if (systemPrompt.isNullOrEmpty()) {
            // If no system prompt, just include the user message
            listOf(
                DeepSeekMessage(role = "user", content = userPrompt)
            )
        } else {
            // Include both system and user messages
            listOf(
                DeepSeekMessage(role = "system", content = systemPrompt),
                DeepSeekMessage(role = "user", content = userPrompt)
            )
        }
    }
}
