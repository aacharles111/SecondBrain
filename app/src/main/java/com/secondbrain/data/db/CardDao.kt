package com.secondbrain.data.db

import androidx.room.*
import com.secondbrain.data.model.Card
import com.secondbrain.data.model.CardSearchResult
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardById(id: String): Flow<Card?>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardByIdSync(id: String): Card?

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCardById(id: String)

    @Update
    suspend fun updateCard(card: Card)

    @Query("SELECT * FROM cards WHERE tags LIKE '%' || :tag || '%'")
    fun getCardsByTag(tag: String): Flow<List<Card>>

    // This is a simplified implementation. In a real app, you would need a more sophisticated
    // approach to handle tag searches properly, possibly with a junction table.
    fun getCardsByTags(tags: List<String>): Flow<List<Card>> {
        // This is a placeholder. In a real implementation, you would query based on the tags.
        return getAllCards()
    }

    /**
     * Advanced search across all card fields
     * Searches in title, content, summary, source, tags, and metadata
     */
    @Query("SELECT * FROM cards WHERE "
        + "title LIKE :query OR "
        + "content LIKE :query OR "
        + "summary LIKE :query OR "
        + "source LIKE :query OR "
        + "tags LIKE :query OR "
        + "metadata LIKE :query")
    fun searchCards(query: String): Flow<List<Card>>

    /**
     * Advanced search with field-specific results
     * Returns cards where any field matches the query, and includes information about which field matched
     */
    @Query("SELECT *, "
        + "CASE "
        + "  WHEN title LIKE :query THEN 'title' "
        + "  WHEN content LIKE :query THEN 'content' "
        + "  WHEN summary LIKE :query THEN 'summary' "
        + "  WHEN source LIKE :query THEN 'source' "
        + "  WHEN tags LIKE :query THEN 'tags' "
        + "  WHEN metadata LIKE :query THEN 'metadata' "
        + "  ELSE 'unknown' "
        + "END AS matchedField "
        + "FROM cards WHERE "
        + "title LIKE :query OR "
        + "content LIKE :query OR "
        + "summary LIKE :query OR "
        + "source LIKE :query OR "
        + "tags LIKE :query OR "
        + "metadata LIKE :query")
    fun searchCardsWithMatchInfo(query: String): Flow<List<CardSearchResult>>
}
