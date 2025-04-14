package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.DeepSeekProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeepSeekModelViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val deepSeekProvider: DeepSeekProvider
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
     * Load models from DeepSeek
     */
    private fun loadModels() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Check if API key is configured
                if (deepSeekProvider.isConfigured()) {
                    // Get models from DeepSeek
                    val result = deepSeekProvider.fetchAvailableModels()
                    result.onSuccess { modelList ->
                        _models.value = modelList

                        // Set selected model
                        val currentModel = deepSeekProvider.selectedModel.value
                        _selectedModel.value = currentModel ?: modelList.firstOrNull()

                        // Update filtered models
                        searchHelper.filterModels()
                    }
                } else {
                    // Use default models if API key is not configured
                    _models.value = deepSeekProvider.availableModels
                    _selectedModel.value = deepSeekProvider.selectedModel.value ?: deepSeekProvider.availableModels.firstOrNull()

                    // Update filtered models
                    searchHelper.filterModels()
                }
            } catch (e: Exception) {
                android.util.Log.e("DeepSeekModelViewModel", "Error loading models", e)
                // Fallback to default models
                _models.value = deepSeekProvider.availableModels
                _selectedModel.value = deepSeekProvider.selectedModel.value ?: deepSeekProvider.availableModels.firstOrNull()

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
     * Refresh models from DeepSeek
     */
    override fun refreshModels() {
        loadModels()
    }

    /**
     * Select a model
     */
    override fun selectModel(model: AiModel) {
        _selectedModel.value = model
        deepSeekProvider.setSelectedModel(model)

        // Save the selection to settings
        viewModelScope.launch {
            settingsRepository.setDefaultAiModel("DeepSeek - ${model.name}")
        }
    }
}
