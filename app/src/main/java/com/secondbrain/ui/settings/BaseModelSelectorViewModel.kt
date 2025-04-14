package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import com.secondbrain.data.service.ai.AiModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for model selector view models
 */
interface BaseModelSelectorViewModel {
    /**
     * List of available models
     */
    val models: StateFlow<List<AiModel>>

    /**
     * Currently selected model
     */
    val selectedModel: StateFlow<AiModel?>

    /**
     * Loading state
     */
    val isLoading: StateFlow<Boolean>

    /**
     * Search query
     */
    val searchQuery: StateFlow<String>

    /**
     * Filtered models based on search query
     */
    val filteredModels: StateFlow<List<AiModel>>

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String)

    /**
     * Refresh models from the API
     */
    fun refreshModels()

    /**
     * Select a model
     */
    fun selectModel(model: AiModel)
}
