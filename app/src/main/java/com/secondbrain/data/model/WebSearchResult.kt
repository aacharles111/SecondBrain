package com.secondbrain.data.model

/**
 * Data class representing a web search result
 */
data class WebSearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String, // Google, Wikipedia, WikiData
    val thumbnailUrl: String? = null,
    val isSelected: Boolean = false
)
