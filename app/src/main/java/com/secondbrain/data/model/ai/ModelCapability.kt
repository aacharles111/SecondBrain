package com.secondbrain.data.model.ai

/**
 * Represents the cost tier of an AI model
 */
enum class CostTier {
    FREE,
    LOW_COST,
    MEDIUM_COST,
    HIGH_COST
}

/**
 * Represents user preferences for model cost
 */
enum class CostPreference {
    FREE_ONLY,
    PREFER_FREE,
    BALANCED,
    QUALITY_FIRST
}

/**
 * Represents the content types that can be processed
 */
enum class ContentType {
    TEXT,
    IMAGE,
    PDF,
    AUDIO,
    WEB_LINK,
    YOUTUBE
}

/**
 * Represents the features supported by an AI model
 */
enum class Feature {
    SUMMARIZATION,
    TAG_GENERATION,
    TITLE_GENERATION,
    CODE_UNDERSTANDING
}

/**
 * Represents the task types that can be performed
 */
enum class TaskType {
    SHORT_TEXT_SUMMARY,
    LONG_DOCUMENT_SUMMARY,
    IMAGE_ANALYSIS,
    AUDIO_TRANSCRIPTION,
    WEB_CONTENT_EXTRACTION,
    YOUTUBE_SUMMARY,
    CODE_EXPLANATION
}

/**
 * Represents the provider of an AI model
 */
enum class Provider {
    OPENAI,
    ANTHROPIC,
    GOOGLE,
    DEEPSEEK,
    OPENROUTER,
    OTHER
}

/**
 * Represents the capabilities of an AI model
 */
data class ModelCapability(
    val id: String,
    val name: String,
    val costTier: CostTier,
    val estimatedCostPer1KTokens: Float? = null, // null for free models
    val provider: Provider,
    val supportedContentTypes: Set<ContentType>,
    val maxTokens: Int,
    val features: Set<Feature>,
    val reliabilityScore: Float, // 0.0-1.0
    val recommendedFor: Set<TaskType> = emptySet()
)
