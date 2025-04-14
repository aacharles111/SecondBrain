package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.service.ai.AiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Helper class for model view models to implement search functionality
 */
class ModelViewModelHelper(
    private val viewModel: ViewModel,
    private val modelsFlow: MutableStateFlow<List<AiModel>>,
    private val viewModelScope: kotlinx.coroutines.CoroutineScope
) {
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Filtered models based on search query
    private val _filteredModels = MutableStateFlow<List<AiModel>>(emptyList())
    val filteredModels: StateFlow<List<AiModel>> = _filteredModels.asStateFlow()
    
    init {
        // Initialize filtered models with all models
        _filteredModels.value = modelsFlow.value
    }
    
    /**
     * Filter models based on search query
     */
    fun filterModels() {
        val query = _searchQuery.value.trim().lowercase()
        if (query.isEmpty()) {
            _filteredModels.value = modelsFlow.value
        } else {
            _filteredModels.value = modelsFlow.value.filter { model ->
                model.name.lowercase().contains(query) ||
                model.id.lowercase().contains(query)
            }
        }
    }
    
    /**
     * Update search query and filter models
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterModels()
    }
    
    /**
     * Update models and filter them
     */
    fun updateModels(models: List<AiModel>) {
        viewModelScope.launch {
            modelsFlow.value = models
            filterModels()
        }
    }
}
