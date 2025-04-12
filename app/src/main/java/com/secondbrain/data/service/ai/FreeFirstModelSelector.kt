package com.secondbrain.data.service.ai

import android.util.Log
import com.secondbrain.data.model.ai.ContentType
import com.secondbrain.data.model.ai.CostPreference
import com.secondbrain.data.model.ai.CostTier
import com.secondbrain.data.model.ai.Feature
import com.secondbrain.data.model.ai.ModelCapability
import com.secondbrain.data.model.ai.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model selector that prioritizes free models when possible
 */
@Singleton
class FreeFirstModelSelector @Inject constructor(
    private val openRouterModelRepository: OpenRouterModelRepository
) {
    companion object {
        private const val TAG = "FreeFirstModelSelector"
    }
    
    /**
     * Select the optimal model for a task based on user preferences
     */
    suspend fun selectOptimalModel(
        apiKey: String,
        contentType: ContentType,
        contentSize: Int = 0,
        requiredFeatures: Set<Feature> = emptySet(),
        costPreference: CostPreference = CostPreference.PREFER_FREE
    ): Result<List<ModelCapability>> = withContext(Dispatchers.IO) {
        try {
            // Get all models that support the content type and required features
            val modelsResult = openRouterModelRepository.getModelsForTask(
                apiKey = apiKey,
                contentType = contentType,
                requiredFeatures = requiredFeatures
            )
            
            if (modelsResult.isFailure) {
                return@withContext modelsResult
            }
            
            val allModels = modelsResult.getOrNull() ?: emptyList()
            
            // Filter models that can handle the content size
            val suitableModels = if (contentSize > 0) {
                allModels.filter { it.maxTokens >= contentSize }
            } else {
                allModels
            }
            
            // Sort models based on cost preference
            val sortedModels = when (costPreference) {
                CostPreference.FREE_ONLY -> {
                    // Only include free models
                    suitableModels.filter { it.costTier == CostTier.FREE }
                        .sortedByDescending { it.reliabilityScore }
                }
                CostPreference.PREFER_FREE -> {
                    // Prioritize free models, then low cost, then others
                    suitableModels.sortedWith(
                        compareBy<ModelCapability> { it.costTier.ordinal }
                            .thenByDescending { it.reliabilityScore }
                    )
                }
                CostPreference.BALANCED -> {
                    // Balance cost and quality
                    suitableModels.sortedWith(
                        compareBy<ModelCapability> { 
                            // Create a score that balances cost and reliability
                            val costScore = when (it.costTier) {
                                CostTier.FREE -> 0
                                CostTier.LOW_COST -> 1
                                CostTier.MEDIUM_COST -> 2
                                CostTier.HIGH_COST -> 3
                            }
                            // Lower score is better (cost is weighted less than reliability)
                            costScore - (it.reliabilityScore * 3).toInt()
                        }
                    )
                }
                CostPreference.QUALITY_FIRST -> {
                    // Prioritize quality over cost
                    suitableModels.sortedWith(
                        compareByDescending<ModelCapability> { it.reliabilityScore }
                            .thenBy { it.costTier.ordinal }
                    )
                }
            }
            
            Result.success(sortedModels)
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting optimal model", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get fallback models for a primary model
     */
    suspend fun getFallbackModels(
        apiKey: String,
        primaryModel: ModelCapability,
        contentType: ContentType,
        requiredFeatures: Set<Feature> = emptySet()
    ): Result<List<ModelCapability>> = withContext(Dispatchers.IO) {
        try {
            // Get all models that support the content type and required features
            val modelsResult = openRouterModelRepository.getModelsForTask(
                apiKey = apiKey,
                contentType = contentType,
                requiredFeatures = requiredFeatures
            )
            
            if (modelsResult.isFailure) {
                return@withContext modelsResult
            }
            
            val allModels = modelsResult.getOrNull() ?: emptyList()
            
            // Filter out the primary model
            val otherModels = allModels.filter { it.id != primaryModel.id }
            
            // Sort fallback models by reliability and similarity to primary model
            val sortedFallbacks = otherModels.sortedWith(
                compareByDescending<ModelCapability> { it.reliabilityScore }
                    .thenBy { 
                        // Prefer models with similar cost tier
                        Math.abs(it.costTier.ordinal - primaryModel.costTier.ordinal) 
                    }
            )
            
            Result.success(sortedFallbacks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fallback models", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get the best free model for a task
     */
    suspend fun getBestFreeModel(
        apiKey: String,
        contentType: ContentType,
        requiredFeatures: Set<Feature> = emptySet()
    ): Result<ModelCapability?> = withContext(Dispatchers.IO) {
        try {
            // Get free models for the task
            val freeModelsResult = openRouterModelRepository.getFreeModelsForTask(
                apiKey = apiKey,
                contentType = contentType,
                requiredFeatures = requiredFeatures
            )
            
            if (freeModelsResult.isFailure) {
                return@withContext Result.failure(freeModelsResult.exceptionOrNull() ?: Exception("Failed to get free models"))
            }
            
            val freeModels = freeModelsResult.getOrNull() ?: emptyList()
            
            // Get the best free model by reliability
            val bestFreeModel = freeModels.maxByOrNull { it.reliabilityScore }
            
            Result.success(bestFreeModel)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting best free model", e)
            Result.failure(e)
        }
    }
}
