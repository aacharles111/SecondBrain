package com.secondbrain.data.service

import com.secondbrain.data.service.ai.AiServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Legacy AI service that uses the new AiServiceManager
 * This is kept for backward compatibility
 */
@Singleton
class AiService @Inject constructor(
    private val aiServiceManager: AiServiceManager
) {

    /**
     * Summarize content using the specified AI model
     *
     * @param content The content to summarize
     * @param summaryType The type of summary to generate (concise, detailed, etc.)
     * @param language The language to use for the summary
     * @param aiModel The AI model to use for summarization
     * @param customInstructions Optional custom instructions for the AI
     * @return The generated summary
     */
    suspend fun summarize(
        content: String,
        summaryType: String,
        language: String,
        aiModel: String,
        customInstructions: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Delegate to the AiServiceManager
            aiServiceManager.summarize(
                content = content,
                summaryType = summaryType,
                language = language,
                aiModel = aiModel,
                customInstructions = customInstructions
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract tags from content
     *
     * @param content The content to extract tags from
     * @return A list of suggested tags
     */
    suspend fun extractTags(content: String, language: String = "English"): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            // Delegate to the AiServiceManager
            aiServiceManager.generateTags(
                content = content,
                language = language
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a title from content
     *
     * @param content The content to generate a title for
     * @return A suggested title
     */
    suspend fun generateTitle(content: String, language: String = "English"): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Delegate to the AiServiceManager
            aiServiceManager.generateTitle(
                content = content,
                language = language
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
