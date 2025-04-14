package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.provider.OpenRouterPromptFormatter
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
 * Implementation of BaseAiProvider for OpenRouter
 * OpenRouter provides access to multiple AI models from different providers
 */
@Singleton
class OpenRouterProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val secureStorage: SecureStorage
) : BaseAiProvider {

    companion object {
        private const val TAG = "OpenRouterProvider"
    }

    override val name: String = "OpenRouter"

    override val systemPromptFormat: SystemPromptHandler.SystemPromptFormat =
        SystemPromptHandler.SystemPromptFormat.DEDICATED_SYSTEM_MESSAGE

    /**
     * Process a system prompt according to OpenRouter's requirements
     * For OpenRouter, we use a dedicated system message, but the exact format
     * depends on the underlying model being used
     */
    override suspend fun processSystemPrompt(systemPrompt: String?, userPrompt: String): Any {
        // Get the selected model ID
        val modelId = getSelectedModelId() ?: "openai/gpt-4"

        // Use the OpenRouterPromptFormatter to format the prompt
        return OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)
    }

    // Default models available through OpenRouter
    override val availableModels: List<AiModel> = listOf(
        AiModel(
            id = "anthropic/claude-3-opus",
            name = "Claude 3 Opus (via OpenRouter)",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.IMAGE_UNDERSTANDING,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 200000,
            contextWindow = 200000
        ),
        AiModel(
            id = "anthropic/claude-3-sonnet",
            name = "Claude 3 Sonnet (via OpenRouter)",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.IMAGE_UNDERSTANDING,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 200000,
            contextWindow = 200000
        ),
        AiModel(
            id = "google/gemini-pro",
            name = "Gemini Pro (via OpenRouter)",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 32768,
            contextWindow = 32768
        ),
        AiModel(
            id = "openai/gpt-4o",
            name = "GPT-4o (via OpenRouter)",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.IMAGE_UNDERSTANDING,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 128000,
            contextWindow = 128000
        ),
        AiModel(
            id = "meta-llama/llama-3-70b-instruct",
            name = "Llama 3 70B (via OpenRouter)",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 32768,
            contextWindow = 32768
        ),
        AiModel(
            id = "mistralai/mistral-large",
            name = "Mistral Large (via OpenRouter)",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 32768,
            contextWindow = 32768
        )
    )

    // All available models from OpenRouter
    private val _allModels = MutableStateFlow<List<AiModel>>(availableModels)
    val allModels: StateFlow<List<AiModel>> = _allModels.asStateFlow()

    // Currently selected model
    private val _selectedModel = MutableStateFlow<AiModel?>(availableModels.firstOrNull())
    val selectedModel: StateFlow<AiModel?> = _selectedModel.asStateFlow()

    // Get API key from secure storage
    private suspend fun getApiKey(): String? {
        return secureStorage.getString(SecureStorage.KEY_OPENROUTER_API_KEY)
    }

    override fun isConfigured(): Boolean {
        return secureStorage.containsKey(SecureStorage.KEY_OPENROUTER_API_KEY) &&
               secureStorage.getString(SecureStorage.KEY_OPENROUTER_API_KEY).isNotEmpty()
    }

    /**
     * Set the selected model
     */
    fun setSelectedModel(model: AiModel) {
        _selectedModel.value = model
    }

    /**
     * Fetch all available models from OpenRouter
     */
    suspend fun fetchAvailableModels(): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching available models from OpenRouter")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenRouter API key not configured"))
            }

            // Create API client
            val apiClient = com.secondbrain.data.service.ai.api.OpenRouterApiClient()

            // Call the API to get models
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
            Log.e(TAG, "Error fetching available models from OpenRouter", e)
            Result.failure(e)
        }
    }



    override suspend fun summarizeText(
        content: String,
        options: SummarizationOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = selectedModel.value ?: availableModels.first()
            Log.d(TAG, "Summarizing text with OpenRouter (${model.name}): ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenRouter API key not configured"))
            }

            // Create API client
            val apiClient = com.secondbrain.data.service.ai.api.OpenRouterApiClient()

            // Call the API to summarize text
            apiClient.summarizeText(content, options, apiKey, model.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error summarizing text with OpenRouter", e)
            Result.failure(e)
        }
    }

    override suspend fun transcribeAudio(
        audioUri: Uri,
        options: TranscriptionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = selectedModel.value ?: availableModels.first()
            Log.d(TAG, "Transcribing audio with OpenRouter (${model.name}): $audioUri")

            // Check if the selected model supports audio transcription
            if (!model.capabilities.contains(ModelCapability.AUDIO_TRANSCRIPTION)) {
                return@withContext Result.failure(Exception("Selected model does not support audio transcription"))
            }

            // In a real implementation, this would call the OpenRouter API
            // For now, we'll just return a placeholder transcription
            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenRouter API key not configured"))
            }

            // Simulate API call
            val transcription = "This is a transcription of the audio file using ${model.name} via OpenRouter. The transcription captures the spoken content accurately, including proper punctuation and speaker identification where possible."

            Result.success(transcription)
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio with OpenRouter", e)
            Result.failure(e)
        }
    }

    override suspend fun extractTextFromImage(
        imageUri: Uri,
        options: ExtractionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = selectedModel.value ?: availableModels.first()
            Log.d(TAG, "Extracting text from image with OpenRouter (${model.name}): $imageUri")

            // Check if the selected model supports image understanding
            if (!model.capabilities.contains(ModelCapability.IMAGE_UNDERSTANDING)) {
                return@withContext Result.failure(Exception("Selected model does not support image understanding"))
            }

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenRouter API key not configured"))
            }

            // Create API client
            val apiClient = com.secondbrain.data.service.ai.api.OpenRouterApiClient()

            // Call the API to extract text from image
            apiClient.extractTextFromImage(imageUri, options, apiKey, model.id, context)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from image with OpenRouter", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTags(
        content: String,
        options: TagGenerationOptions
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val model = selectedModel.value ?: availableModels.first()
            Log.d(TAG, "Generating tags with OpenRouter (${model.name}): ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenRouter API key not configured"))
            }

            // Create API client
            val apiClient = com.secondbrain.data.service.ai.api.OpenRouterApiClient()

            // Call the API to generate tags
            apiClient.generateTags(content, options, apiKey, model.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tags with OpenRouter", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTitle(
        content: String,
        options: TitleGenerationOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = selectedModel.value ?: availableModels.first()
            Log.d(TAG, "Generating title with OpenRouter (${model.name}): ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenRouter API key not configured"))
            }

            // Create API client
            val apiClient = com.secondbrain.data.service.ai.api.OpenRouterApiClient()

            // Call the API to generate title
            apiClient.generateTitle(content, options, apiKey, model.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating title with OpenRouter", e)
            Result.failure(e)
        }
    }

    /**
     * Get the selected model ID for this provider
     */
    override suspend fun getSelectedModelId(): String? {
        return settingsRepository.getSelectedOpenRouterModel()
    }
}
