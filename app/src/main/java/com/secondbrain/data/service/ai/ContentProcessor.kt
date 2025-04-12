package com.secondbrain.data.service.ai

import android.util.Log
import com.secondbrain.data.model.ai.ContentType
import com.secondbrain.data.model.ai.Feature
import com.secondbrain.data.model.ai.ModelCapability
import com.secondbrain.data.service.ai.api.OpenRouterApiClient
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processes content with AI models, handling chunking for large content
 */
@Singleton
class ContentProcessor @Inject constructor(
    private val openRouterApiClient: OpenRouterApiClient
) {
    companion object {
        private const val TAG = "ContentProcessor"

        // Approximate tokens per character for different content types
        private const val TOKENS_PER_CHAR_TEXT = 0.25
        private const val TOKENS_PER_CHAR_CODE = 0.33
        private const val CHUNK_OVERLAP = 100 // Characters to overlap between chunks
    }

    /**
     * Process content with a selected model, handling chunking if needed
     */
    suspend fun processContent(
        content: String,
        contentType: ContentType,
        model: ModelCapability,
        apiKey: String,
        systemPrompt: String = "You are a helpful assistant that summarizes content accurately and concisely.",
        userPromptTemplate: String = "Please summarize the following content:\n\n{content}"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Preprocess content based on type
            val preprocessedContent = preprocessContent(content, contentType)

            // Check if content needs chunking
            if (needsChunking(preprocessedContent, model, contentType)) {
                Log.d(TAG, "Content too large for model, chunking...")
                return@withContext processInChunks(
                    content = preprocessedContent,
                    contentType = contentType,
                    model = model,
                    apiKey = apiKey,
                    systemPrompt = systemPrompt,
                    userPromptTemplate = userPromptTemplate
                )
            }

            // Process content directly
            Log.d(TAG, "Processing content with model ${model.name}")
            val userPrompt = userPromptTemplate.replace("{content}", preprocessedContent)

            // Create options with system prompt and user prompt
            val options = SummarizationOptions(
                summaryType = SummaryType.DETAILED,
                language = "English",
                maxLength = model.maxTokens / 2, // Use half of max tokens for output
                customInstructions = null,
                systemPrompt = systemPrompt
            )

            val result = openRouterApiClient.summarizeText(
                content = preprocessedContent,
                options = options,
                apiKey = apiKey,
                modelId = model.id
            )

            if (result.isFailure) {
                Log.e(TAG, "Error processing content with model ${model.name}", result.exceptionOrNull())
                return@withContext result
            }

            Result.success(result.getOrNull() ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error in content processing", e)
            Result.failure(e)
        }
    }

    /**
     * Check if content needs chunking based on model token limit
     */
    private fun needsChunking(content: String, model: ModelCapability, contentType: ContentType): Boolean {
        val tokensPerChar = if (contentType == ContentType.TEXT && content.contains("```")) {
            // If content contains code blocks, use code token ratio
            TOKENS_PER_CHAR_CODE
        } else {
            TOKENS_PER_CHAR_TEXT
        }

        val estimatedTokens = (content.length * tokensPerChar).toInt()
        val maxInputTokens = (model.maxTokens * 0.7).toInt() // Leave 30% for output

        return estimatedTokens > maxInputTokens
    }

    /**
     * Preprocess content based on content type
     */
    private fun preprocessContent(content: String, contentType: ContentType): String {
        return when (contentType) {
            ContentType.PDF -> cleanPdfContent(content)
            ContentType.WEB_LINK -> extractMainContent(content)
            ContentType.YOUTUBE -> optimizeTranscript(content)
            else -> content
        }
    }

    /**
     * Clean PDF content by removing extra whitespace and formatting issues
     */
    private fun cleanPdfContent(content: String): String {
        return content
            .replace(Regex("\\s+"), " ") // Replace multiple whitespace with single space
            .replace(Regex("- (?=\\w)"), "") // Remove hyphenation
            .replace(Regex("\\n+"), "\n") // Replace multiple newlines with single newline
            .trim()
    }

    /**
     * Extract main content from web page content
     */
    private fun extractMainContent(content: String): String {
        // Simple extraction - in a real app, this would be more sophisticated
        return content.trim()
    }

    /**
     * Optimize YouTube transcript for processing
     */
    private fun optimizeTranscript(content: String): String {
        return content
            .replace(Regex("\\[.*?\\]"), "") // Remove timestamps and speaker labels
            .replace(Regex("\\s+"), " ") // Replace multiple whitespace with single space
            .trim()
    }

    /**
     * Process content in chunks and merge results
     */
    private suspend fun processInChunks(
        content: String,
        contentType: ContentType,
        model: ModelCapability,
        apiKey: String,
        systemPrompt: String,
        userPromptTemplate: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Split content into chunks
            val chunks = splitIntoChunks(content, model, contentType)
            Log.d(TAG, "Split content into ${chunks.size} chunks")

            // Process each chunk
            val chunkResults = mutableListOf<String>()

            for ((index, chunk) in chunks.withIndex()) {
                Log.d(TAG, "Processing chunk ${index + 1}/${chunks.size}")

                // Create chunk-specific prompts
                val chunkSystemPrompt = if (chunks.size > 1) {
                    "$systemPrompt\nThis is part ${index + 1} of ${chunks.size}. Focus on extracting the key information from this part."
                } else {
                    systemPrompt
                }

                val chunkUserPrompt = userPromptTemplate.replace("{content}", chunk)

                // Process chunk
                val chunkOptions = SummarizationOptions(
                    summaryType = SummaryType.DETAILED,
                    language = "English",
                    maxLength = model.maxTokens / 2, // Use half of max tokens for output
                    customInstructions = null,
                    systemPrompt = chunkSystemPrompt
                )

                val result = openRouterApiClient.summarizeText(
                    content = chunk,
                    options = chunkOptions,
                    apiKey = apiKey,
                    modelId = model.id
                )

                if (result.isFailure) {
                    Log.e(TAG, "Error processing chunk ${index + 1}", result.exceptionOrNull())
                    return@withContext result
                }

                val chunkOutput = result.getOrNull() ?: ""
                chunkResults.add(chunkOutput)
            }

            // If we only had one chunk, return its result directly
            if (chunkResults.size == 1) {
                return@withContext Result.success(chunkResults.first())
            }

            // Merge chunk results
            val mergedResult = mergeChunkResults(chunkResults, model, apiKey, systemPrompt)

            Result.success(mergedResult)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing content in chunks", e)
            Result.failure(e)
        }
    }

    /**
     * Split content into chunks that fit within model's token limit
     */
    private fun splitIntoChunks(content: String, model: ModelCapability, contentType: ContentType): List<String> {
        val tokensPerChar = if (contentType == ContentType.TEXT && content.contains("```")) {
            // If content contains code blocks, use code token ratio
            TOKENS_PER_CHAR_CODE
        } else {
            TOKENS_PER_CHAR_TEXT
        }

        val maxInputTokens = (model.maxTokens * 0.7).toInt() // Leave 30% for output
        val maxCharsPerChunk = (maxInputTokens / tokensPerChar).toInt()

        // If content fits in one chunk, return it
        if (content.length <= maxCharsPerChunk) {
            return listOf(content)
        }

        val chunks = mutableListOf<String>()
        var startIndex = 0

        while (startIndex < content.length) {
            // Calculate end index for this chunk
            var endIndex = minOf(startIndex + maxCharsPerChunk, content.length)

            // Try to end at a sentence or paragraph boundary if possible
            if (endIndex < content.length) {
                // Look for paragraph break
                val paragraphBreak = content.lastIndexOf("\n\n", endIndex)
                if (paragraphBreak > startIndex && paragraphBreak > endIndex - 200) {
                    endIndex = paragraphBreak + 2
                } else {
                    // Look for sentence break
                    val sentenceBreak = content.lastIndexOfAny(charArrayOf('.', '!', '?', '\n'), endIndex)
                    if (sentenceBreak > startIndex && sentenceBreak > endIndex - 100) {
                        endIndex = sentenceBreak + 1
                    }
                }
            }

            // Extract chunk
            val chunk = content.substring(startIndex, endIndex)
            chunks.add(chunk)

            // Move to next chunk, with overlap
            startIndex = maxOf(startIndex + 1, endIndex - CHUNK_OVERLAP)
        }

        return chunks
    }

    /**
     * Merge results from multiple chunks
     */
    private suspend fun mergeChunkResults(
        chunkResults: List<String>,
        model: ModelCapability,
        apiKey: String,
        systemPrompt: String
    ): String = withContext(Dispatchers.IO) {
        // If we have a small number of chunks, we can merge them directly
        if (chunkResults.size <= 3) {
            val combinedChunks = chunkResults.joinToString("\n\n--- Next Part ---\n\n")

            // Create merge prompt
            val mergeSystemPrompt = "You are a helpful assistant that combines multiple summaries into a single coherent summary. Eliminate redundancy and create a well-structured final summary."
            val mergeUserPrompt = "I have multiple summaries of different parts of a document. Please combine them into a single coherent summary, removing any redundancy and ensuring good flow between sections.\n\nHere are the summaries:\n\n$combinedChunks"

            // Process merge
            val mergeOptions = SummarizationOptions(
                summaryType = SummaryType.DETAILED,
                language = "English",
                maxLength = model.maxTokens / 2,
                customInstructions = null,
                systemPrompt = mergeSystemPrompt
            )

            val result = openRouterApiClient.summarizeText(
                content = combinedChunks,
                options = mergeOptions,
                apiKey = apiKey,
                modelId = model.id
            )

            if (result.isFailure) {
                Log.e(TAG, "Error merging chunk results", result.exceptionOrNull())
                // If merging fails, just concatenate the chunks
                return@withContext chunkResults.joinToString("\n\n")
            }

            return@withContext result.getOrNull() ?: chunkResults.joinToString("\n\n")
        } else {
            // For many chunks, we need to merge hierarchically
            // First, merge adjacent pairs
            val firstLevelMerges = mutableListOf<String>()

            for (i in chunkResults.indices step 2) {
                if (i + 1 < chunkResults.size) {
                    // Merge pair
                    val pair = "${chunkResults[i]}\n\n--- Next Part ---\n\n${chunkResults[i + 1]}"

                    val mergeSystemPrompt = "You are a helpful assistant that combines multiple summaries into a single coherent summary. Eliminate redundancy and create a well-structured partial summary."
                    val mergeUserPrompt = "Please combine these two partial summaries into a single coherent summary, removing any redundancy:\n\n$pair"

                    val pairMergeOptions = SummarizationOptions(
                        summaryType = SummaryType.DETAILED,
                        language = "English",
                        maxLength = model.maxTokens / 2,
                        customInstructions = null,
                        systemPrompt = mergeSystemPrompt
                    )

                    val result = openRouterApiClient.summarizeText(
                        content = pair,
                        options = pairMergeOptions,
                        apiKey = apiKey,
                        modelId = model.id
                    )

                    if (result.isSuccess) {
                        firstLevelMerges.add(result.getOrNull() ?: pair)
                    } else {
                        firstLevelMerges.add(pair)
                    }
                } else {
                    // Add the last odd chunk
                    firstLevelMerges.add(chunkResults[i])
                }
            }

            // Recursively merge until we have a single result
            return@withContext mergeChunkResults(firstLevelMerges, model, apiKey, systemPrompt)
        }
    }
}
