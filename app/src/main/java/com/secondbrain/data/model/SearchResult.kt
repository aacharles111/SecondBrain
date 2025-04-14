package com.secondbrain.data.model

/**
 * Unified search result model that can represent either a Card or Note
 */
data class SearchResult(
    val id: String,
    val title: String,
    val snippet: String,
    val type: SearchResultType,
    val tags: List<String> = emptyList(),
    val source: String? = null,
    val thumbnailUrl: String? = null,
    val matchedField: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Type of search result
 */
enum class SearchResultType {
    CARD_URL,
    CARD_PDF,
    CARD_NOTE,
    CARD_AUDIO,
    CARD_SEARCH,
    NOTE
}
