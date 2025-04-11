package com.secondbrain.util

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Network utilities for handling API calls
 */
object NetworkUtils {
    private const val TAG = "NetworkUtils"

    /**
     * Retry a suspending operation with exponential backoff
     *
     * @param times Number of retry attempts
     * @param initialDelayMs Initial delay in milliseconds
     * @param maxDelayMs Maximum delay in milliseconds
     * @param factor Exponential factor for backoff
     * @param block The suspending function to retry
     * @return Result of the operation
     */
    suspend fun <T> retryWithExponentialBackoff(
        times: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 20000,
        factor: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelayMs
        repeat(times) { attempt ->
            // Execute the operation
            val result = block()

            // If successful or not a retryable error, return the result
            if (result.isSuccess || !isRetryableError(result.exceptionOrNull())) {
                return result
            }

            // Log the retry attempt
            Log.d(TAG, "Retry attempt ${attempt + 1}/$times after $currentDelay ms")

            // Wait before next retry
            delay(currentDelay)

            // Increase the delay for next retry, but don't exceed maxDelayMs
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
        }

        // Last attempt
        return block()
    }

    /**
     * Check if an error is retryable
     */
    private fun isRetryableError(error: Throwable?): Boolean {
        if (error == null) return false

        return when (error) {
            // Network errors
            is java.net.SocketTimeoutException,
            is java.net.ConnectException,
            is java.net.UnknownHostException,
            is java.io.IOException,

            // HTTP errors (5xx)
            is retrofit2.HttpException -> {
                if (error is retrofit2.HttpException) {
                    val code = error.code()
                    // Retry server errors (5xx) and rate limit errors (429)
                    code in 500..599 || code == 429
                } else {
                    true
                }
            }

            // Other retryable errors
            is ApiRateLimitException,
            is ApiTemporaryErrorException,
            is ApiServerOverloadException -> true

            // Non-retryable errors
            is ApiAuthenticationException,
            is ApiInvalidRequestException -> false

            // Default to not retry for unknown errors
            else -> false
        }
    }
}

// Custom API exceptions
class ApiRateLimitException(message: String) : Exception(message)
class ApiTemporaryErrorException(message: String) : Exception(message)
class ApiAuthenticationException(message: String) : Exception(message)
class ApiInvalidRequestException(message: String) : Exception(message)
