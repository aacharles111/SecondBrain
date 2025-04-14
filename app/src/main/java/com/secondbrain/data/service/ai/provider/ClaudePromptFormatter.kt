package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.ClaudeApiClient.ClaudeMessage

/**
 * Utility class for formatting prompts for the Claude API
 */
object ClaudePromptFormatter {

    /**
     * Format a system prompt and user prompt for Claude
     *
     * Claude uses a top-level "system" field in the request, separate from the messages array.
     * The messages array contains only the user and assistant messages.
     *
     * @param systemPrompt The system instructions (can be null)
     * @param userPrompt The user's query
     * @return A pair of (system prompt, messages list)
     */
    fun formatPrompt(systemPrompt: String?, userPrompt: String): Pair<String, List<ClaudeMessage>> {
        // Claude uses a top-level system field, so we return it separately from the messages
        return Pair(
            systemPrompt ?: "", // Convert null to empty string to avoid type mismatch
            listOf(
                ClaudeMessage(role = "user", content = userPrompt)
            )
        )
    }
}
