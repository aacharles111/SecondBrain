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
import com.secondbrain.data.service.ai.TranscriptionOptions
import com.secondbrain.data.service.ai.provider.OpenAiPromptFormatter
import com.secondbrain.util.ApiAuthenticationException
import com.secondbrain.util.ApiInvalidRequestException
import com.secondbrain.util.ApiRateLimitException
import com.secondbrain.util.ApiTemporaryErrorException
import com.secondbrain.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API client for OpenAI
 */
class OpenAiApiClient {
    companion object {
        private const val TAG = "OpenAiApiClient"
        private const val BASE_URL = "https://api.openai.com/v1"
        private const val CHAT_ENDPOINT = "$BASE_URL/chat/completions"
        private const val AUDIO_TRANSCRIPTION_ENDPOINT = "$BASE_URL/audio/transcriptions"
        private const val VISION_MODEL = "gpt-4-vision-preview"
        private const val DEFAULT_MODEL = "gpt-4o"
        private const val WHISPER_MODEL = "whisper-1"
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
     * Fetch available models from OpenAI API
     */
    suspend fun fetchAvailableModels(apiKey: String): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create request
                val request = Request.Builder()
                    .url("$BASE_URL/models")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .get()
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    val errorMessage = "Error fetching models: ${response.code} - ${responseBody ?: "Unknown error"}"
                    Log.e(TAG, errorMessage)
                    return@retryWithExponentialBackoff Result.failure(IOException(errorMessage))
                }

                // Parse response
                val jsonResponse = JSONObject(responseBody)
                val modelsArray = jsonResponse.getJSONArray("data")

                val models = mutableListOf<AiModel>()
                val gptModels = mutableListOf<JSONObject>()

                // First pass: filter for GPT models
                for (i in 0 until modelsArray.length()) {
                    val modelObj = modelsArray.getJSONObject(i)
                    val id = modelObj.getString("id")

                    // Only include GPT models
                    if (id.startsWith("gpt-")) {
                        gptModels.add(modelObj)
                    }
                }

                // Second pass: create AiModel objects
                for (modelObj in gptModels) {
                    val id = modelObj.getString("id")

                    // Determine capabilities based on model ID
                    val capabilities = mutableSetOf<ModelCapability>()

                    // Basic capabilities all models have
                    capabilities.add(ModelCapability.TEXT_CONTENT)
                    capabilities.add(ModelCapability.TEXT_SUMMARIZATION)
                    capabilities.add(ModelCapability.TAG_GENERATION)
                    capabilities.add(ModelCapability.TITLE_GENERATION)

                    // Check if model supports vision
                    if (id.contains("vision") || id.contains("gpt-4o")) {
                        capabilities.add(ModelCapability.IMAGE_UNDERSTANDING)
                    }

                    // Add PDF processing for newer models
                    if (id.contains("gpt-4") || id.contains("gpt-3.5-turbo")) {
                        capabilities.add(ModelCapability.PDF_PROCESSING)
                        capabilities.add(ModelCapability.WEB_CONTENT)
                    }

                    // Add code understanding for newer GPT models
                    if (id.contains("gpt-4") || id.contains("gpt-3.5")) {
                        capabilities.add(ModelCapability.CODE_UNDERSTANDING)
                    }

                    // Determine context window based on model
                    val contextWindow = when {
                        id.contains("gpt-4o") -> 128000
                        id.contains("gpt-4-turbo") -> 128000
                        id.contains("gpt-4") -> 8192
                        id.contains("gpt-3.5-turbo-16k") -> 16384
                        id.contains("gpt-3.5-turbo") -> 4096
                        else -> 4096
                    }

                    // Create a more user-friendly display name
                    val displayName = when {
                        id == "gpt-4o" -> "GPT-4o"
                        id.contains("gpt-4-vision") -> "GPT-4 Vision"
                        id.contains("gpt-4-turbo") -> "GPT-4 Turbo"
                        id.contains("gpt-4") -> "GPT-4"
                        id.contains("gpt-3.5-turbo-16k") -> "GPT-3.5 Turbo 16K"
                        id.contains("gpt-3.5-turbo") -> "GPT-3.5 Turbo"
                        else -> id
                    }

                    models.add(
                        AiModel(
                            id = id,
                            name = displayName,
                            capabilities = capabilities,
                            maxTokens = contextWindow / 2, // Approximate max output tokens
                            contextWindow = contextWindow
                        )
                    )
                }

                // Sort models by capability (most capable first) and then by name
                val sortedModels = models.sortedWith(compareByDescending<AiModel> { it.capabilities.size }.thenBy { it.name })

                Result.success(sortedModels)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching models from OpenAI API", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Summarize text using OpenAI
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

                // Create request body with appropriate message format using the formatter
                val messages = OpenAiPromptFormatter.formatPrompt(systemPrompt, fullUserPrompt)

                val requestBody = ChatCompletionRequest(
                    model = DEFAULT_MODEL,
                    messages = messages,
                    temperature = 0.3,
                    maxTokens = options.maxLength ?: 1000
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
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
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenAI API"))
                }

                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")

                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenAI API call", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Transcribe audio using OpenAI's Whisper model
     */
    suspend fun transcribeAudio(
        audioFile: File,
        options: TranscriptionOptions,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create multipart request
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model", WHISPER_MODEL)
                    .addFormDataPart("file", audioFile.name, audioFile.asRequestBody("audio/*".toMediaType()))
                    .addFormDataPart("language", options.language)

                if (!options.prompt.isNullOrEmpty()) {
                    requestBody.addFormDataPart("prompt", options.prompt)
                }

                if (options.timestamped) {
                    requestBody.addFormDataPart("response_format", "verbose_json")
                } else {
                    requestBody.addFormDataPart("response_format", "text")
                }

                // Create request
                val request = Request.Builder()
                    .url(AUDIO_TRANSCRIPTION_ENDPOINT)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(requestBody.build())
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    handleErrorResponse(response.code, responseBody)
                }

                // Parse response
                if (options.timestamped) {
                    // Parse JSON response with timestamps
                    val jsonResponse = JSONObject(responseBody)
                    val text = jsonResponse.getString("text")
                    Result.success(text)
                } else {
                    // Plain text response
                    Result.success(responseBody)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenAI transcription API call", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Extract text from an image using OpenAI's Vision model
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
                val requestBody = ChatCompletionRequest(
                    model = VISION_MODEL,
                    messages = listOf(
                        Message(role = "system", content = systemPrompt),
                        Message(
                            role = "user",
                            content = listOf(
                                MessageContent(type = "text", text = userPrompt),
                                MessageContent(
                                    type = "image_url",
                                    imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
                                )
                            )
                        )
                    ),
                    temperature = 0.3,
                    maxTokens = 1000
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
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
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenAI API"))
                }

                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")

                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenAI Vision API call", e)
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

                // Create request body
                val requestBody = ChatCompletionRequest(
                    model = DEFAULT_MODEL,
                    messages = listOf(
                        Message(role = "system", content = systemPrompt),
                        Message(role = "user", content = userPrompt)
                    ),
                    temperature = 0.3,
                    maxTokens = 100
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
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
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenAI API"))
                }

                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")

                // Parse tags from comma-separated list
                val tags = content.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .take(options.maxTags)

                Result.success(tags)
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenAI API call for tag generation", e)
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

                // Create request body
                val requestBody = ChatCompletionRequest(
                    model = DEFAULT_MODEL,
                    messages = listOf(
                        Message(role = "system", content = systemPrompt),
                        Message(role = "user", content = userPrompt)
                    ),
                    temperature = 0.3,
                    maxTokens = 50
                )

                val jsonBody = gson.toJson(requestBody)

                // Create request
                val request = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
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
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenAI API"))
                }

                val message = choices.getJSONObject(0).getJSONObject("message")
                val title = message.getString("content").trim()

                Result.success(title)
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenAI API call for title generation", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Handle error responses from the OpenAI API
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
            401 -> ApiAuthenticationException("Authentication error: $errorMessage")
            400 -> ApiInvalidRequestException("Invalid request: $errorMessage")
            429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
            in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
            else -> IOException("HTTP error $code: $errorMessage")
        }
    }

    // Data classes for API requests and responses
    data class ChatCompletionRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7,
        @SerializedName("max_tokens") val maxTokens: Int = 1000
    )

    data class Message(
        val role: String,
        val content: Any // Can be String or List<MessageContent>
    )

    data class MessageContent(
        val type: String, // "text" or "image_url"
        val text: String? = null,
        @SerializedName("image_url") val imageUrl: ImageUrl? = null
    )

    data class ImageUrl(
        val url: String
    )
}
