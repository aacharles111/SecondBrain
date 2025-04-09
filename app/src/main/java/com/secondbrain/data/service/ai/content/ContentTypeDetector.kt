package com.secondbrain.data.service.ai.content

import android.util.Log
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for detecting content type
 */
@Singleton
class ContentTypeDetector @Inject constructor(
    private val aiServiceManager: AiServiceManager
) {
    companion object {
        private const val TAG = "ContentTypeDetector"

        // Content types
        const val TYPE_ACADEMIC = "academic"
        const val TYPE_NEWS = "news"
        const val TYPE_TECHNICAL = "technical"
        const val TYPE_CREATIVE = "creative"
        const val TYPE_BUSINESS = "business"
        const val TYPE_PERSONAL = "personal"
        const val TYPE_UNKNOWN = "unknown"
    }

    /**
     * Detect content type
     */
    suspend fun detectContentType(content: String): Result<ContentType> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Detecting content type for text: ${content.take(100)}...")

            // For short content, use a simple rule-based approach
            if (content.length < 500) {
                return@withContext Result.success(detectContentTypeRuleBased(content))
            }

            // For longer content, use AI to detect content type
            val prompt = "Analyze the following content and determine its type. " +
                    "Respond with exactly one of these types: academic, news, technical, creative, business, personal. " +
                    "Do not include any explanation, just the type.\n\n$content"

            val options = SummarizationOptions(
                summaryType = SummaryType.CONCISE,
                language = "en",
                maxLength = 10
            )

            val result = aiServiceManager.summarize(prompt, options, null)

            if (result.isSuccess) {
                val typeString = result.getOrNull()?.trim()?.lowercase() ?: TYPE_UNKNOWN
                val contentType = when {
                    typeString.contains(TYPE_ACADEMIC) -> ContentType.ACADEMIC
                    typeString.contains(TYPE_NEWS) -> ContentType.NEWS
                    typeString.contains(TYPE_TECHNICAL) -> ContentType.TECHNICAL
                    typeString.contains(TYPE_CREATIVE) -> ContentType.CREATIVE
                    typeString.contains(TYPE_BUSINESS) -> ContentType.BUSINESS
                    typeString.contains(TYPE_PERSONAL) -> ContentType.PERSONAL
                    else -> ContentType.UNKNOWN
                }

                Result.success(contentType)
            } else {
                // Fallback to rule-based approach if AI fails
                Result.success(detectContentTypeRuleBased(content))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting content type", e)
            Result.failure(e)
        }
    }

    /**
     * Detect content type using rule-based approach
     */
    private fun detectContentTypeRuleBased(content: String): ContentType {
        val lowercaseContent = content.lowercase()

        // Academic indicators
        val academicIndicators = listOf(
            "abstract", "introduction", "methodology", "literature review", "hypothesis",
            "conclusion", "references", "et al", "journal", "doi", "study", "research",
            "experiment", "analysis", "findings", "data", "results", "university"
        )

        // News indicators
        val newsIndicators = listOf(
            "breaking", "reported", "according to", "sources say", "officials",
            "announced", "statement", "press", "news", "report", "journalist",
            "correspondent", "editor", "headline", "byline", "dateline"
        )

        // Technical indicators
        val technicalIndicators = listOf(
            "code", "function", "algorithm", "implementation", "documentation",
            "api", "interface", "framework", "library", "module", "class",
            "method", "variable", "parameter", "return", "value", "object",
            "instance", "prototype", "inheritance", "polymorphism", "encapsulation"
        )

        // Creative indicators
        val creativeIndicators = listOf(
            "story", "novel", "poem", "fiction", "character", "plot", "setting",
            "theme", "metaphor", "simile", "imagery", "symbolism", "narrative",
            "dialogue", "scene", "chapter", "verse", "stanza", "rhyme"
        )

        // Business indicators
        val businessIndicators = listOf(
            "company", "business", "market", "industry", "product", "service",
            "customer", "client", "revenue", "profit", "loss", "sales", "marketing",
            "strategy", "management", "executive", "ceo", "cfo", "cto", "board"
        )

        // Personal indicators
        val personalIndicators = listOf(
            "i", "me", "my", "mine", "we", "us", "our", "ours", "you", "your",
            "yours", "feel", "think", "believe", "opinion", "experience", "personal",
            "diary", "journal", "blog", "today", "yesterday", "tomorrow"
        )

        // Count occurrences of indicators
        var academicCount = 0
        var newsCount = 0
        var technicalCount = 0
        var creativeCount = 0
        var businessCount = 0
        var personalCount = 0

        for (indicator in academicIndicators) {
            if (lowercaseContent.contains(indicator)) {
                academicCount++
            }
        }

        for (indicator in newsIndicators) {
            if (lowercaseContent.contains(indicator)) {
                newsCount++
            }
        }

        for (indicator in technicalIndicators) {
            if (lowercaseContent.contains(indicator)) {
                technicalCount++
            }
        }

        for (indicator in creativeIndicators) {
            if (lowercaseContent.contains(indicator)) {
                creativeCount++
            }
        }

        for (indicator in businessIndicators) {
            if (lowercaseContent.contains(indicator)) {
                businessCount++
            }
        }

        for (indicator in personalIndicators) {
            if (lowercaseContent.contains(indicator)) {
                personalCount++
            }
        }

        // Determine content type based on highest count
        val counts = mapOf(
            ContentType.ACADEMIC to academicCount,
            ContentType.NEWS to newsCount,
            ContentType.TECHNICAL to technicalCount,
            ContentType.CREATIVE to creativeCount,
            ContentType.BUSINESS to businessCount,
            ContentType.PERSONAL to personalCount
        )

        return counts.maxByOrNull { it.value }?.key ?: ContentType.UNKNOWN
    }
}

/**
 * Content type enum
 */
enum class ContentType {
    ACADEMIC,
    NEWS,
    TECHNICAL,
    CREATIVE,
    BUSINESS,
    PERSONAL,
    UNKNOWN
}
