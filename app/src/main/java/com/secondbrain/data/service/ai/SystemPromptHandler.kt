package com.secondbrain.data.service.ai

/**
 * Utility class for handling system prompts across different AI providers
 */
object SystemPromptHandler {

    /**
     * Enum representing different ways to handle system prompts
     */
    enum class SystemPromptFormat {
        /**
         * Use a dedicated system message (OpenAI, DeepSeek)
         * Format: [{"role": "system", "content": "..."}, {"role": "user", "content": "..."}]
         */
        DEDICATED_SYSTEM_MESSAGE,

        /**
         * Use a system field at the top level (Claude)
         * Format: {"system": "...", "messages": [{"role": "user", "content": "..."}]}
         */
        TOP_LEVEL_SYSTEM_FIELD,

        /**
         * Include system prompt as part of the conversation (Gemini)
         * Format: [
         *   {"role": "user", "content": "You are an AI with these instructions: ..."},
         *   {"role": "model", "content": "I understand and will follow these instructions."},
         *   {"role": "user", "content": "Actual query..."}
         * ]
         */
        CONVERSATION_BASED
    }

    /**
     * Get the appropriate system prompt format for a given provider
     */
    fun getFormatForProvider(providerName: String): SystemPromptFormat {
        return when (providerName.lowercase()) {
            "openai" -> SystemPromptFormat.DEDICATED_SYSTEM_MESSAGE
            "deepseek" -> SystemPromptFormat.DEDICATED_SYSTEM_MESSAGE
            "claude" -> SystemPromptFormat.TOP_LEVEL_SYSTEM_FIELD
            "gemini" -> SystemPromptFormat.CONVERSATION_BASED
            "openrouter" -> SystemPromptFormat.DEDICATED_SYSTEM_MESSAGE // Default for OpenRouter
            else -> SystemPromptFormat.DEDICATED_SYSTEM_MESSAGE // Default format
        }
    }

    /**
     * Create a default system prompt based on the task
     */
    fun createDefaultSystemPrompt(task: String, language: String): String {
        return when (task.lowercase()) {
            "summarize_concise" -> "You are a helpful assistant that creates concise summaries. Keep the summary brief and to the point, focusing only on the most important information. Respond in $language."
            "summarize_detailed" -> "You are a helpful assistant that creates detailed summaries. Include all important details, explanations, and context in your summary. Respond in $language."
            "summarize_bullet" -> "You are a helpful assistant that creates bullet point summaries. Format your summary as a list of bullet points covering the key information. Respond in $language."
            "summarize_qa" -> "You are a helpful assistant that creates Q&A summaries. Format your summary as a series of questions and answers covering the key information. Respond in $language."
            "summarize_key_facts" -> "You are a helpful assistant that extracts key facts. Identify and list the most important facts from the content. Respond in $language."
            "generate_tags" -> "You are a helpful assistant that generates relevant tags. Create tags that accurately represent the main topics and themes of the content. Respond in $language."
            "generate_title" -> "You are a helpful assistant that creates titles. Create a concise, descriptive title that captures the essence of the content. Respond in $language."
            "extract_text" -> "You are a helpful assistant that extracts text from images. Accurately transcribe all visible text while maintaining the original formatting as much as possible. Respond in $language."
            "transcribe_audio" -> "You are a helpful assistant that transcribes audio. Accurately transcribe the spoken content, indicating different speakers when possible. Respond in $language."
            else -> "You are a helpful assistant. Respond in $language."
        }
    }
}
