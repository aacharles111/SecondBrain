package com.secondbrain.data.service.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.secondbrain.data.model.CardType
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.repository.SystemPromptRepository
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
    private val systemPromptRepository: SystemPromptRepository,
    private val geminiProvider: GeminiProvider,
    private val openAiProvider: OpenAiProvider,
    private val claudeProvider: ClaudeProvider,
    private val deepSeekProvider: DeepSeekProvider,
    private val openRouterProvider: OpenRouterProvider
) {

    companion object {
        private const val TAG = "AiServiceManager"
    }
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

        // First try to get the provider based on the model name
        val provider = when {
            defaultModel.contains("Gemini", ignoreCase = true) -> geminiProvider
            defaultModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
            defaultModel.contains("Claude", ignoreCase = true) -> claudeProvider
            defaultModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
            defaultModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
            else -> null
        }

        // If the provider is configured, return it
        if (provider != null && provider.isConfigured()) {
            return provider
        }

        // Otherwise, find the first configured provider
        return providers.values.firstOrNull { it.isConfigured() }
            ?: throw Exception("No AI provider is configured. Please add an API key in Settings.")
    }

    /**
     * Summarize content using the specified or default AI provider
     */
    suspend fun summarize(
        content: String,
        options: SummarizationOptions,
        aiModel: String? = null,
        contentType: CardType? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Log the requested AI model
            Log.d(TAG, "Summarizing with AI model: $aiModel")

            // Determine which provider to use
            val provider = if (aiModel != null) {
                // Try to get the specified provider
                val selectedProvider = when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> null
                }

                // If the provider is configured, use it
                if (selectedProvider != null && selectedProvider.isConfigured()) {
                    // Log the selected provider
                    Log.d(TAG, "Using provider: ${selectedProvider.name}")
                    selectedProvider
                } else {
                    // Otherwise, find any configured provider
                    Log.d(TAG, "Requested provider not configured, finding alternative")
                    providers.values.firstOrNull { it.isConfigured() }
                        ?: throw Exception("No AI provider is configured. Please add an API key in Settings.")
                }
            } else {
                getDefaultProvider()
            }

            // For OpenRouter, make sure we're using the selected model
            if (provider is OpenRouterProvider) {
                Log.d(TAG, "Using OpenRouter with model: ${provider.selectedModel.value?.name ?: "default"}")
            }

            // Get the appropriate system prompt if not already provided
            val optionsWithSystemPrompt = if (options.systemPrompt == null) {
                // Check if this is YouTube content
                val isYouTube = contentType == CardType.URL &&
                    content.contains("YouTube Video:") &&
                    (content.contains("Video ID:") || content.contains("Transcript:"))

                val systemPrompt = if (isYouTube) {
                    // Use YouTube-specific prompt
                    Log.d(TAG, "Using YouTube-specific prompt for ${options.summaryType.name}")
                    // Create a temporary CardType for YouTube
                    val youtubeType = CardType.URL
                    systemPromptRepository.getSystemPrompt(
                        youtubeType,
                        options.summaryType
                    ).first()
                } else if (contentType != null) {
                    // Use regular content type prompt
                    Log.d(TAG, "Using system prompt for ${contentType.name} and ${options.summaryType.name}")
                    systemPromptRepository.getSystemPrompt(
                        contentType,
                        options.summaryType
                    ).first()
                } else {
                    // Fallback to default prompt
                    null
                }

                // Create new options with the system prompt
                options.copy(systemPrompt = systemPrompt)
            } else {
                options
            }

            // Call the provider
            provider.summarizeText(content, optionsWithSystemPrompt)
        } catch (e: Exception) {
            Log.e(TAG, "Error summarizing content", e)
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
        maxTags: Int = 15,
        aiModel: String? = null
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            // Log the requested AI model
            Log.d(TAG, "Generating tags with AI model: $aiModel")

            // Determine which provider to use
            val provider = if (aiModel != null) {
                // Try to get the specified provider
                val selectedProvider = when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> null
                }

                // If the provider is configured, use it
                if (selectedProvider != null && selectedProvider.isConfigured()) {
                    // Log the selected provider
                    Log.d(TAG, "Using provider: ${selectedProvider.name}")
                    selectedProvider
                } else {
                    // Otherwise, find any configured provider
                    Log.d(TAG, "Requested provider not configured, finding alternative")
                    providers.values.firstOrNull { it.isConfigured() }
                        ?: throw Exception("No AI provider is configured. Please add an API key in Settings.")
                }
            } else {
                getDefaultProvider()
            }

            // For OpenRouter, make sure we're using the selected model
            if (provider is OpenRouterProvider) {
                Log.d(TAG, "Using OpenRouter with model: ${provider.selectedModel.value?.name ?: "default"}")
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
            // Log the requested AI model
            Log.d(TAG, "Generating title with AI model: $aiModel")

            // Determine which provider to use
            val provider = if (aiModel != null) {
                // Try to get the specified provider
                val selectedProvider = when {
                    aiModel.contains("Gemini", ignoreCase = true) -> geminiProvider
                    aiModel.contains("OpenAI", ignoreCase = true) -> openAiProvider
                    aiModel.contains("Claude", ignoreCase = true) -> claudeProvider
                    aiModel.contains("DeepSeek", ignoreCase = true) -> deepSeekProvider
                    aiModel.contains("OpenRouter", ignoreCase = true) -> openRouterProvider
                    else -> null
                }

                // If the provider is configured, use it
                if (selectedProvider != null && selectedProvider.isConfigured()) {
                    // Log the selected provider
                    Log.d(TAG, "Using provider: ${selectedProvider.name}")
                    selectedProvider
                } else {
                    // Otherwise, find any configured provider
                    Log.d(TAG, "Requested provider not configured, finding alternative")
                    providers.values.firstOrNull { it.isConfigured() }
                        ?: throw Exception("No AI provider is configured. Please add an API key in Settings.")
                }
            } else {
                getDefaultProvider()
            }

            // For OpenRouter, make sure we're using the selected model
            if (provider is OpenRouterProvider) {
                Log.d(TAG, "Using OpenRouter with model: ${provider.selectedModel.value?.name ?: "default"}")
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
        customInstructions: String? = null,
        contentType: CardType? = null
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

        return summarize(content, options, aiModel, contentType)
    }
}
