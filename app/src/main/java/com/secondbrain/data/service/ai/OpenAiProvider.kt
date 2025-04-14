package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.api.OpenAiApiClient
import com.secondbrain.data.service.ai.provider.OpenAiPromptFormatter
import com.secondbrain.util.SecureStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BaseAiProvider for OpenAI
 */
@Singleton
class OpenAiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val secureStorage: SecureStorage
) : BaseAiProvider {

    companion object {
        private const val TAG = "OpenAiProvider"
    }

    override val name: String = "OpenAI"

    override val systemPromptFormat: SystemPromptHandler.SystemPromptFormat =
        SystemPromptHandler.SystemPromptFormat.DEDICATED_SYSTEM_MESSAGE

    /**
     * Process a system prompt according to OpenAI's requirements
     * For OpenAI, we use a dedicated system message
     */
    override suspend fun processSystemPrompt(systemPrompt: String?, userPrompt: String): Any {
        // Use the OpenAiPromptFormatter to format the prompt
        return OpenAiPromptFormatter.formatPrompt(systemPrompt, userPrompt)
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
            id = "gpt-4o",
            name = "GPT-4o",
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
            id = "gpt-4-turbo",
            name = "GPT-4 Turbo",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 128000,
            contextWindow = 128000
        ),
        AiModel(
            id = "gpt-3.5-turbo",
            name = "GPT-3.5 Turbo",
            capabilities = setOf(
                ModelCapability.TEXT_SUMMARIZATION,
                ModelCapability.TAG_GENERATION,
                ModelCapability.TITLE_GENERATION
            ),
            maxTokens = 16385,
            contextWindow = 16385
        ),
        AiModel(
            id = "whisper-1",
            name = "Whisper",
            capabilities = setOf(
                ModelCapability.AUDIO_TRANSCRIPTION
            ),
            maxTokens = 0,
            contextWindow = 0
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
    private val apiClient = OpenAiApiClient()

    // Get API key from secure storage
    private suspend fun getApiKey(): String? {
        return secureStorage.getString(SecureStorage.KEY_OPENAI_API_KEY)
    }

    override fun isConfigured(): Boolean {
        return secureStorage.containsKey(SecureStorage.KEY_OPENAI_API_KEY) &&
               secureStorage.getString(SecureStorage.KEY_OPENAI_API_KEY).isNotEmpty()
    }

    /**
     * Fetch all available models from OpenAI
     */
    suspend fun fetchAvailableModels(): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching available models from OpenAI")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenAI API key not configured"))
            }

            // Call the OpenAI API client
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
            Log.e(TAG, "Error fetching available models from OpenAI", e)
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
            Log.d(TAG, "Summarizing text with OpenAI: ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenAI API key not configured"))
            }

            // Call the OpenAI API client
            apiClient.summarizeText(content, options, apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error summarizing text with OpenAI", e)
            Result.failure(e)
        }
    }

    override suspend fun transcribeAudio(
        audioUri: Uri,
        options: TranscriptionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Transcribing audio with OpenAI: $audioUri")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenAI API key not configured"))
            }

            // Convert Uri to File
            val inputStream = context.contentResolver.openInputStream(audioUri)
                ?: return@withContext Result.failure(Exception("Could not open audio file"))

            val tempFile = File.createTempFile("audio", ".mp3", context.cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Call the OpenAI API client
            val result = apiClient.transcribeAudio(tempFile, options, apiKey)

            // Delete the temp file
            tempFile.delete()

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio with OpenAI", e)
            Result.failure(e)
        }
    }

    override suspend fun extractTextFromImage(
        imageUri: Uri,
        options: ExtractionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Extracting text from image with OpenAI: $imageUri")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenAI API key not configured"))
            }

            // Call the OpenAI API client
            apiClient.extractTextFromImage(imageUri, options, apiKey, context)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from image with OpenAI", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTags(
        content: String,
        options: TagGenerationOptions
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating tags with OpenAI: ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenAI API key not configured"))
            }

            // Call the OpenAI API client
            apiClient.generateTags(content, options, apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tags with OpenAI", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTitle(
        content: String,
        options: TitleGenerationOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating title with OpenAI: ${content.take(100)}...")

            val apiKey = getApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("OpenAI API key not configured"))
            }

            // Call the OpenAI API client
            apiClient.generateTitle(content, options, apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating title with OpenAI", e)
            Result.failure(e)
        }
    }
}
