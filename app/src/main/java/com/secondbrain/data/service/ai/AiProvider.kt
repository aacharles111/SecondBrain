package com.secondbrain.data.service.ai

import android.net.Uri
import com.secondbrain.util.PdfContent
import com.secondbrain.util.UrlContent

/**
 * Interface for AI providers (Gemini, OpenAI, Claude, etc.)
 */
interface AiProvider {
    /**
     * Get the name of the AI provider
     */
    val name: String

    /**
     * Get the available models for this provider
     */
    val availableModels: List<AiModel>

    /**
     * Check if the provider is configured with valid API keys
     */
    fun isConfigured(): Boolean

    /**
     * Summarize text content
     *
     * @param content The text content to summarize
     * @param options The summarization options
     * @return A Result containing the summary or an error
     */
    suspend fun summarizeText(
        content: String,
        options: SummarizationOptions
    ): Result<String>

    /**
     * Transcribe audio content
     *
     * @param audioUri The URI of the audio file
     * @param options The transcription options
     * @return A Result containing the transcription or an error
     */
    suspend fun transcribeAudio(
        audioUri: Uri,
        options: TranscriptionOptions
    ): Result<String>

    /**
     * Extract text from an image
     *
     * @param imageUri The URI of the image file
     * @param options The extraction options
     * @return A Result containing the extracted text or an error
     */
    suspend fun extractTextFromImage(
        imageUri: Uri,
        options: ExtractionOptions
    ): Result<String>

    /**
     * Generate tags from content
     *
     * @param content The content to generate tags from
     * @param options The tag generation options
     * @return A Result containing the generated tags or an error
     */
    suspend fun generateTags(
        content: String,
        options: TagGenerationOptions
    ): Result<List<String>>

    /**
     * Generate a title from content
     *
     * @param content The content to generate a title from
     * @param options The title generation options
     * @return A Result containing the generated title or an error
     */
    suspend fun generateTitle(
        content: String,
        options: TitleGenerationOptions
    ): Result<String>
}

/**
 * Data class representing an AI model
 */
data class AiModel(
    val id: String,
    val name: String,
    val capabilities: Set<ModelCapability>,
    val maxTokens: Int,
    val contextWindow: Int
)

/**
 * Enum representing AI model capabilities
 */
enum class ModelCapability {
    // Content types
    TEXT_CONTENT,           // Plain text content
    IMAGE_UNDERSTANDING,    // Image analysis
    AUDIO_TRANSCRIPTION,    // Audio transcription
    PDF_PROCESSING,         // PDF document processing
    WEB_CONTENT,            // Web links and content

    // Features
    TEXT_SUMMARIZATION,     // Text summarization
    TAG_GENERATION,         // Tag generation
    TITLE_GENERATION,       // Title generation
    CODE_UNDERSTANDING      // Code analysis and understanding
}

/**
 * Data class for summarization options
 */
data class SummarizationOptions(
    val summaryType: SummaryType,
    val language: String,
    val maxLength: Int? = null,
    val customInstructions: String? = null,
    val systemPrompt: String? = null
)

/**
 * Enum representing summary types
 */
enum class SummaryType(val displayName: String) {
    CONCISE("Concise summary"),
    DETAILED("Detailed summary"),
    BULLET_POINTS("Bullet points"),
    QUESTION_ANSWER("Question & Answer"),
    KEY_FACTS("Key facts")
}

/**
 * Data class for transcription options
 */
data class TranscriptionOptions(
    val language: String,
    val prompt: String? = null,
    val timestamped: Boolean = false
)

/**
 * Data class for text extraction options
 */
data class ExtractionOptions(
    val language: String,
    val extractTables: Boolean = false,
    val extractDiagrams: Boolean = false
)

/**
 * Data class for tag generation options
 */
data class TagGenerationOptions(
    val maxTags: Int = 5,
    val language: String
)

/**
 * Data class for title generation options
 */
data class TitleGenerationOptions(
    val maxLength: Int = 100,
    val language: String
)
