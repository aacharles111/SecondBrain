package com.secondbrain.data.repository

import com.secondbrain.data.db.CardDao
import com.secondbrain.data.model.Card
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao
) {
    suspend fun saveCard(card: Card): Result<String> {
        return try {
            android.util.Log.d("CardRepository", "Saving card with ID: ${card.id}, title: ${card.title}, thumbnailUrl: ${card.thumbnailUrl}")
            cardDao.insertCard(card)
            Result.success(card.id)
        } catch (e: Exception) {
            android.util.Log.e("CardRepository", "Error saving card: ${card.id}", e)
            Result.failure(e)
        }
    }

    fun getCards(): Flow<List<Card>> {
        return try {
            cardDao.getAllCards()
        } catch (e: Exception) {
            android.util.Log.e("CardRepository", "Error getting cards", e)
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    fun getCardById(id: String): Flow<Card?> {
        android.util.Log.d("CardRepository", "Getting card by ID: $id")
        return cardDao.getCardById(id).also { flow ->
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    val card = flow.first()
                    android.util.Log.d("CardRepository", "Retrieved card: $id, thumbnailUrl: ${card?.thumbnailUrl}")
                } catch (e: Exception) {
                    android.util.Log.e("CardRepository", "Error retrieving card: $id", e)
                }
            }
        }
    }

    suspend fun deleteCard(id: String): Result<Boolean> {
        return try {
            cardDao.deleteCardById(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCard(card: Card): Result<Boolean> {
        return try {
            cardDao.updateCard(card)
            Result.success(true)
        } catch (e: Exception) {
            android.util.Log.e("CardRepository", "Error updating card: ${card.id}", e)
            Result.failure(e)
        }
    }

    /**
     * Update only the thumbnail URL for a card
     */
    suspend fun updateCardThumbnail(cardId: String, thumbnailUrl: String): Result<Boolean> {
        return try {
            // Get the current card
            val card = cardDao.getCardByIdSync(cardId)
            if (card != null) {
                // Update only the thumbnail URL
                val updatedCard = card.copy(thumbnailUrl = thumbnailUrl)
                cardDao.updateCard(updatedCard)
                android.util.Log.d("CardRepository", "Updated thumbnail for card: $cardId to $thumbnailUrl")
                Result.success(true)
            } else {
                android.util.Log.e("CardRepository", "Card not found: $cardId")
                Result.failure(Exception("Card not found: $cardId"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CardRepository", "Error updating card thumbnail: $cardId", e)
            Result.failure(e)
        }
    }

    fun getCardsByTags(tags: List<String>): Flow<List<Card>> {
        return cardDao.getCardsByTags(tags)
    }

    fun searchCards(query: String): Flow<List<Card>> {
        return cardDao.searchCards("%$query%")
    }
}
