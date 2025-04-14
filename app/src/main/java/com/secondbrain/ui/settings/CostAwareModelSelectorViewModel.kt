package com.secondbrain.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.ai.ContentType
import com.secondbrain.data.model.ai.CostPreference
import com.secondbrain.data.model.ai.CostTier
import com.secondbrain.data.model.ai.ModelCapability
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.FreeFirstModelSelector
import com.secondbrain.data.service.ai.OpenRouterModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the cost-aware model selector screen
 */
@HiltViewModel
class CostAwareModelSelectorViewModel @Inject constructor(
    private val openRouterModelRepository: OpenRouterModelRepository,
    private val freeFirstModelSelector: FreeFirstModelSelector,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    companion object {
        private const val TAG = "CostAwareModelVM"
    }

    // All available models
    private val _models = MutableStateFlow<List<ModelCapability>>(emptyList())
    val models: StateFlow<List<ModelCapability>> = _models.asStateFlow()

    // Filtered models based on search and filters
    private val _filteredModels = MutableStateFlow<List<ModelCapability>>(emptyList())
    val filteredModels: StateFlow<List<ModelCapability>> = _filteredModels.asStateFlow()

    // Currently selected model
    private val _selectedModel = MutableStateFlow<ModelCapability?>(null)
    val selectedModel: StateFlow<ModelCapability?> = _selectedModel.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Cost preference
    private val _costPreference = MutableStateFlow(CostPreference.PREFER_FREE)
    val costPreference: StateFlow<CostPreference> = _costPreference.asStateFlow()

    // Selected content type filter
    private val _selectedContentType = MutableStateFlow<ContentType?>(null)
    val selectedContentType: StateFlow<ContentType?> = _selectedContentType.asStateFlow()

    // OpenRouter API key
    private var apiKey: String = ""

    init {
        // Load API key
        viewModelScope.launch {
            apiKey = settingsRepository.getOpenRouterApiKey()

            // Load cost preference
            val savedPreference = settingsRepository.getCostPreference()
            _costPreference.value = savedPreference ?: CostPreference.PREFER_FREE
        }
    }

    /**
     * Refresh models from OpenRouter
     */
    fun refreshModels() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                if (apiKey.isNotEmpty()) {
                    val result = openRouterModelRepository.getAllModels(apiKey, forceRefresh = true)

                    result.onSuccess { modelList ->
                        _models.value = modelList

                        // Apply filters
                        applyFilters()

                        // Set selected model
                        val savedModelId = settingsRepository.getSelectedModelId()
                        _selectedModel.value = modelList.find { it.id == savedModelId }
                            ?: modelList.firstOrNull { it.costTier == CostTier.FREE }
                            ?: modelList.firstOrNull()
                    }.onFailure { error ->
                        Log.e(TAG, "Error fetching models", error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing models", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query and filter models
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
     * Update cost preference and filter models
     */
    fun updateCostPreference(preference: CostPreference) {
        _costPreference.value = preference

        // Save preference
        viewModelScope.launch {
            settingsRepository.saveCostPreference(preference)
        }

        applyFilters()
    }

    /**
     * Update content type filter and filter models
     */
    fun updateContentType(contentType: ContentType) {
        // Toggle selection
        _selectedContentType.value = if (_selectedContentType.value == contentType) {
            null
        } else {
            contentType
        }

        applyFilters()
    }

    /**
     * Select a model
     */
    fun selectModel(model: ModelCapability) {
        _selectedModel.value = model

        // Save selection
        viewModelScope.launch {
            // Save the model ID for future reference
            settingsRepository.saveSelectedModelId(model.id)

            // Save the full model name with provider for display
            settingsRepository.setDefaultAiModel("OpenRouter - ${model.name}")
        }
    }

    /**
     * Apply filters to models
     */
    private fun applyFilters() {
        val allModels = _models.value

        // Apply cost preference filter
        val costFiltered = when (_costPreference.value) {
            CostPreference.FREE_ONLY -> allModels.filter { it.costTier == CostTier.FREE }
            CostPreference.PREFER_FREE -> allModels // Show all, but sort by cost tier
            CostPreference.BALANCED -> allModels // Show all, but sort by balance of cost and quality
            CostPreference.QUALITY_FIRST -> allModels // Show all, but sort by quality
        }

        // Apply content type filter
        val contentTypeFiltered = _selectedContentType.value?.let { contentType ->
            costFiltered.filter { it.supportedContentTypes.contains(contentType) }
        } ?: costFiltered

        // Apply search filter
        val searchFiltered = if (_searchQuery.value.isNotEmpty()) {
            val query = _searchQuery.value.lowercase()
            contentTypeFiltered.filter { model ->
                model.name.lowercase().contains(query) ||
                model.id.lowercase().contains(query)
            }
        } else {
            contentTypeFiltered
        }

        // Sort models based on cost preference
        val sortedModels = when (_costPreference.value) {
            CostPreference.FREE_ONLY -> searchFiltered.sortedByDescending { it.reliabilityScore }
            CostPreference.PREFER_FREE -> searchFiltered.sortedWith(
                compareBy<ModelCapability> { it.costTier.ordinal }
                    .thenByDescending { it.reliabilityScore }
            )
            CostPreference.BALANCED -> searchFiltered.sortedWith(
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
            CostPreference.QUALITY_FIRST -> searchFiltered.sortedWith(
                compareByDescending<ModelCapability> { it.reliabilityScore }
                    .thenBy { it.costTier.ordinal }
            )
        }

        _filteredModels.value = sortedModels
    }
}
