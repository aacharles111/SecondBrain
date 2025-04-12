package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.secondbrain.util.PdfContent
import com.secondbrain.util.UrlContent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of AiProvider that delegates to the appropriate provider based on settings
 */
@Singleton
class DefaultAiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAiProvider: OpenAiProvider,
    private val geminiProvider: GeminiProvider,
    private val claudeProvider: ClaudeProvider,
    private val deepSeekProvider: DeepSeekProvider,
    private val openRouterProvider: OpenRouterProvider
) : AiProvider {

    companion object {
        private const val TAG = "DefaultAiProvider"
    }

    override val name: String = "Default"

    override val availableModels: List<AiModel>
        get() = openRouterProvider.availableModels

    override fun isConfigured(): Boolean {
        // Check if at least one provider is configured
        return openAiProvider.isConfigured() ||
               geminiProvider.isConfigured() ||
               claudeProvider.isConfigured() ||
               deepSeekProvider.isConfigured() ||
               openRouterProvider.isConfigured()
    }

    override suspend fun summarizeText(
        content: String,
        options: SummarizationOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Summarizing text with default provider")

            // Try providers in order of preference
            val providers = listOf(
                openRouterProvider,
                openAiProvider,
                claudeProvider,
                geminiProvider,
                deepSeekProvider
            )

            // Find the first configured provider
            for (provider in providers) {
                if (provider.isConfigured()) {
                    Log.d(TAG, "Using ${provider.name} for summarization")
                    return@withContext provider.summarizeText(content, options)
                }
            }

            return@withContext Result.failure(Exception("No AI provider is configured"))
        } catch (e: Exception) {
            Log.e(TAG, "Error summarizing text with default provider", e)
            Result.failure(e)
        }
    }

    override suspend fun extractTextFromImage(
        imageUri: Uri,
        options: ExtractionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Extracting text from image with default provider")

            // Try providers in order of preference
            val providers = listOf(
                openRouterProvider,
                openAiProvider,
                claudeProvider,
                geminiProvider
            )

            // Find the first configured provider
            for (provider in providers) {
                if (provider.isConfigured()) {
                    Log.d(TAG, "Using ${provider.name} for image text extraction")
                    return@withContext provider.extractTextFromImage(imageUri, options)
                }
            }

            return@withContext Result.failure(Exception("No AI provider is configured for image processing"))
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from image with default provider", e)
            Result.failure(e)
        }
    }

    override suspend fun transcribeAudio(
        audioUri: Uri,
        options: TranscriptionOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Transcribing audio with default provider")

            // Try providers in order of preference
            val providers = listOf(
                openAiProvider,
                openRouterProvider,
                geminiProvider
            )

            // Find the first configured provider
            for (provider in providers) {
                if (provider.isConfigured()) {
                    Log.d(TAG, "Using ${provider.name} for audio transcription")
                    return@withContext provider.transcribeAudio(audioUri, options)
                }
            }

            return@withContext Result.failure(Exception("No AI provider is configured for audio transcription"))
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio with default provider", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTags(
        content: String,
        options: TagGenerationOptions
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating tags with default provider")

            // Try providers in order of preference
            val providers = listOf(
                openRouterProvider,
                openAiProvider,
                claudeProvider,
                geminiProvider,
                deepSeekProvider
            )

            // Find the first configured provider
            for (provider in providers) {
                if (provider.isConfigured()) {
                    Log.d(TAG, "Using ${provider.name} for tag generation")
                    return@withContext provider.generateTags(content, options)
                }
            }

            return@withContext Result.failure(Exception("No AI provider is configured"))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tags with default provider", e)
            Result.failure(e)
        }
    }

    override suspend fun generateTitle(
        content: String,
        options: TitleGenerationOptions
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating title with default provider")

            // Try providers in order of preference
            val providers = listOf(
                openRouterProvider,
                openAiProvider,
                claudeProvider,
                geminiProvider,
                deepSeekProvider
            )

            // Find the first configured provider
            for (provider in providers) {
                if (provider.isConfigured()) {
                    Log.d(TAG, "Using ${provider.name} for title generation")
                    return@withContext provider.generateTitle(content, options)
                }
            }

            return@withContext Result.failure(Exception("No AI provider is configured"))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating title with default provider", e)
            Result.failure(e)
        }
    }
}
