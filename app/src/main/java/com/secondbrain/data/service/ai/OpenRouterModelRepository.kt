package com.secondbrain.data.service.ai

import android.util.Log
import com.secondbrain.data.model.ai.ContentType
import com.secondbrain.data.model.ai.CostTier
import com.secondbrain.data.model.ai.Feature
import com.secondbrain.data.model.ai.ModelCapability
import com.secondbrain.data.model.ai.Provider
import com.secondbrain.data.model.ai.TaskType
import com.secondbrain.data.service.ai.api.OpenRouterApiClient
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.ModelCapability as OldModelCapability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for OpenRouter models with focus on free models
 */
@Singleton
class OpenRouterModelRepository @Inject constructor(
    private val openRouterApiClient: OpenRouterApiClient
) {
    companion object {
        private const val TAG = "OpenRouterModelRepo"

        // Known free model IDs on OpenRouter
        private val FREE_MODEL_IDS = setOf(
            "openrouter/auto",
            "mistralai/mistral-7b-instruct",
            "meta-llama/llama-3-8b-instruct",
            "google/gemma-7b-it",
            "nousresearch/nous-hermes-2-mixtral-8x7b-dpo",
            "openchat/openchat-7b",
            "01-ai/yi-1.5-9b-chat"
        )
    }

    // Cache of model capabilities
    private var modelCapabilitiesCache: List<ModelCapability>? = null

    /**
     * Get all available models from OpenRouter
     */
    suspend fun getAllModels(apiKey: String, forceRefresh: Boolean = false): Result<List<ModelCapability>> = withContext(Dispatchers.IO) {
        // Return cached models if available and not forcing refresh
        if (!forceRefresh && modelCapabilitiesCache != null) {
            return@withContext Result.success(modelCapabilitiesCache!!)
        }

        try {
            val modelsResult = openRouterApiClient.fetchAvailableModels(apiKey)

            if (modelsResult.isFailure) {
                return@withContext Result.failure(modelsResult.exceptionOrNull() ?: Exception("Failed to fetch models"))
            }

            val aiModels = modelsResult.getOrNull() ?: emptyList()
            val modelCapabilities = aiModels.map { aiModel ->
                // Determine if this is a free model
                val isFreeModel = FREE_MODEL_IDS.any { freeId ->
                    aiModel.id.contains(freeId, ignoreCase = true)
                } || aiModel.id.contains("free", ignoreCase = true)

                // Determine cost tier
                val costTier = when {
                    isFreeModel -> CostTier.FREE
                    aiModel.id.contains("gpt-3.5") -> CostTier.LOW_COST
                    aiModel.id.contains("claude-instant") -> CostTier.LOW_COST
                    aiModel.id.contains("gpt-4") -> CostTier.HIGH_COST
                    aiModel.id.contains("claude-3-opus") -> CostTier.HIGH_COST
                    aiModel.id.contains("claude-3-sonnet") -> CostTier.MEDIUM_COST
                    aiModel.id.contains("claude-3-haiku") -> CostTier.LOW_COST
                    else -> CostTier.MEDIUM_COST
                }

                // Determine estimated cost per 1K tokens (null for free models)
                val estimatedCostPer1KTokens: Float? = when {
                    isFreeModel -> null
                    aiModel.id.contains("gpt-3.5") -> 0.0015f
                    aiModel.id.contains("gpt-4-turbo") -> 0.01f
                    aiModel.id.contains("gpt-4") -> 0.03f
                    aiModel.id.contains("claude-3-opus") -> 0.03f
                    aiModel.id.contains("claude-3-sonnet") -> 0.015f
                    aiModel.id.contains("claude-3-haiku") -> 0.0025f
                    else -> 0.01f
                }

                // Map capabilities to content types
                val supportedContentTypes = mutableSetOf<ContentType>()
                supportedContentTypes.add(ContentType.TEXT) // All models support text

                if (aiModel.capabilities.contains(OldModelCapability.IMAGE_UNDERSTANDING)) {
                    supportedContentTypes.add(ContentType.IMAGE)
                }

                if (aiModel.capabilities.contains(OldModelCapability.PDF_PROCESSING)) {
                    supportedContentTypes.add(ContentType.PDF)
                }

                if (aiModel.capabilities.contains(OldModelCapability.WEB_CONTENT)) {
                    supportedContentTypes.add(ContentType.WEB_LINK)
                }

                if (aiModel.capabilities.contains(OldModelCapability.AUDIO_TRANSCRIPTION)) {
                    supportedContentTypes.add(ContentType.AUDIO)
                }

                // Map capabilities to features
                val features = mutableSetOf<Feature>()
                features.add(Feature.SUMMARIZATION) // All models support summarization

                if (aiModel.capabilities.contains(OldModelCapability.TAG_GENERATION)) {
                    features.add(Feature.TAG_GENERATION)
                }

                if (aiModel.capabilities.contains(OldModelCapability.TITLE_GENERATION)) {
                    features.add(Feature.TITLE_GENERATION)
                }

                if (aiModel.capabilities.contains(OldModelCapability.CODE_UNDERSTANDING)) {
                    features.add(Feature.CODE_UNDERSTANDING)
                }

                // Determine reliability score (0.0-1.0)
                // This is a heuristic based on model capabilities and known performance
                val reliabilityScore = when {
                    aiModel.id.contains("gpt-4") -> 0.95f
                    aiModel.id.contains("claude-3-opus") -> 0.95f
                    aiModel.id.contains("claude-3-sonnet") -> 0.9f
                    aiModel.id.contains("gpt-3.5") -> 0.85f
                    aiModel.id.contains("claude-3-haiku") -> 0.85f
                    aiModel.id.contains("llama-3") -> 0.8f
                    aiModel.id.contains("mistral") -> 0.75f
                    isFreeModel -> 0.7f
                    else -> 0.65f
                }

                // Determine recommended tasks
                val recommendedTasks = mutableSetOf<TaskType>()

                // Large context models are good for long documents
                if (aiModel.contextWindow >= 16000) {
                    recommendedTasks.add(TaskType.LONG_DOCUMENT_SUMMARY)
                } else {
                    recommendedTasks.add(TaskType.SHORT_TEXT_SUMMARY)
                }

                // Vision models are good for image analysis
                if (supportedContentTypes.contains(ContentType.IMAGE)) {
                    recommendedTasks.add(TaskType.IMAGE_ANALYSIS)
                }

                // Models with web content capability are good for web extraction
                if (supportedContentTypes.contains(ContentType.WEB_LINK)) {
                    recommendedTasks.add(TaskType.WEB_CONTENT_EXTRACTION)
                }

                // Models with audio capability are good for transcription
                if (supportedContentTypes.contains(ContentType.AUDIO)) {
                    recommendedTasks.add(TaskType.AUDIO_TRANSCRIPTION)
                }

                // Models with code understanding are good for code explanation
                if (features.contains(Feature.CODE_UNDERSTANDING)) {
                    recommendedTasks.add(TaskType.CODE_EXPLANATION)
                }

                ModelCapability(
                    id = aiModel.id,
                    name = aiModel.name,
                    costTier = costTier,
                    estimatedCostPer1KTokens = estimatedCostPer1KTokens,
                    provider = Provider.OPENROUTER,
                    supportedContentTypes = supportedContentTypes,
                    maxTokens = aiModel.maxTokens,
                    features = features,
                    reliabilityScore = reliabilityScore,
                    recommendedFor = recommendedTasks
                )
            }

            // Cache the results
            modelCapabilitiesCache = modelCapabilities

            Result.success(modelCapabilities)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting models from OpenRouter", e)
            Result.failure(e)
        }
    }

    /**
     * Get free models from OpenRouter
     */
    suspend fun getFreeModels(apiKey: String): Result<List<ModelCapability>> = withContext(Dispatchers.IO) {
        val allModelsResult = getAllModels(apiKey)

        if (allModelsResult.isFailure) {
            return@withContext allModelsResult
        }

        val allModels = allModelsResult.getOrNull() ?: emptyList()
        val freeModels = allModels.filter { it.costTier == CostTier.FREE }

        Result.success(freeModels)
    }

    /**
     * Get models suitable for a specific task
     */
    suspend fun getModelsForTask(
        apiKey: String,
        contentType: ContentType,
        requiredFeatures: Set<Feature> = emptySet()
    ): Result<List<ModelCapability>> = withContext(Dispatchers.IO) {
        val allModelsResult = getAllModels(apiKey)

        if (allModelsResult.isFailure) {
            return@withContext allModelsResult
        }

        val allModels = allModelsResult.getOrNull() ?: emptyList()
        val suitableModels = allModels.filter { model ->
            model.supportedContentTypes.contains(contentType) &&
            model.features.containsAll(requiredFeatures)
        }

        Result.success(suitableModels)
    }

    /**
     * Get free models suitable for a specific task
     */
    suspend fun getFreeModelsForTask(
        apiKey: String,
        contentType: ContentType,
        requiredFeatures: Set<Feature> = emptySet()
    ): Result<List<ModelCapability>> = withContext(Dispatchers.IO) {
        val allModelsResult = getAllModels(apiKey)

        if (allModelsResult.isFailure) {
            return@withContext allModelsResult
        }

        val allModels = allModelsResult.getOrNull() ?: emptyList()
        val suitableModels = allModels.filter { model ->
            model.costTier == CostTier.FREE &&
            model.supportedContentTypes.contains(contentType) &&
            model.features.containsAll(requiredFeatures)
        }

        Result.success(suitableModels)
    }

    /**
     * Get recommended models for a specific task, prioritizing free models
     */
    suspend fun getRecommendedModelsForTask(
        apiKey: String,
        contentType: ContentType,
        requiredFeatures: Set<Feature> = emptySet(),
        preferFree: Boolean = true
    ): Result<List<ModelCapability>> = withContext(Dispatchers.IO) {
        val allModelsResult = getAllModels(apiKey)

        if (allModelsResult.isFailure) {
            return@withContext allModelsResult
        }

        val allModels = allModelsResult.getOrNull() ?: emptyList()
        val taskType = mapContentTypeToTaskType(contentType)

        // Filter models that support the content type and required features
        val suitableModels = allModels.filter { model ->
            model.supportedContentTypes.contains(contentType) &&
            model.features.containsAll(requiredFeatures)
        }

        // Sort models by preference
        val sortedModels = if (preferFree) {
            // First by cost tier (free first), then by recommendation for task, then by reliability
            suitableModels.sortedWith(
                compareBy<ModelCapability> { it.costTier.ordinal }
                    .thenByDescending { if (it.recommendedFor.contains(taskType)) 1 else 0 }
                    .thenByDescending { it.reliabilityScore }
            )
        } else {
            // First by recommendation for task, then by reliability, then by cost tier
            suitableModels.sortedWith(
                compareByDescending<ModelCapability> { if (it.recommendedFor.contains(taskType)) 1 else 0 }
                    .thenByDescending { it.reliabilityScore }
                    .thenBy { it.costTier.ordinal }
            )
        }

        Result.success(sortedModels)
    }

    /**
     * Map content type to task type
     */
    private fun mapContentTypeToTaskType(contentType: ContentType): TaskType {
        return when (contentType) {
            ContentType.TEXT -> TaskType.SHORT_TEXT_SUMMARY
            ContentType.IMAGE -> TaskType.IMAGE_ANALYSIS
            ContentType.PDF -> TaskType.LONG_DOCUMENT_SUMMARY
            ContentType.AUDIO -> TaskType.AUDIO_TRANSCRIPTION
            ContentType.WEB_LINK -> TaskType.WEB_CONTENT_EXTRACTION
            ContentType.YOUTUBE -> TaskType.YOUTUBE_SUMMARY
        }
    }
}
