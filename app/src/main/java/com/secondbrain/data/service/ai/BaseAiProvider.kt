package com.secondbrain.data.service.ai

import android.net.Uri

/**
 * Base interface for AI providers with standardized system prompt handling
 */
interface BaseAiProvider : AiProvider {

    /**
     * Get the system prompt format used by this provider
     */
    val systemPromptFormat: SystemPromptHandler.SystemPromptFormat

    /**
     * Process a system prompt according to this provider's requirements
     * This method should be used by API clients to format system prompts correctly
     *
     * @param systemPrompt The system prompt to process
     * @param userPrompt The user prompt that will be sent along with the system prompt
     * @return A formatted prompt object that can be used in the API request
     */
    suspend fun processSystemPrompt(systemPrompt: String?, userPrompt: String): Any

    /**
     * Get the API key for this provider
     * This is already implemented in the provider classes
     */
    // No getApiKey() method here - providers already implement this

    /**
     * Get the selected model ID for this provider
     */
    suspend fun getSelectedModelId(): String?

    /**
     * Create default summarization options with appropriate system prompt
     */
    fun createSummarizationOptions(
        summaryType: SummaryType,
        language: String,
        maxLength: Int? = null,
        customInstructions: String? = null
    ): SummarizationOptions {
        // Create a task identifier based on summary type
        val task = when (summaryType) {
            SummaryType.CONCISE -> "summarize_concise"
            SummaryType.DETAILED -> "summarize_detailed"
            SummaryType.BULLET_POINTS -> "summarize_bullet"
            SummaryType.QUESTION_ANSWER -> "summarize_qa"
            SummaryType.KEY_FACTS -> "summarize_key_facts"
        }

        // Create a default system prompt
        val systemPrompt = SystemPromptHandler.createDefaultSystemPrompt(task, language)

        return SummarizationOptions(
            summaryType = summaryType,
            language = language,
            maxLength = maxLength,
            customInstructions = customInstructions,
            systemPrompt = systemPrompt
        )
    }

    /**
     * Create default tag generation options with appropriate system prompt
     */
    fun createTagGenerationOptions(
        maxTags: Int = 10,
        language: String
    ): TagGenerationOptions {
        return TagGenerationOptions(
            maxTags = maxTags,
            language = language
        )
    }

    /**
     * Create default title generation options with appropriate system prompt
     */
    fun createTitleGenerationOptions(
        maxLength: Int = 100,
        language: String
    ): TitleGenerationOptions {
        return TitleGenerationOptions(
            maxLength = maxLength,
            language = language
        )
    }

    /**
     * Create default extraction options
     */
    fun createExtractionOptions(
        language: String,
        extractTables: Boolean = false,
        extractDiagrams: Boolean = false
    ): ExtractionOptions {
        return ExtractionOptions(
            language = language,
            extractTables = extractTables,
            extractDiagrams = extractDiagrams
        )
    }

    /**
     * Create default transcription options
     */
    fun createTranscriptionOptions(
        language: String,
        prompt: String? = null,
        timestamped: Boolean = false
    ): TranscriptionOptions {
        return TranscriptionOptions(
            language = language,
            prompt = prompt,
            timestamped = timestamped
        )
    }
}
