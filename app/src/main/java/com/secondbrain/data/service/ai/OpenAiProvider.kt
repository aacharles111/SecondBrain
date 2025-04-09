package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.ai.api.OpenAiApiClient
import com.secondbrain.util.SecureStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AiProvider for OpenAI
 */
@Singleton
class OpenAiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val secureStorage: SecureStorage
) : AiProvider {

    companion object {
        private const val TAG = "OpenAiProvider"
    }

    override val name: String = "OpenAI"

    override val availableModels: List<AiModel> = listOf(
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
