package com.secondbrain.data.repository

import com.secondbrain.data.db.CardDao
import com.secondbrain.data.db.NoteDao
import com.secondbrain.data.model.Card
import com.secondbrain.data.model.CardSearchResult
import com.secondbrain.data.model.CardType
import com.secondbrain.data.model.Note
import com.secondbrain.data.model.NoteSearchResult
import com.secondbrain.data.model.SearchResult
import com.secondbrain.data.model.SearchResultType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val cardDao: CardDao,
    private val noteDao: NoteDao
) {
    /**
     * Search across all content types (cards and notes)
     * @param query The search query
     * @return A flow of search results
     */
    fun search(query: String): Flow<List<SearchResult>> {
        val formattedQuery = "%$query%"

        // Get card search results
        val cardResults = cardDao.searchCardsWithMatchInfo(formattedQuery)
            .map { results -> results.map { it.toSearchResult() } }

        // Get note search results
        val noteResults = noteDao.searchNotesWithMatchInfo(query)
            .map { results -> results.map { it.toSearchResult() } }

        // Combine and sort results by relevance and recency
        return cardResults.combine(noteResults) { cards, notes ->
            (cards + notes).sortedByDescending { it.updatedAt }
        }
    }

    /**
     * Convert a CardSearchResult to a unified SearchResult
     */
    private fun CardSearchResult.toSearchResult(): SearchResult {
        val cardType = when (card.type) {
            CardType.URL -> SearchResultType.CARD_URL
            CardType.PDF -> SearchResultType.CARD_PDF
            CardType.NOTE -> SearchResultType.CARD_NOTE
            CardType.AUDIO -> SearchResultType.CARD_AUDIO
            CardType.SEARCH -> SearchResultType.CARD_SEARCH
        }

        // Create a snippet based on the matched field
        val snippet = when (matchedField) {
            "title" -> card.title
            "summary" -> card.summary.take(150) + if (card.summary.length > 150) "..." else ""
            "content" -> card.content.take(150) + if (card.content.length > 150) "..." else ""
            "tags" -> "Tags: " + card.tags.joinToString(", ")
            "source" -> "Source: " + card.source
            "metadata" -> "Metadata match"
            else -> card.summary.take(150) + if (card.summary.length > 150) "..." else ""
        }

        return SearchResult(
            id = card.id,
            title = card.title,
            snippet = snippet,
            type = cardType,
            tags = card.tags,
            source = card.source,
            thumbnailUrl = card.thumbnailUrl,
            matchedField = matchedField,
            createdAt = card.createdAt,
            updatedAt = card.updatedAt
        )
    }

    /**
     * Convert a NoteSearchResult to a unified SearchResult
     */
    private fun NoteSearchResult.toSearchResult(): SearchResult {
        // Create a snippet based on the matched field
        val snippet = when (matchedField) {
            "title" -> note.title
            "content" -> note.content.take(150) + if (note.content.length > 150) "..." else ""
            else -> note.content.take(150) + if (note.content.length > 150) "..." else ""
        }

        return SearchResult(
            id = note.id,
            title = note.title,
            snippet = snippet,
            type = SearchResultType.NOTE,
            matchedField = matchedField,
            createdAt = note.createdAt.time,
            updatedAt = note.updatedAt.time
        )
    }

    /**
     * Filter search results by type
     */
    fun filterByType(results: List<SearchResult>, type: SearchResultType?): List<SearchResult> {
        return if (type == null) {
            results
        } else {
            results.filter { it.type == type }
        }
    }
}
