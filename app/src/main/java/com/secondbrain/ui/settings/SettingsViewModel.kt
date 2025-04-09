package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsRepository: SettingsRepository
) : ViewModel() {

    // Dark mode setting
    val darkMode: StateFlow<String> = settingsRepository.darkModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    // Dynamic colors setting
    val useDynamicColors: StateFlow<Boolean> = settingsRepository.useDynamicColorsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Default AI model
    val defaultAiModel: StateFlow<String> = settingsRepository.defaultAiModelFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Gemini"
        )

    // Default language
    val defaultLanguage: StateFlow<String> = settingsRepository.defaultLanguageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "English"
        )

    // Update dark mode setting
    fun setDarkMode(darkMode: String) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(darkMode)
        }
    }

    // Update dynamic colors setting
    fun setUseDynamicColors(useDynamicColors: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColors(useDynamicColors)
        }
    }

    // Update default AI model
    fun setDefaultAiModel(model: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultAiModel(model)
        }
    }

    // Update default language
    fun setDefaultLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultLanguage(language)
        }
    }
}
