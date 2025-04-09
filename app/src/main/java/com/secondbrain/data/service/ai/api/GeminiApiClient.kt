package com.secondbrain.data.service.ai.api

import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.secondbrain.data.service.ai.ExtractionOptions
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import com.secondbrain.data.service.ai.TagGenerationOptions
import com.secondbrain.data.service.ai.TitleGenerationOptions
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
 * API client for Google's Gemini AI
 */
class GeminiApiClient {
    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1"
        private const val GEMINI_PRO_MODEL = "gemini-pro"
        private const val GEMINI_PRO_VISION_MODEL = "gemini-pro-vision"
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
     * Summarize text using Gemini
     */
    suspend fun summarizeText(
        content: String,
        options: SummarizationOptions,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.retryWithExponentialBackoff {
            try {
                // Create prompt based on summary type
                val prompt = when (options.summaryType) {
                    SummaryType.CONCISE -> "Create a concise summary of the following content in ${options.language}:"
                    SummaryType.DETAILED -> "Create a detailed summary of the following content in ${options.language}:"
                    SummaryType.BULLET_POINTS -> "Summarize the following content as bullet points in ${options.language}:"
                    SummaryType.QUESTION_ANSWER -> "Create a Q&A summary of the following content in ${options.language}:"
                    SummaryType.KEY_FACTS -> "Extract the key facts from the following content in ${options.language}:"
                }
                
                // Add custom instructions if provided
                val fullPrompt = if (options.customInstructions.isNullOrEmpty()) {
                    "$prompt\n\n$content"
                } else {
                    "$prompt\n\nAdditional instructions: ${options.customInstructions}\n\n$content"
                }
                
                // Create request body
                val requestBody = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = fullPrompt)
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.3f,
                        maxOutputTokens = options.maxLength ?: 1000,
                        topP = 0.95f
                    )
                )
                
                val jsonBody = gson.toJson(requestBody)
                
                // Create request URL with API key
                val url = "$BASE_URL/models/$GEMINI_PRO_MODEL:generateContent?key=$apiKey"
                
                // Create request
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
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
                
                // Check for errors in the response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val message = error.getString("message")
                    val code = error.getInt("code")
                    
                    throw when (code) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $message")
                        400 -> ApiInvalidRequestException("Invalid request: $message")
                        429 -> ApiRateLimitException("Rate limit exceeded: $message")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $message")
                        else -> IOException("API error $code: $message")
                    }
                }
                
                // Extract the generated text
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from Gemini API"))
                }
                
                val content = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                
                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Gemini API call", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Extract text from an image using Gemini Vision
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
                
                // Create prompt
                val prompt = "Extract all text from this image in ${options.language}. Maintain the original formatting as much as possible."
                
                // Create request body with image
                val requestBody = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = prompt),
                                GeminiPart(
                                    inlineData = InlineData(
                                        mimeType = "image/jpeg",
                                        data = base64Image
                                    )
                                )
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.2f,
                        maxOutputTokens = 1000,
                        topP = 0.95f
                    )
                )
                
                val jsonBody = gson.toJson(requestBody)
                
                // Create request URL with API key
                val url = "$BASE_URL/models/$GEMINI_PRO_VISION_MODEL:generateContent?key=$apiKey"
                
                // Create request
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
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
                
                // Check for errors in the response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val message = error.getString("message")
                    val code = error.getInt("code")
                    
                    throw when (code) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $message")
                        400 -> ApiInvalidRequestException("Invalid request: $message")
                        429 -> ApiRateLimitException("Rate limit exceeded: $message")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $message")
                        else -> IOException("API error $code: $message")
                    }
                }
                
                // Extract the generated text
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from Gemini API"))
                }
                
                val content = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                
                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Gemini Vision API call", e)
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
                // Create prompt
                val prompt = "Generate up to ${options.maxTags} tags for the following content in ${options.language}. Return only the tags as a comma-separated list, without any additional text or explanation:\n\n$content"
                
                // Create request body
                val requestBody = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = prompt)
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.3f,
                        maxOutputTokens = 100,
                        topP = 0.95f
                    )
                )
                
                val jsonBody = gson.toJson(requestBody)
                
                // Create request URL with API key
                val url = "$BASE_URL/models/$GEMINI_PRO_MODEL:generateContent?key=$apiKey"
                
                // Create request
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
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
                
                // Check for errors in the response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val message = error.getString("message")
                    val code = error.getInt("code")
                    
                    throw when (code) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $message")
                        400 -> ApiInvalidRequestException("Invalid request: $message")
                        429 -> ApiRateLimitException("Rate limit exceeded: $message")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $message")
                        else -> IOException("API error $code: $message")
                    }
                }
                
                // Extract the generated text
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from Gemini API"))
                }
                
                val content = candidates.getJSONObject(0)
                    .getJSONObject("content")
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
                Log.e(TAG, "Error in Gemini API call for tag generation", e)
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
                // Create prompt
                val prompt = "Generate a title for the following content in ${options.language}. The title should be concise (maximum ${options.maxLength} characters) and descriptive. Return only the title, without any additional text or explanation:\n\n$content"
                
                // Create request body
                val requestBody = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = prompt)
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.3f,
                        maxOutputTokens = 50,
                        topP = 0.95f
                    )
                )
                
                val jsonBody = gson.toJson(requestBody)
                
                // Create request URL with API key
                val url = "$BASE_URL/models/$GEMINI_PRO_MODEL:generateContent?key=$apiKey"
                
                // Create request
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
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
                
                // Check for errors in the response
                if (jsonResponse.has("error")) {
                    val error = jsonResponse.getJSONObject("error")
                    val message = error.getString("message")
                    val code = error.getInt("code")
                    
                    throw when (code) {
                        401, 403 -> ApiAuthenticationException("Authentication error: $message")
                        400 -> ApiInvalidRequestException("Invalid request: $message")
                        429 -> ApiRateLimitException("Rate limit exceeded: $message")
                        in 500..599 -> ApiTemporaryErrorException("Server error: $message")
                        else -> IOException("API error $code: $message")
                    }
                }
                
                // Extract the generated text
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() == 0) {
                    return@retryWithExponentialBackoff Result.failure(IOException("No response from Gemini API"))
                }
                
                val title = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                
                Result.success(title)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Gemini API call for title generation", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Handle error responses from the Gemini API
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
    data class GeminiRequest(
        val contents: List<GeminiContent>,
        @SerializedName("generation_config") val generationConfig: GenerationConfig
    )
    
    data class GeminiContent(
        val parts: List<GeminiPart>
    )
    
    data class GeminiPart(
        val text: String? = null,
        @SerializedName("inline_data") val inlineData: InlineData? = null
    )
    
    data class InlineData(
        @SerializedName("mime_type") val mimeType: String,
        val data: String
    )
    
    data class GenerationConfig(
        val temperature: Float,
        @SerializedName("max_output_tokens") val maxOutputTokens: Int,
        @SerializedName("top_p") val topP: Float
    )
}
