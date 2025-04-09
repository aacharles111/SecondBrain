package com.secondbrain.data.service.ai.content

import android.util.Log
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for content-aware summarization
 */
@Singleton
class ContentAwareSummarizer @Inject constructor(
    private val contentTypeDetector: ContentTypeDetector,
    private val promptGenerator: SpecializedPromptGenerator,
    private val aiServiceManager: AiServiceManager,
    private val entityExtractor: EntityExtractor
) {
    companion object {
        private const val TAG = "ContentAwareSummarizer"
    }
    
    /**
     * Summarize content with content-aware prompts
     */
    suspend fun summarize(
        content: String,
        summaryType: SummaryType,
        language: String,
        maxLength: Int? = null,
        customInstructions: String? = null,
        aiModel: String? = null
    ): Result<SummarizationResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Summarizing content with content-aware prompts: ${content.take(100)}...")
            
            // Detect content type
            val contentTypeResult = contentTypeDetector.detectContentType(content)
            if (contentTypeResult.isFailure) {
                return@withContext Result.failure(contentTypeResult.exceptionOrNull() ?: Exception("Failed to detect content type"))
            }
            
            val contentType = contentTypeResult.getOrNull() ?: ContentType.UNKNOWN
            Log.d(TAG, "Detected content type: $contentType")
            
            // Generate specialized prompts
            val systemPrompt = promptGenerator.generateSystemPrompt(contentType, summaryType)
            val userPrompt = promptGenerator.generateUserPrompt(contentType, summaryType, language, customInstructions)
            
            // Create summarization options
            val options = SummarizationOptions(
                summaryType = summaryType,
                language = language,
                maxLength = maxLength,
                customInstructions = userPrompt
            )
            
            // Extract entities
            val entitiesResult = entityExtractor.extractEntities(content)
            val entities = entitiesResult.getOrNull() ?: emptyList()
            
            // Summarize content
            val summaryResult = aiServiceManager.summarize(content, options, aiModel)
            
            if (summaryResult.isSuccess) {
                val summary = summaryResult.getOrNull() ?: ""
                
                // Create summarization result
                val result = SummarizationResult(
                    summary = summary,
                    contentType = contentType,
                    entities = entities
                )
                
                Result.success(result)
            } else {
                Result.failure(summaryResult.exceptionOrNull() ?: Exception("Failed to summarize content"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error summarizing content with content-aware prompts", e)
            Result.failure(e)
        }
    }
}

/**
 * Result of content-aware summarization
 */
data class SummarizationResult(
    val summary: String,
    val contentType: ContentType,
    val entities: List<Entity>
)
