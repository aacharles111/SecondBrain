package com.secondbrain.data.service.ai.provider

import com.secondbrain.data.service.ai.api.OpenRouterApiClient.OpenRouterMessage

/**
 * Utility class for formatting prompts for the OpenRouter API
 */
object OpenRouterPromptFormatter {

    /**
     * Format a system prompt and user prompt for OpenRouter
     *
     * OpenRouter supports multiple model providers, but generally follows the OpenAI format
     * with a dedicated system message with role="system" followed by the user's message.
     * For models that don't support system messages, OpenRouter handles the conversion.
     *
     * @param systemPrompt The system instructions (can be null)
     * @param userPrompt The user's query
     * @param modelId The ID of the model being used (to handle model-specific formatting)
     * @return A list of OpenRouterMessage objects representing the conversation
     */
    fun formatPrompt(systemPrompt: String?, userPrompt: String, modelId: String): List<OpenRouterMessage> {
        // For Claude models via OpenRouter, we might need special handling
        val isClaudeModel = modelId.contains("claude", ignoreCase = true)
        
        return if (systemPrompt.isNullOrEmpty()) {
            // If no system prompt, just include the user message
            listOf(
                OpenRouterMessage(role = "user", content = userPrompt)
            )
        } else if (isClaudeModel) {
            // For Claude models, OpenRouter will handle the system prompt appropriately
            listOf(
                OpenRouterMessage(role = "system", content = systemPrompt),
                OpenRouterMessage(role = "user", content = userPrompt)
            )
        } else {
            // Standard format for most models
            listOf(
                OpenRouterMessage(role = "system", content = systemPrompt),
                OpenRouterMessage(role = "user", content = userPrompt)
            )
        }
    }
}
