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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API client for OpenRouter
 */
class OpenRouterApiClient {
    companion object {
        private const val TAG = "OpenRouterApiClient"
        private const val BASE_URL = "https://openrouter.ai/api/v1"
        private const val CHAT_ENDPOINT = "$BASE_URL/chat/completions"
        private const val MODELS_ENDPOINT = "$BASE_URL/models"
        private const val DEFAULT_MODEL = "anthropic/claude-3-sonnet"
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
     * Fetch available models from OpenRouter
     */
    suspend fun fetchAvailableModels(apiKey: String): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create request
                val request = Request.Builder()
                    .url(MODELS_ENDPOINT)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .get()
                    .build()
                
                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (!response.isSuccessful || responseBody == null) {
                    handleErrorResponse(response.code, responseBody)
                }
                
                // Parse response
                val jsonResponse = JSONObject(responseBody)
                val data = jsonResponse.getJSONArray("data")
                
                val models = mutableListOf<AiModel>()
                
                for (i in 0 until data.length()) {
                    val modelObj = data.getJSONObject(i)
                    val id = modelObj.getString("id")
                    val name = modelObj.getString("name")
                    val contextLength = modelObj.optInt("context_length", 4096)
                    
                    // Determine capabilities based on model properties
                    val capabilities = mutableSetOf<ModelCapability>()
                    capabilities.add(ModelCapability.TEXT_SUMMARIZATION)
                    capabilities.add(ModelCapability.TAG_GENERATION)
                    capabilities.add(ModelCapability.TITLE_GENERATION)
                    
                    // Check if model supports vision
                    if (modelObj.optBoolean("vision", false)) {
                        capabilities.add(ModelCapability.IMAGE_UNDERSTANDING)
                    }
                    
                    // Add model to list
                    models.add(
                        AiModel(
                            id = id,
                            name = name,
                            capabilities = capabilities,
                            maxTokens = contextLength,
                            contextWindow = contextLength
                        )
                    )
                }
                
                Result.success(models)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching models from OpenRouter", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Summarize text using OpenRouter
     */
    suspend fun summarizeText(
        content: String,
        options: SummarizationOptions,
        apiKey: String,
        modelId: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create system prompt based on summary type
                val systemPrompt = when (options.summaryType) {
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
                
                // Create request body
                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = systemPrompt),
                        OpenRouterMessage(role = "user", content = fullUserPrompt)
                    ),
                    temperature = 0.3,
                    maxTokens = options.maxLength ?: 1000
                )
                
                val jsonBody = gson.toJson(requestBody)
                
                // Create request
                val request = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("HTTP-Referer", "https://secondbrain.app") // Required by OpenRouter
                    .addHeader("X-Title", "Second Brain App") // Required by OpenRouter
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
                
                // Extract the generated text
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
                }
                
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")
                
                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenRouter API call", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Extract text from an image using OpenRouter
     */
    suspend fun extractTextFromImage(
        imageUri: Uri,
        options: ExtractionOptions,
        apiKey: String,
        modelId: String,
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
                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = systemPrompt),
                        OpenRouterMessage(
                            role = "user",
                            content = listOf(
                                OpenRouterContent(type = "text", text = userPrompt),
                                OpenRouterContent(
                                    type = "image_url",
                                    imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
                                )
                            )
                        )
                    ),
                    temperature = 0.2,
                    maxTokens = 1000
                )
                
                val jsonBody = gson.toJson(requestBody)
                
                // Create request
                val request = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("HTTP-Referer", "https://secondbrain.app") // Required by OpenRouter
                    .addHeader("X-Title", "Second Brain App") // Required by OpenRouter
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
                
                // Extract the generated text
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
                }
                
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")
                
                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenRouter Vision API call", e)
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
        apiKey: String,
        modelId: String = DEFAULT_MODEL
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create system prompt
                val systemPrompt = "You are a helpful assistant that generates relevant tags for content. Generate tags that accurately represent the main topics, concepts, and entities in the content."
                
                // Create user prompt
                val userPrompt = "Generate up to ${options.maxTags} tags for the following content in ${options.language}. Return only the tags as a comma-separated list, without any additional text or explanation:\n\n$content"
                
                // Create request body
                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = systemPrompt),
                        OpenRouterMessage(role = "user", content = userPrompt)
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
                    .addHeader("HTTP-Referer", "https://secondbrain.app") // Required by OpenRouter
                    .addHeader("X-Title", "Second Brain App") // Required by OpenRouter
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
                
                // Extract the generated text
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
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
                Log.e(TAG, "Error in OpenRouter API call for tag generation", e)
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
        apiKey: String,
        modelId: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create system prompt
                val systemPrompt = "You are a helpful assistant that generates concise, descriptive titles for content. Generate a title that accurately represents the main topic or theme of the content."
                
                // Create user prompt
                val userPrompt = "Generate a title for the following content in ${options.language}. The title should be concise (maximum ${options.maxLength} characters) and descriptive. Return only the title, without any additional text or explanation:\n\n$content"
                
                // Create request body
                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = systemPrompt),
                        OpenRouterMessage(role = "user", content = userPrompt)
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
                    .addHeader("HTTP-Referer", "https://secondbrain.app") // Required by OpenRouter
                    .addHeader("X-Title", "Second Brain App") // Required by OpenRouter
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
                
                // Extract the generated text
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
                }
                
                val message = choices.getJSONObject(0).getJSONObject("message")
                val title = message.getString("content").trim()
                
                Result.success(title)
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenRouter API call for title generation", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Handle error responses from the OpenRouter API
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
    data class OpenRouterRequest(
        val model: String,
        val messages: List<OpenRouterMessage>,
        val temperature: Double = 0.7,
        @SerializedName("max_tokens") val maxTokens: Int = 1000
    )
    
    data class OpenRouterMessage(
        val role: String,
        val content: Any // Can be String or List<OpenRouterContent>
    )
    
    data class OpenRouterContent(
        val type: String, // "text" or "image_url"
        val text: String? = null,
        @SerializedName("image_url") val imageUrl: ImageUrl? = null
    )
    
    data class ImageUrl(
        val url: String
    )
}
