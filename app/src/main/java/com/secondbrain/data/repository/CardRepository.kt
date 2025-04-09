package com.secondbrain.data.repository

import com.secondbrain.data.db.CardDao
import com.secondbrain.data.model.Card
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao
) {
    suspend fun saveCard(card: Card): Result<String> {
        return try {
            cardDao.insertCard(card)
            Result.success(card.id)
        } catch (e: Exception) {
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
        return cardDao.getCardById(id)
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
