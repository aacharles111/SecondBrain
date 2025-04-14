package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.CardType
import com.secondbrain.data.repository.SystemPromptRepository
import com.secondbrain.data.service.ai.SummaryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SystemPromptSettingsViewModel @Inject constructor(
    private val systemPromptRepository: SystemPromptRepository
) : ViewModel() {
    
    // Selected content type
    private val _selectedContentType = MutableStateFlow(CardType.URL)
    val selectedContentType: StateFlow<CardType> = _selectedContentType.asStateFlow()
    
    // Selected summary type
    private val _selectedSummaryType = MutableStateFlow(SummaryType.CONCISE)
    val selectedSummaryType: StateFlow<SummaryType> = _selectedSummaryType.asStateFlow()
    
    // Current prompt
    private val _currentPrompt = MutableStateFlow("")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()
    
    // Editing state
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()
    
    // Saving state
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    init {
        loadCurrentPrompt()
    }
    
    /**
     * Load the current prompt for the selected content type and summary type
     */
    private fun loadCurrentPrompt() {
        viewModelScope.launch {
            val contentType = _selectedContentType.value
            val summaryType = _selectedSummaryType.value
            
            val prompt = systemPromptRepository.getSystemPrompt(contentType, summaryType).first()
            _currentPrompt.value = prompt
        }
    }
    
    /**
     * Select a content type
     */
    fun selectContentType(contentType: CardType) {
        _selectedContentType.value = contentType
        loadCurrentPrompt()
    }
    
    /**
     * Select a summary type
     */
    fun selectSummaryType(summaryType: SummaryType) {
        _selectedSummaryType.value = summaryType
        loadCurrentPrompt()
    }
    
    /**
     * Start editing the current prompt
     */
    fun startEditing() {
        _isEditing.value = true
    }
    
    /**
     * Update the current prompt
     */
    fun updatePrompt(prompt: String) {
        _currentPrompt.value = prompt
    }
    
    /**
     * Save the current prompt
     */
    fun savePrompt() {
        viewModelScope.launch {
            _isSaving.value = true
            
            try {
                val contentType = _selectedContentType.value
                val summaryType = _selectedSummaryType.value
                
                systemPromptRepository.saveSystemPrompt(contentType, summaryType, _currentPrompt.value)
                
                _isEditing.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    /**
     * Reset the current prompt to the default
     */
    fun resetPrompt() {
        viewModelScope.launch {
            _isSaving.value = true
            
            try {
                val contentType = _selectedContentType.value
                val summaryType = _selectedSummaryType.value
                
                systemPromptRepository.resetSystemPrompt(contentType, summaryType)
                
                // Reload the prompt
                loadCurrentPrompt()
                
                _isEditing.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }
}
