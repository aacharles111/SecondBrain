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
import com.secondbrain.data.service.ai.provider.OpenRouterPromptFormatter
import com.secondbrain.util.ApiAuthenticationException
import com.secondbrain.util.ApiInvalidRequestException
import com.secondbrain.util.ApiPaymentRequiredException
import com.secondbrain.util.ApiRateLimitException
import com.secondbrain.util.ApiServerOverloadException
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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API client for OpenRouter
 */
@Singleton
class OpenRouterApiClient @Inject constructor() {
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

                // Check for error response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val errorMessage = if (error.has("message")) error.getString("message") else "Unknown error"
                    val errorCode = if (error.has("code")) error.getInt("code") else 400

                    // Handle specific error codes
                    val exception = when (errorCode) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $errorMessage")
                        402 -> ApiPaymentRequiredException("Payment required: $errorMessage")
                        429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
                        else -> IOException("API error: $errorMessage")
                    }

                    Log.e(TAG, "OpenRouter API error: $errorCode - $errorMessage")
                    return@retryWithExponentialBackoff Result.failure(exception)
                }

                val data = jsonResponse.getJSONArray("data")
                val models = mutableListOf<AiModel>()

                for (i in 0 until data.length()) {
                    val modelObj = data.getJSONObject(i)
                    val id = modelObj.getString("id")
                    val name = modelObj.getString("name")
                    val contextLength = modelObj.optInt("context_length", 4096)

                    // Determine capabilities based on model properties
                    val capabilities = mutableSetOf<ModelCapability>()

                    // Basic capabilities all models have
                    capabilities.add(ModelCapability.TEXT_CONTENT)
                    capabilities.add(ModelCapability.TEXT_SUMMARIZATION)
                    capabilities.add(ModelCapability.TAG_GENERATION)
                    capabilities.add(ModelCapability.TITLE_GENERATION)
                    capabilities.add(ModelCapability.WEB_CONTENT)

                    // Check if model supports vision
                    if (modelObj.optBoolean("vision", false)) {
                        capabilities.add(ModelCapability.IMAGE_UNDERSTANDING)
                    }

                    // Add PDF processing for models with large context windows
                    if (contextLength >= 16000) {
                        capabilities.add(ModelCapability.PDF_PROCESSING)
                    }

                    // Add code understanding for specific models
                    if (name.contains("code", ignoreCase = true) ||
                        name.contains("gpt-4", ignoreCase = true) ||
                        name.contains("claude", ignoreCase = true) ||
                        name.contains("llama", ignoreCase = true)) {
                        capabilities.add(ModelCapability.CODE_UNDERSTANDING)
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

                // Create request body using the formatter
                val messages = OpenRouterPromptFormatter.formatPrompt(systemPrompt, fullUserPrompt, modelId)

                // Calculate a reasonable token limit based on content length
                // Use a conservative estimate: for summarization, output is typically shorter than input
                // Default to 300 tokens if no specific length is provided
                val estimatedTokens = estimateRequiredTokens(content.length, options.summaryType)
                val requestedTokens = options.maxLength ?: estimatedTokens

                // Use the smaller of the requested tokens or estimated tokens to avoid credit issues
                val maxTokens = minOf(requestedTokens, 300) // Conservative default max of 300 tokens

                // Log detailed token usage information
                Log.d(TAG, "TOKEN USAGE - Summarization request details:")
                Log.d(TAG, "TOKEN USAGE - Content length: ${content.length} characters")
                Log.d(TAG, "TOKEN USAGE - Summary type: ${options.summaryType}")
                Log.d(TAG, "TOKEN USAGE - Estimated tokens needed: $estimatedTokens")
                Log.d(TAG, "TOKEN USAGE - Requested tokens: $requestedTokens")
                Log.d(TAG, "TOKEN USAGE - Final max_tokens: $maxTokens")
                Log.d(TAG, "TOKEN USAGE - Model: $modelId")

                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = messages,
                    temperature = 0.3,
                    maxTokens = maxTokens
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

                // Log token usage from the response if available
                try {
                    if (jsonResponse.has("usage")) {
                        val usage = jsonResponse.getJSONObject("usage")
                        val promptTokens = usage.optInt("prompt_tokens", 0)
                        val completionTokens = usage.optInt("completion_tokens", 0)
                        val totalTokens = usage.optInt("total_tokens", 0)

                        Log.d(TAG, "TOKEN USAGE - API response token usage:")
                        Log.d(TAG, "TOKEN USAGE - Prompt tokens: $promptTokens")
                        Log.d(TAG, "TOKEN USAGE - Completion tokens: $completionTokens")
                        Log.d(TAG, "TOKEN USAGE - Total tokens: $totalTokens")
                    } else {
                        Log.d(TAG, "TOKEN USAGE - No usage information in API response")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "TOKEN USAGE - Error parsing token usage information", e)
                }

                Log.d(TAG, "OpenRouter API response: $responseBody")

                // Check for error response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val errorMessage = if (error.has("message")) error.getString("message") else "Unknown error"
                    val errorCode = if (error.has("code")) error.getInt("code") else 400

                    // Log detailed information for token-related errors
                    if (errorCode == 402 && errorMessage.contains("max_tokens", ignoreCase = true)) {
                        // This is a token limit error, log detailed information
                        Log.e(TAG, "TOKEN USAGE - Token limit error detected")
                        Log.e(TAG, "TOKEN USAGE - Error message: $errorMessage")

                        // Try to extract the available tokens from the error message
                        val regex = "can only afford (\\d+)".toRegex()
                        val matchResult = regex.find(errorMessage)
                        val availableTokens = matchResult?.groupValues?.get(1)?.toIntOrNull()

                        if (availableTokens != null) {
                            Log.e(TAG, "TOKEN USAGE - Available tokens: $availableTokens")
                            Log.e(TAG, "TOKEN USAGE - Requested max_tokens: ${requestBody.maxTokens}")
                            Log.e(TAG, "TOKEN USAGE - Difference: ${requestBody.maxTokens - availableTokens}")
                        }
                    }

                    // Handle specific error codes
                    val exception = when (errorCode) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $errorMessage")
                        402 -> ApiPaymentRequiredException("Payment required: $errorMessage")
                        429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
                        else -> IOException("API error: $errorMessage")
                    }

                    Log.e(TAG, "OpenRouter API error: $errorCode - $errorMessage")
                    return@retryWithExponentialBackoff Result.failure(exception)
                }

                // Extract the generated text
                if (!jsonResponse.has("choices")) {
                    Log.e(TAG, "OpenRouter API response missing 'choices' field: $responseBody")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
                }

                val choice = choices.getJSONObject(0)
                if (!choice.has("message")) {
                    Log.e(TAG, "OpenRouter API response missing 'message' field in choice: $choice")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val message = choice.getJSONObject("message")
                if (!message.has("content")) {
                    Log.e(TAG, "OpenRouter API response missing 'content' field in message: $message")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

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

                // For image processing, we need special handling
                // First get the standard messages from the formatter
                val standardMessages = OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)

                // Then replace the user message with one that includes the image
                val messagesWithImage = if (standardMessages.size > 1) {
                    // Keep the system message if it exists
                    val systemMessage = standardMessages[0]
                    val imageMessage = OpenRouterMessage(
                        role = "user",
                        content = listOf(
                            OpenRouterContent(type = "text", text = userPrompt),
                            OpenRouterContent(
                                type = "image_url",
                                imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
                            )
                        )
                    )
                    listOf(systemMessage, imageMessage)
                } else {
                    // No system message, just the user message with image
                    listOf(
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
                    )
                }

                // For image processing, use a conservative token limit
                val maxTokens = 250 // Reduced from 1000 to conserve tokens

                // Log token usage information for image processing
                Log.d(TAG, "TOKEN USAGE - Image processing request details:")
                Log.d(TAG, "TOKEN USAGE - Using max_tokens: $maxTokens")
                Log.d(TAG, "TOKEN USAGE - Model: $modelId")
                Log.d(TAG, "TOKEN USAGE - Language: ${options.language}")

                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = messagesWithImage,
                    temperature = 0.2,
                    maxTokens = maxTokens
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
                Log.d(TAG, "OpenRouter API response: $responseBody")

                // Check for error response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val errorMessage = if (error.has("message")) error.getString("message") else "Unknown error"
                    val errorCode = if (error.has("code")) error.getInt("code") else 400

                    // Handle specific error codes
                    val exception = when (errorCode) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $errorMessage")
                        402 -> ApiPaymentRequiredException("Payment required: $errorMessage")
                        429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
                        else -> IOException("API error: $errorMessage")
                    }

                    Log.e(TAG, "OpenRouter API error: $errorCode - $errorMessage")
                    return@retryWithExponentialBackoff Result.failure(exception)
                }

                // Extract the generated text
                if (!jsonResponse.has("choices")) {
                    Log.e(TAG, "OpenRouter API response missing 'choices' field: $responseBody")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
                }

                val choice = choices.getJSONObject(0)
                if (!choice.has("message")) {
                    Log.e(TAG, "OpenRouter API response missing 'message' field in choice: $choice")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val message = choice.getJSONObject("message")
                if (!message.has("content")) {
                    Log.e(TAG, "OpenRouter API response missing 'content' field in message: $message")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

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
                val userPrompt = "Generate between 10 and 20 relevant tags for the following content in ${options.language}. The tags should cover all important topics, concepts, and entities in the content. Return only the tags as a comma-separated list, without any additional text or explanation:\n\n$content"

                // Create request body using the formatter
                val messages = OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)

                // For tag generation, we need a small number of tokens
                val maxTokens = 50 // Reduced from 100 to conserve tokens

                // Log token usage information for tag generation
                Log.d(TAG, "TOKEN USAGE - Tag generation request details:")
                Log.d(TAG, "TOKEN USAGE - Content length: ${content.length} characters")
                Log.d(TAG, "TOKEN USAGE - Max tags: ${options.maxTags}")
                Log.d(TAG, "TOKEN USAGE - Using max_tokens: $maxTokens")
                Log.d(TAG, "TOKEN USAGE - Model: $modelId")

                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = messages,
                    temperature = 0.3,
                    maxTokens = maxTokens
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
                Log.d(TAG, "OpenRouter API response: $responseBody")

                // Check for error response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val errorMessage = if (error.has("message")) error.getString("message") else "Unknown error"
                    val errorCode = if (error.has("code")) error.getInt("code") else 400

                    // Handle specific error codes
                    val exception = when (errorCode) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $errorMessage")
                        402 -> ApiPaymentRequiredException("Payment required: $errorMessage")
                        429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
                        else -> IOException("API error: $errorMessage")
                    }

                    Log.e(TAG, "OpenRouter API error: $errorCode - $errorMessage")
                    return@retryWithExponentialBackoff Result.failure(exception)
                }

                // Extract the generated text
                if (!jsonResponse.has("choices")) {
                    Log.e(TAG, "OpenRouter API response missing 'choices' field: $responseBody")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
                }

                val choice = choices.getJSONObject(0)
                if (!choice.has("message")) {
                    Log.e(TAG, "OpenRouter API response missing 'message' field in choice: $choice")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val message = choice.getJSONObject("message")
                if (!message.has("content")) {
                    Log.e(TAG, "OpenRouter API response missing 'content' field in message: $message")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

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

                // Create request body using the formatter
                val messages = OpenRouterPromptFormatter.formatPrompt(systemPrompt, userPrompt, modelId)

                // For title generation, we need very few tokens
                val maxTokens = 30 // Reduced from 50 to conserve tokens

                // Log token usage information for title generation
                Log.d(TAG, "TOKEN USAGE - Title generation request details:")
                Log.d(TAG, "TOKEN USAGE - Content length: ${content.length} characters")
                Log.d(TAG, "TOKEN USAGE - Max title length: ${options.maxLength}")
                Log.d(TAG, "TOKEN USAGE - Using max_tokens: $maxTokens")
                Log.d(TAG, "TOKEN USAGE - Model: $modelId")

                val requestBody = OpenRouterRequest(
                    model = modelId,
                    messages = messages,
                    temperature = 0.3,
                    maxTokens = maxTokens
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
                Log.d(TAG, "OpenRouter API response: $responseBody")

                // Check for error response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val errorMessage = if (error.has("message")) error.getString("message") else "Unknown error"
                    val errorCode = if (error.has("code")) error.getInt("code") else 400

                    // Handle specific error codes
                    val exception = when (errorCode) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $errorMessage")
                        402 -> ApiPaymentRequiredException("Payment required: $errorMessage")
                        429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
                        else -> IOException("API error: $errorMessage")
                    }

                    Log.e(TAG, "OpenRouter API error: $errorCode - $errorMessage")
                    return@retryWithExponentialBackoff Result.failure(exception)
                }

                // Extract the generated text
                if (!jsonResponse.has("choices")) {
                    Log.e(TAG, "OpenRouter API response missing 'choices' field: $responseBody")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from OpenRouter API"))
                }

                val choice = choices.getJSONObject(0)
                if (!choice.has("message")) {
                    Log.e(TAG, "OpenRouter API response missing 'message' field in choice: $choice")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

                val message = choice.getJSONObject("message")
                if (!message.has("content")) {
                    Log.e(TAG, "OpenRouter API response missing 'content' field in message: $message")
                    return@retryWithExponentialBackoff Result.failure(IOException("Invalid response format from OpenRouter API"))
                }

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

        // Check for server overload conditions in the error message
        val isServerOverloaded = errorMessage.contains("server overload", ignoreCase = true) ||
                errorMessage.contains("too many requests", ignoreCase = true) ||
                errorMessage.contains("capacity", ignoreCase = true) ||
                errorMessage.contains("busy", ignoreCase = true) ||
                errorMessage.contains("try again later", ignoreCase = true) ||
                errorMessage.contains("queue", ignoreCase = true)

        // Check for token-related errors and log detailed information
        if (code == 402 && errorMessage.contains("max_tokens", ignoreCase = true)) {
            // This is a token limit error, log detailed information
            Log.e(TAG, "TOKEN USAGE - Token limit error detected in handleErrorResponse")
            Log.e(TAG, "TOKEN USAGE - Error message: $errorMessage")

            // Try to extract the available tokens from the error message
            val regex = "can only afford (\\d+)".toRegex()
            val matchResult = regex.find(errorMessage)
            val availableTokens = matchResult?.groupValues?.get(1)?.toIntOrNull()

            if (availableTokens != null) {
                Log.e(TAG, "TOKEN USAGE - Available tokens: $availableTokens")
                // We don't have access to the requested tokens here, but we can log what we know
                Log.e(TAG, "TOKEN USAGE - Consider reducing max_tokens to: $availableTokens or less")
            }
        }

        throw when {
            isServerOverloaded -> ApiServerOverloadException("OpenRouter server is currently overloaded. Please try again later.")
            code == 401 || code == 403 -> ApiAuthenticationException("Authentication error: $errorMessage")
            code == 400 -> ApiInvalidRequestException("Invalid request: $errorMessage")
            code == 402 -> ApiPaymentRequiredException("Payment required: $errorMessage")
            code == 429 -> ApiRateLimitException("Rate limit exceeded: $errorMessage")
            code in 500..599 -> ApiTemporaryErrorException("Server error: $errorMessage")
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

    /**
     * Estimate the number of tokens required for a summary based on content length and summary type
     * This is a rough estimate to avoid hitting token limits
     *
     * @param contentLength The length of the content to summarize
     * @param summaryType The type of summary to generate
     * @return An estimated number of tokens needed for the summary
     */
    private fun estimateRequiredTokens(contentLength: Int, summaryType: SummaryType): Int {
        // Very rough estimate: 1 token ≈ 4 characters in English
        // For summarization, output is typically much shorter than input
        val ratio = when (summaryType) {
            SummaryType.CONCISE -> 0.1 // Very short summary
            SummaryType.BULLET_POINTS -> 0.15 // Bullet points are concise
            SummaryType.KEY_FACTS -> 0.15 // Key facts are concise
            SummaryType.DETAILED -> 0.25 // Detailed summaries are longer
            SummaryType.QUESTION_ANSWER -> 0.25 // Q&A format can be verbose
        }

        // Calculate estimated tokens based on content length and ratio
        val estimatedChars = contentLength * ratio
        val estimatedTokens = (estimatedChars / 4).toInt()

        // Ensure we stay within reasonable limits
        return when {
            estimatedTokens < 50 -> 50 // Minimum for a useful summary
            estimatedTokens > 300 -> 300 // Conservative maximum to avoid credit issues
            else -> estimatedTokens
        }
    }
}
