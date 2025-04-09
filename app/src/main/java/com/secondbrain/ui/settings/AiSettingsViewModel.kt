package com.secondbrain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.AiProvider
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.util.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val aiServiceManager: AiServiceManager,
    private val secureStorage: SecureStorage
) : ViewModel() {

    // List of available AI providers
    private val _providers = MutableStateFlow<List<AiProvider>>(emptyList())
    val providers: StateFlow<List<AiProvider>> = _providers.asStateFlow()

    // Currently selected provider
    private val _selectedProvider = MutableStateFlow<AiProvider?>(null)
    val selectedProvider: StateFlow<AiProvider?> = _selectedProvider.asStateFlow()

    // API keys for each provider
    private val _apiKeys = MutableStateFlow<Map<String, String>>(emptyMap())
    val apiKeys: StateFlow<Map<String, String>> = _apiKeys.asStateFlow()

    init {
        loadProviders()
        loadApiKeys()
    }

    /**
     * Load available AI providers
     */
    private fun loadProviders() {
        val availableProviders = aiServiceManager.getAvailableProviders()
        _providers.value = availableProviders

        viewModelScope.launch {
            // Set the default provider based on settings
            val defaultProvider = aiServiceManager.getDefaultProvider()
            _selectedProvider.value = defaultProvider
        }
    }

    /**
     * Load API keys from secure storage
     */
    private fun loadApiKeys() {
        val keys = mapOf(
            "Gemini" to secureStorage.getString(SecureStorage.KEY_GEMINI_API_KEY),
            "OpenAI" to secureStorage.getString(SecureStorage.KEY_OPENAI_API_KEY),
            "Claude" to secureStorage.getString(SecureStorage.KEY_CLAUDE_API_KEY),
            "DeepSeek" to secureStorage.getString(SecureStorage.KEY_DEEPSEEK_API_KEY),
            "OpenRouter" to secureStorage.getString(SecureStorage.KEY_OPENROUTER_API_KEY)
        )
        _apiKeys.value = keys
    }

    /**
     * Select a provider as the default
     */
    fun selectProvider(provider: AiProvider) {
        _selectedProvider.value = provider
    }

    /**
     * Update an API key for a provider
     */
    fun updateApiKey(providerName: String, apiKey: String) {
        val currentKeys = _apiKeys.value.toMutableMap()
        currentKeys[providerName] = apiKey
        _apiKeys.value = currentKeys
    }

    /**
     * Save settings to persistent storage
     */
    fun saveSettings() {
        viewModelScope.launch {
            // Save default AI model
            selectedProvider.value?.let { provider ->
                settingsRepository.setDefaultAiModel(provider.name)
            }

            // Save API keys to secure storage
            apiKeys.value["Gemini"]?.let { key ->
                secureStorage.storeString(SecureStorage.KEY_GEMINI_API_KEY, key)
            }
            apiKeys.value["OpenAI"]?.let { key ->
                secureStorage.storeString(SecureStorage.KEY_OPENAI_API_KEY, key)
            }
            apiKeys.value["Claude"]?.let { key ->
                secureStorage.storeString(SecureStorage.KEY_CLAUDE_API_KEY, key)
            }
            apiKeys.value["DeepSeek"]?.let { key ->
                secureStorage.storeString(SecureStorage.KEY_DEEPSEEK_API_KEY, key)
            }
            apiKeys.value["OpenRouter"]?.let { key ->
                secureStorage.storeString(SecureStorage.KEY_OPENROUTER_API_KEY, key)
            }

            android.util.Log.d("AiSettingsViewModel", "API keys saved to secure storage")
        }
    }
}
