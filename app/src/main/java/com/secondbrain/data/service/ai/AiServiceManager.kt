package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.util.PdfContent
import com.secondbrain.util.UrlContent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for AI services that coordinates between different AI providers
 */
@Singleton
class AiServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val geminiProvider: GeminiProvider,
    private val openAiProvider: OpenAiProvider,
    private val claudeProvider: ClaudeProvider,
    private val deepSeekProvider: DeepSeekProvider,
    private val openRouterProvider: OpenRouterProvider
) {
    // Map of provider names to provider instances
    private val providers = mapOf(
        geminiProvider.name to geminiProvider,
        openAiProvider.name to openAiProvider,
        claudeProvider.name to claudeProvider,
        deepSeekProvider.name to deepSeekProvider,
        openRouterProvider.name to openRouterProvider
    )

    /**
     * Get all available AI providers
     */
    fun getAvailableProviders(): List<AiProvider> {
        return providers.values.toList()
    }

    /**
     * Get a provider by name
     */
    fun getProvider(name: String): AiProvider? {
        return providers[name]
    }

    /**
     * Get the default provider based on settings
     */
    suspend fun getDefaultProvider(): AiProvider {
        val defaultModel = settingsRepository.defaultAiModelFlow.first()
        return when {
            defaultModel.contains("Gemini", ignoreCase = true) -> geminiProvider
            defaultModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
            defaultModel.contains("Claude", ignoreCase = true) -> claudeProvider
            defaultModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
            defaultModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
            else -> geminiProvider // Default to Gemini
        }
    }

    /**
     * Summarize content using the specified or default AI provider
     */
    suspend fun summarize(
        content: String,
        options: SummarizationOptions,
        aiModel: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Determine which provider to use
            val provider = if (aiModel != null) {
                when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> getDefaultProvider()
                }
            } else {
                getDefaultProvider()
            }

            // Call the provider
            provider.summarizeText(content, options)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Transcribe audio content
     */
    suspend fun transcribeAudio(
        audioUri: Uri,
        language: String,
        aiModel: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Determine which provider to use
            val provider = if (aiModel != null) {
                when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> getDefaultProvider()
                }
            } else {
                getDefaultProvider()
            }

            // Create options
            val options = TranscriptionOptions(
                language = language
            )

            // Call the provider
            provider.transcribeAudio(audioUri, options)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract text from an image
     */
    suspend fun extractTextFromImage(
        imageUri: Uri,
        language: String,
        aiModel: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Determine which provider to use
            val provider = if (aiModel != null) {
                when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> getDefaultProvider()
                }
            } else {
                getDefaultProvider()
            }

            // Create options
            val options = ExtractionOptions(
                language = language
            )

            // Call the provider
            provider.extractTextFromImage(imageUri, options)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate tags from content
     */
    suspend fun generateTags(
        content: String,
        language: String,
        maxTags: Int = 5,
        aiModel: String? = null
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            // Determine which provider to use
            val provider = if (aiModel != null) {
                when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> getDefaultProvider()
                }
            } else {
                getDefaultProvider()
            }

            // Create options
            val options = TagGenerationOptions(
                maxTags = maxTags,
                language = language
            )

            // Call the provider
            provider.generateTags(content, options)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a title from content
     */
    suspend fun generateTitle(
        content: String,
        language: String,
        aiModel: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Determine which provider to use
            val provider = if (aiModel != null) {
                when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> getDefaultProvider()
                }
            } else {
                getDefaultProvider()
            }

            // Create options
            val options = TitleGenerationOptions(
                language = language
            )

            // Call the provider
            provider.generateTitle(content, options)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convenience method for backward compatibility
     */
    suspend fun summarize(
        content: String,
        summaryType: String,
        language: String,
        aiModel: String? = null,
        customInstructions: String? = null
    ): Result<String> {
        // Convert summary type string to enum
        val summaryTypeEnum = when (summaryType) {
            "Concise summary" -> SummaryType.CONCISE
            "Detailed summary" -> SummaryType.DETAILED
            "Bullet points" -> SummaryType.BULLET_POINTS
            "Question & Answer", "Question and answer" -> SummaryType.QUESTION_ANSWER
            "Key facts" -> SummaryType.KEY_FACTS
            else -> SummaryType.CONCISE
        }

        // Create options
        val options = SummarizationOptions(
            summaryType = summaryTypeEnum,
            language = language,
            customInstructions = customInstructions
        )

        return summarize(content, options, aiModel)
    }
}
