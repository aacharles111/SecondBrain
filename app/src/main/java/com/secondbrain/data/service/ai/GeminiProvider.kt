package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.api.GeminiApiClient
import com.secondbrain.data.service.ai.provider.GeminiPromptFormatter
import com.secondbrain.util.SecureStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BaseAiProvider for Google's Gemini AI
 */
@Singleton
class GeminiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val secureStorage: SecureStorage
) : BaseAiProvider {

    companion object {
        private const val TAG = "GeminiProvider"
    }

    override val name: String = "Gemini"

    override val systemPromptFormat: SystemPromptHandler.SystemPromptFormat =
        SystemPromptHandler.SystemPromptFormat.CONVERSATION_BASED

    /**
     * Process a system prompt according to Gemini's requirements
     * For Gemini, we use a conversation-based approach
     */
    override suspend fun processSystemPrompt(systemPrompt: String?, userPrompt: String): Any {
        // Use the GeminiPromptFormatter to format the prompt
        return GeminiPromptFormatter.formatPrompt(systemPrompt, userPrompt)
    }

    // The getApiKey method is already implemented in this class

    /**
     * Get the selected model ID for this provider
     */
    override suspend fun getSelectedModelId(): String? {
        return _selectedModel.value?.id
    }

    // Default models to use if API call fails
    private val defaultModels = listOf(
        AiModel(
            id = "gemini-1.5-pro",
            name = "Gemini 1.5 Pro",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION,
                ModelCapability.IMAGE_UNDERSTANDING
            ),
            maxTokens = 32768,
            contextWindow = 1000000
        ),
        AiModel(
            id = "gemini-1.5-flash",
            name = "Gemini 1.5 Flash",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.IMAGE_UNDERSTANDING,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 32768,
            contextWindow = 1000000
        ),
        AiModel(
            id = "gemini-1.0-pro",
            name = "Gemini 1.0 Pro",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 32768,
            contextWindow = 32768
        ),
        AiModel(
            id = "gemini-1.0-pro-vision",
            name = "Gemini 1.0 Pro Vision",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.IMAGE_UNDERSTANDING,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 32768,
            contextWindow = 32768
        )
    )

    // All available models (will be populated from API)
    private val _allModels = MutableStateFlow<List<AiModel>>(defaultModels)
    val allModels: StateFlow<List<AiModel>> = _allModels.asStateFlow()

    // Currently selected model
    private val _selectedModel = MutableStateFlow<AiModel?>(defaultModels.firstOrNull())
    val selectedModel: StateFlow<AiModel?> = _selectedModel.asStateFlow()

    override val availableModels: List<AiModel>
        get() = _allModels.value

    // API client
    private val apiClient = GeminiApiClient()

    // Get API key from secure storage
    private suspend fun getApiKey(): String? {
        return secureStorage.getString(SecureStorage.KEY_GEMINI_API_KEY)
    }

    override fun isConfigured(): Boolean {
        return secureStorage.containsKey(SecureStorage.KEY_GEMINI_API_KEY) &&
               secureStorage.getString(SecureStorage.KEY_GEMINI_API_KEY).isNotEmpty()
    }

    /**
     * Fetch all available models from Gemini
     */
    suspend fun fetchAvailableModels(): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching available models from Gemini")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            // Call the Gemini API client
            val result = apiClient.fetchAvailableModels(apiKey)

            if (result.isSuccess) {
                val models = result.getOrNull() ?: emptyList()
                _allModels.value = models
                return@withContext Result.success(models)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Unknown error fetching models")
                return@withContext Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching available models from Gemini", e)
            Result.failure(e)
        }
    }

    /**
     * Set the selected model
     */
    fun setSelectedModel(model: AiModel) {
        _selectedModel.value = model
    }

    override suspend fun summarizeText(
        content: String,
        options: SummarizationOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Summarizing text with Gemini: ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            // Get the selected model ID
            val selectedModelId = _selectedModel.value?.id

            // Call the Gemini API client with the selected model
            apiClient.summarizeText(content, options, apiKey, selectedModelId)
        } catch (e: Exception) {
            Log.e(TAG, "Error summarizing text with Gemini", e)
            Result.failure(e)
        }
    }

    override suspend fun transcribeAudio(
        audioUri: Uri,
        options: TranscriptionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Transcribing audio with Gemini: $audioUri")

            // In a real implementation, this would call the Gemini API
            // For now, we'll just return a placeholder transcription
            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            // Simulate API call
            val transcription = "This is a transcription of the audio file. It contains the spoken content converted to text format. [Generated by Gemini AI]"

            Result.success(transcription)
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio with Gemini", e)
            Result.failure(e)
        }
    }

    override suspend fun extractTextFromImage(
        imageUri: Uri,
        options: ExtractionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Extracting text from image with Gemini: $imageUri")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            // Get the selected model ID
            val selectedModelId = _selectedModel.value?.id

            // Call the Gemini API client with the selected model
            apiClient.extractTextFromImage(imageUri, options, apiKey, context, selectedModelId)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from image with Gemini", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTags(
        content: String,
        options: TagGenerationOptions
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating tags with Gemini: ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            // Get the selected model ID
            val selectedModelId = _selectedModel.value?.id

            // Call the Gemini API client with the selected model
            apiClient.generateTags(content, options, apiKey, selectedModelId)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tags with Gemini", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTitle(
        content: String,
        options: TitleGenerationOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating title with Gemini: ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            // Get the selected model ID
            val selectedModelId = _selectedModel.value?.id

            // Call the Gemini API client with the selected model
            apiClient.generateTitle(content, options, apiKey, selectedModelId)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating title with Gemini", e)
            Result.failure(e)
        }
    }
}
