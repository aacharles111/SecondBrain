package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.ClaudeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClaudeModelViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val claudeProvider: ClaudeProvider
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

    // Search functionality helper
    private val searchHelper = ModelViewModelHelper(this, _models, viewModelScope)
    override val searchQuery: StateFlow<String> = searchHelper.searchQuery
    override val filteredModels: StateFlow<List<AiModel>> = searchHelper.filteredModels

    init {
        loadModels()
    }

    /**
     * Load models from Claude
     */
    private fun loadModels() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Check if API key is configured
                if (claudeProvider.isConfigured()) {
                    // Get models from Claude
                    val result = claudeProvider.fetchAvailableModels()
                    result.onSuccess { modelList ->
                        _models.value = modelList

                        // Set selected model
                        val currentModel = claudeProvider.selectedModel.value
                        _selectedModel.value = currentModel ?: modelList.firstOrNull()

                        // Update filtered models
                        searchHelper.filterModels()
                    }
                } else {
                    // Use default models if API key is not configured
                    _models.value = claudeProvider.availableModels
                    _selectedModel.value = claudeProvider.selectedModel.value ?: claudeProvider.availableModels.firstOrNull()

                    // Update filtered models
                    searchHelper.filterModels()
                }
            } catch (e: Exception) {
                android.util.Log.e("ClaudeModelViewModel", "Error loading models", e)
                // Fallback to default models
                _models.value = claudeProvider.availableModels
                _selectedModel.value = claudeProvider.selectedModel.value ?: claudeProvider.availableModels.firstOrNull()

                // Update filtered models
                searchHelper.filterModels()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query
     */
    override fun updateSearchQuery(query: String) {
        searchHelper.updateSearchQuery(query)
    }

    /**
     * Refresh models from Claude
     */
    override fun refreshModels() {
        loadModels()
    }

    /**
     * Select a model
     */
    override fun selectModel(model: AiModel) {
        _selectedModel.value = model
        claudeProvider.setSelectedModel(model)

        // Save the selection to settings
        viewModelScope.launch {
            settingsRepository.setDefaultAiModel("Claude - ${model.name}")
        }
    }
}
