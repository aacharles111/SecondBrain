package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.OpenRouterProvider
import com.secondbrain.util.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpenRouterViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val openRouterProvider: OpenRouterProvider,
    private val secureStorage: SecureStorage
) : ViewModel(), BaseModelSelectorViewModel {

    // List of available models
    private val _models = MutableStateFlow<List<AiModel>>(emptyList())
    override val models: StateFlow<List<AiModel>> = _models.asStateFlow()

    // Currently selected model
    private val _selectedModel = MutableStateFlow<AiModel?>(null)
    override val selectedModel: StateFlow<AiModel?> = _selectedModel.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered models based on search query
    private val _filteredModels = MutableStateFlow<List<AiModel>>(emptyList())
    override val filteredModels: StateFlow<List<AiModel>> = _filteredModels.asStateFlow()

    init {
        loadModels()
    }

    /**
     * Load models from OpenRouter
     */
    private fun loadModels() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Check if API key is configured
                if (openRouterProvider.isConfigured()) {
                    // Get models from OpenRouter
                    val result = openRouterProvider.fetchAvailableModels()
                    result.onSuccess { modelList ->
                        _models.value = modelList

                        // Set selected model
                        val currentModel = openRouterProvider.selectedModel.value
                        _selectedModel.value = currentModel ?: modelList.firstOrNull()

                        // Filter models based on current search query
                        filterModels()
                    }
                } else {
                    // Use default models if API key is not configured
                    _models.value = openRouterProvider.availableModels
                    _selectedModel.value = openRouterProvider.selectedModel.value ?: openRouterProvider.availableModels.firstOrNull()

                    // Filter models based on current search query
                    filterModels()
                }
            } catch (e: Exception) {
                android.util.Log.e("OpenRouterViewModel", "Error loading models", e)
                // Fallback to default models
                _models.value = openRouterProvider.availableModels
                _selectedModel.value = openRouterProvider.selectedModel.value ?: openRouterProvider.availableModels.firstOrNull()

                // Filter models based on current search query
                filterModels()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filter models based on search query
     */
    private fun filterModels() {
        val query = _searchQuery.value.trim().lowercase()
        if (query.isEmpty()) {
            _filteredModels.value = _models.value
        } else {
            _filteredModels.value = _models.value.filter { model ->
                model.name.lowercase().contains(query) ||
                model.id.lowercase().contains(query)
            }
        }
    }

    /**
     * Update search query and filter models
     */
    override fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterModels()
    }

    /**
     * Refresh models from OpenRouter
     */
    override fun refreshModels() {
        loadModels()
    }

    /**
     * Select a model
     */
    override fun selectModel(model: AiModel) {
        _selectedModel.value = model
        openRouterProvider.setSelectedModel(model)

        // Save the selection to settings
        viewModelScope.launch {
            settingsRepository.setDefaultAiModel("OpenRouter - ${model.name}")
        }
    }

    /**
     * Save OpenRouter API key
     */
    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            secureStorage.putString(SecureStorage.KEY_OPENROUTER_API_KEY, apiKey)
            // Refresh models after API key is saved
            loadModels()
        }
    }


}
