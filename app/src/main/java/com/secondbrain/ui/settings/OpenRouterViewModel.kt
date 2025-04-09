package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.OpenRouterProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpenRouterViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val openRouterProvider: OpenRouterProvider
) : ViewModel() {
    
    // List of available models
    private val _models = MutableStateFlow<List<AiModel>>(emptyList())
    val models: StateFlow<List<AiModel>> = _models.asStateFlow()
    
    // Currently selected model
    private val _selectedModel = MutableStateFlow<AiModel?>(null)
    val selectedModel: StateFlow<AiModel?> = _selectedModel.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
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
                // Get models from OpenRouter
                val result = openRouterProvider.fetchAvailableModels()
                result.onSuccess { modelList ->
                    _models.value = modelList
                    
                    // Set selected model
                    val currentModel = openRouterProvider.selectedModel.value
                    _selectedModel.value = currentModel ?: modelList.firstOrNull()
                }
            } catch (e: Exception) {
                android.util.Log.e("OpenRouterViewModel", "Error loading models", e)
                // Fallback to default models
                _models.value = openRouterProvider.availableModels
                _selectedModel.value = openRouterProvider.selectedModel.value ?: openRouterProvider.availableModels.firstOrNull()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh models from OpenRouter
     */
    fun refreshModels() {
        loadModels()
    }
    
    /**
     * Select a model
     */
    fun selectModel(model: AiModel) {
        _selectedModel.value = model
        openRouterProvider.setSelectedModel(model)
        
        // Save the selection to settings
        viewModelScope.launch {
            settingsRepository.setDefaultAiModel("OpenRouter - ${model.name}")
        }
    }
}
