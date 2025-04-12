package com.secondbrain.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys for preferences
    companion object {
        val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
        val USE_DYNAMIC_COLORS_KEY = booleanPreferencesKey("use_dynamic_colors")
        val DEFAULT_AI_MODEL_KEY = stringPreferencesKey("default_ai_model")
        val DEFAULT_LANGUAGE_KEY = stringPreferencesKey("default_language")
        val PINNED_CARDS_KEY = stringPreferencesKey("pinned_cards")
        val COST_PREFERENCE_KEY = stringPreferencesKey("cost_preference")
        val SELECTED_MODEL_ID_KEY = stringPreferencesKey("selected_model_id")
    }

    // Dark mode settings
    val darkModeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: "system" // Default to system
    }

    suspend fun setDarkMode(darkMode: String) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = darkMode
        }
    }

    // Dynamic colors setting
    val useDynamicColorsFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLORS_KEY] ?: true // Default to true
    }

    suspend fun setUseDynamicColors(useDynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS_KEY] = useDynamicColors
        }
    }

    // Default AI model
    val defaultAiModelFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[DEFAULT_AI_MODEL_KEY] ?: "Gemini" // Default to Gemini
    }

    suspend fun setDefaultAiModel(model: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_AI_MODEL_KEY] = model
        }
    }

    // Default language
    val defaultLanguageFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[DEFAULT_LANGUAGE_KEY] ?: "English" // Default to English
    }

    suspend fun setDefaultLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_LANGUAGE_KEY] = language
        }
    }

    // Pinned cards
    fun getPinnedCards(): Flow<List<String>> = dataStore.data.map { preferences ->
        val pinnedCardsString = preferences[PINNED_CARDS_KEY] ?: ""
        if (pinnedCardsString.isEmpty()) {
            emptyList()
        } else {
            pinnedCardsString.split(",")
        }
    }

    suspend fun savePinnedCards(cardIds: List<String>) {
        dataStore.edit { preferences ->
            preferences[PINNED_CARDS_KEY] = cardIds.joinToString(",")
        }
    }

    // Cost preference
    suspend fun getCostPreference(): com.secondbrain.data.model.ai.CostPreference? {
        // Default to PREFER_FREE if not set
        return com.secondbrain.data.model.ai.CostPreference.PREFER_FREE
    }

    suspend fun saveCostPreference(preference: com.secondbrain.data.model.ai.CostPreference) {
        dataStore.edit { preferences ->
            preferences[COST_PREFERENCE_KEY] = preference.name
        }
    }

    // Selected model ID
    suspend fun getSelectedModelId(): String? {
        // Return null by default
        return null
    }

    suspend fun saveSelectedModelId(modelId: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_MODEL_ID_KEY] = modelId
        }
    }

    // OpenRouter API key
    suspend fun getOpenRouterApiKey(): String {
        return com.secondbrain.util.SecureStorage(context).getString(com.secondbrain.util.SecureStorage.KEY_OPENROUTER_API_KEY)
    }
}
