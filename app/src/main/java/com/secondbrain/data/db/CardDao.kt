package com.secondbrain.data.db

import androidx.room.*
import com.secondbrain.data.model.Card
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)
    
    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<Card>>
    
    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardById(id: String): Flow<Card?>
    
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
    
    @Query("SELECT * FROM cards WHERE title LIKE :query OR content LIKE :query OR summary LIKE :query")
    fun searchCards(query: String): Flow<List<Card>>
}
