package com.secondbrain.data.service.ai.api

import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.ExtractionOptions
import com.secondbrain.data.service.ai.ModelCapability
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import com.secondbrain.data.service.ai.TagGenerationOptions
import com.secondbrain.data.service.ai.TitleGenerationOptions
import com.secondbrain.data.service.ai.provider.ClaudePromptFormatter
import com.secondbrain.util.ApiAuthenticationException
import com.secondbrain.util.ApiInvalidRequestException
import com.secondbrain.util.ApiRateLimitException
import com.secondbrain.util.ApiTemporaryErrorException
import com.secondbrain.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API client for Anthropic's Claude
 */
class ClaudeApiClient {
    companion object {
        private const val TAG = "ClaudeApiClient"
        private const val BASE_URL = "https://api.anthropic.com/v1"
        private const val MESSAGES_ENDPOINT = "$BASE_URL/messages"
        private const val CLAUDE_3_OPUS = "claude-3-opus-20240229"
        private const val CLAUDE_3_SONNET = "claude-3-sonnet-20240229"
        private const val CLAUDE_3_HAIKU = "claude-3-haiku-20240307"
        private const val DEFAULT_MODEL = CLAUDE_3_SONNET
        private const val ANTHROPIC_VERSION = "2023-06-01"
    }

    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson = Gson()

    /**
     * Fetch available models from Claude API
     * Note: Claude API doesn't have a models endpoint, so we'll return the known models
     */
    suspend fun fetchAvailableModels(apiKey: String): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Claude doesn't have a models endpoint, so we'll create a list of known models
                // We'll verify the API key is valid by making a simple request
                val request = Request.Builder()
                    .url("$BASE_URL/models")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .get()
                    .build()

                // Execute request to check if API key is valid
                val response = client.newCall(request).execute()

                // If unauthorized, throw an exception
                if (response.code == 401) {
                    return@retryWithExponentialBackoff Result.failure(ApiAuthenticationException("Invalid Claude API key"))
                }

                // Create list of known Claude models
                val models = listOf(
                    AiModel(
                        id = "claude-3-opus-20240229",
                        name = "Claude 3 Opus",
                        capabilities = setOf(
                            ModelCapability.TEXT_CONTENT,
                            ModelCapability.TEXT_SUMMARIZATION,
                            ModelCapability.IMAGE_UNDERSTANDING,
                            ModelCapability.TAG_GENERATION,
                            ModelCapability.TITLE_GENERATION,
                            ModelCapability.PDF_PROCESSING,
                            ModelCapability.WEB_CONTENT,
                            ModelCapability.CODE_UNDERSTANDING
                        ),
                        maxTokens = 100000,
                        contextWindow = 200000
                    ),
                    AiModel(
                        id = "claude-3-sonnet-20240229",
                        name = "Claude 3 Sonnet",
                        capabilities = setOf(
                            ModelCapability.TEXT_CONTENT,
                            ModelCapability.TEXT_SUMMARIZATION,
                            ModelCapability.IMAGE_UNDERSTANDING,
                            ModelCapability.TAG_GENERATION,
                            ModelCapability.TITLE_GENERATION,
                            ModelCapability.PDF_PROCESSING,
                            ModelCapability.WEB_CONTENT,
                            ModelCapability.CODE_UNDERSTANDING
                        ),
                        maxTokens = 100000,
                        contextWindow = 200000
                    ),
                    AiModel(
                        id = "claude-3-haiku-20240307",
                        name = "Claude 3 Haiku",
                        capabilities = setOf(
                            ModelCapability.TEXT_CONTENT,
                            ModelCapability.TEXT_SUMMARIZATION,
                            ModelCapability.IMAGE_UNDERSTANDING,
                            ModelCapability.TAG_GENERATION,
                            ModelCapability.TITLE_GENERATION,
                            ModelCapability.WEB_CONTENT,
                            ModelCapability.CODE_UNDERSTANDING
                        ),
                        maxTokens = 100000,
                        contextWindow = 200000
                    )
                )

                Result.success(models)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching models from Claude API", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Summarize text using Claude
     */
    suspend fun summarizeText(
        content: String,
        options: SummarizationOptions,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Use provided system prompt or create one based on summary type
                val systemPrompt = options.systemPrompt ?: when (options.summaryType) {
                    SummaryType.CONCISE -> "You are a helpful assistant that creates concise summaries. Keep the summary brief and to the point, focusing only on the most important information."
                    SummaryType.DETAILED -> "You are a helpful assistant that creates detailed summaries. Include all important details, explanations, and context in your summary."
                    SummaryType.BULLET_POINTS -> "You are a helpful assistant that creates bullet point summaries. Format your summary as a list of bullet points, each covering a key point from the content."
                    SummaryType.QUESTION_ANSWER -> "You are a helpful assistant that creates Q&A summaries. Format your summary as a series of questions and answers that cover the key points from the content."
                    SummaryType.KEY_FACTS -> "You are a helpful assistant that extracts key facts. Identify and list the most important facts from the content."
                }

                // Create user prompt based on summary type
                val userPrompt = when (options.summaryType) {
                    SummaryType.CONCISE -> "Create a concise summary of the following content in ${options.language}:"
                    SummaryType.DETAILED -> "Create a detailed summary of the following content in ${options.language}:"
                    SummaryType.BULLET_POINTS -> "Summarize the following content as bullet points in ${options.language}:"
                    SummaryType.QUESTION_ANSWER -> "Create a Q&A summary of the following content in ${options.language}:"
                    SummaryType.KEY_FACTS -> "Extract the key facts from the following content in ${options.language}:"
                }

                // Add custom instructions if provided
                val fullUserPrompt = if (options.customInstructions.isNullOrEmpty()) {
                    "$userPrompt\n\n$content"
                } else {
                    "$userPrompt\n\nAdditional instructions: ${options.customInstructions}\n\n$content"
                }

                // Create request body using the formatter
                val (formattedSystemPrompt, formattedMessages) = ClaudePromptFormatter.formatPrompt(systemPrompt, fullUserPrompt)

                val requestBody = ClaudeRequest(
                    model = DEFAULT_MODEL,
                    maxTokens = options.maxLength ?: 1000,
                    messages = formattedMessages,
                    system = formattedSystemPrompt,
                    temperature = 0.3
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(MESSAGES_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    handleErrorResponse(response.code, responseBody)
                }

                // Parse response
                val jsonResponse = JSONObject(responseBody)

                // Extract the content
                val content = jsonResponse.getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Claude API call", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Extract text from an image using Claude
     */
    suspend fun extractTextFromImage(
        imageUri: Uri,
        options: ExtractionOptions,
        apiKey: String,
        context: android.content.Context
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Convert URI to base64
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@retryWithExponentialBackoff Result.failure(IOException("Could not open image file"))

                val bytes = inputStream.readBytes()
                inputStream.close()

                val base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)

                // Create system prompt
                val systemPrompt = "You are a helpful assistant that extracts text from images. Extract all visible text from the image, maintaining the original formatting as much as possible."

                // Create user prompt
                val userPrompt = "Extract all text from this image in ${options.language}."

                // Create request body with image
                // For image processing, we need to handle the formatter result differently
                val (formattedSystemPrompt, formattedMessages) = ClaudePromptFormatter.formatPrompt(systemPrompt, userPrompt)

                // Create a special message with both text and image content
                val imageMessage = ClaudeMessage(
                    role = "user",
                    content = listOf(
                        ClaudeContent(type = "text", text = userPrompt),
                        ClaudeContent(
                            type = "image",
                            source = ClaudeImageSource(
                                type = "base64",
                                mediaType = "image/jpeg",
                                data = base64Image
                            )
                        )
                    )
                )

                // Replace the standard text message with our image message
                val messagesWithImage = listOf(imageMessage)

                val requestBody = ClaudeRequest(
                    model = DEFAULT_MODEL,
                    maxTokens = 1000,
                    messages = messagesWithImage,
                    system = formattedSystemPrompt,
                    temperature = 0.2
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(MESSAGES_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    handleErrorResponse(response.code, responseBody)
                }

                // Parse response
                val jsonResponse = JSONObject(responseBody)

                // Extract the content
                val content = jsonResponse.getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Claude Vision API call", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Generate tags from content
     */
    suspend fun generateTags(
        content: String,
        options: TagGenerationOptions,
        apiKey: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create system prompt
                val systemPrompt = "You are a helpful assistant that generates relevant tags for content. Generate tags that accurately represent the main topics, concepts, and entities in the content."

                // Create user prompt
                val userPrompt = "Generate up to ${options.maxTags} tags for the following content in ${options.language}. Return only the tags as a comma-separated list, without any additional text or explanation:\n\n$content"

                // Create request body using the formatter
                val (formattedSystemPrompt, formattedMessages) = ClaudePromptFormatter.formatPrompt(systemPrompt, userPrompt)

                val requestBody = ClaudeRequest(
                    model = DEFAULT_MODEL,
                    maxTokens = 100,
                    messages = formattedMessages,
                    system = formattedSystemPrompt,
                    temperature = 0.3
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(MESSAGES_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    handleErrorResponse(response.code, responseBody)
                }

                // Parse response
                val jsonResponse = JSONObject(responseBody)

                // Extract the content
                val content = jsonResponse.getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Parse tags from comma-separated list
                val tags = content.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .take(options.maxTags)

                Result.success(tags)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Claude API call for tag generation", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Generate a title from content
     */
    suspend fun generateTitle(
        content: String,
        options: TitleGenerationOptions,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create system prompt
                val systemPrompt = "You are a helpful assistant that generates concise, descriptive titles for content. Generate a title that accurately represents the main topic or theme of the content."

                // Create user prompt
                val userPrompt = "Generate a title for the following content in ${options.language}. The title should be concise (maximum ${options.maxLength} characters) and descriptive. Return only the title, without any additional text or explanation:\n\n$content"

                // Create request body using the formatter
                val (formattedSystemPrompt, formattedMessages) = ClaudePromptFormatter.formatPrompt(systemPrompt, userPrompt)

                val requestBody = ClaudeRequest(
                    model = DEFAULT_MODEL,
                    maxTokens = 50,
                    messages = formattedMessages,
                    system = formattedSystemPrompt,
                    temperature = 0.3
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(MESSAGES_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    handleErrorResponse(response.code, responseBody)
                }

                // Parse response
                val jsonResponse = JSONObject(responseBody)

                // Extract the content
                val title = jsonResponse.getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                Result.success(title)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Claude API call for title generation", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Handle error responses from the Claude API
     */
    private fun handleErrorResponse(code: Int, responseBody: String?): Nothing {
        val errorMessage = try {
            if (responseBody != null) {
                val jsonError = JSONObject(responseBody)
                if (jsonError.has("error")) {
                    val error = jsonError.getJSONObject("error")
                    if (error.has("message")) {
                        error.getString("message")
                    } else {
                        "Unknown error"
                    }
                } else {
                    "Unknown error"
                }
            } else {
                "Unknown error"
            }
        } catch (e: Exception) {
            "Error parsing error response: ${e.message}"
        }

        throw when (code) {
            401, 403 -> ApiAuthenticationException("Authentication error: $errorMessage")
            400 -> ApiInvalidRequestException("Invalid request: $errorMessage")
            429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
            in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
            else -> IOException("HTTP error $code: $errorMessage")
        }
    }

    // Data classes for API requests and responses
    data class ClaudeRequest(
        val model: String,
        @SerializedName("max_tokens") val maxTokens: Int,
        val messages: List<ClaudeMessage>,
        val system: String,
        val temperature: Double = 0.7
    )

    data class ClaudeMessage(
        val role: String,
        val content: Any // Can be String or List<ClaudeContent>
    )

    data class ClaudeContent(
        val type: String, // "text" or "image"
        val text: String? = null,
        val source: ClaudeImageSource? = null
    )

    data class ClaudeImageSource(
        val type: String, // "base64"
        @SerializedName("media_type") val mediaType: String,
        val data: String
    )
}
