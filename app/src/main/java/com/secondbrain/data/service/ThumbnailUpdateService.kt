package com.secondbrain.data.service

import android.util.Log
import com.secondbrain.data.model.Card
import com.secondbrain.data.repository.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailUpdateService @Inject constructor(
    private val cardRepository: CardRepository,
    private val thumbnailService: ThumbnailService
) {
    private val TAG = "ThumbnailUpdateService"
    
    /**
     * Generate thumbnails for all cards that don't have one
     */
    suspend fun updateAllCardThumbnails() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting thumbnail update for all cards")
            
            // Get all cards
            val cards = cardRepository.getCards().first()
            
            // Count of cards that need thumbnails
            val cardsWithoutThumbnails = cards.filter { it.thumbnailUrl.isNullOrEmpty() }
            Log.d(TAG, "Found ${cardsWithoutThumbnails.size} cards without thumbnails out of ${cards.size} total cards")
            
            // Update thumbnails for each card
            cardsWithoutThumbnails.forEach { card ->
                updateCardThumbnail(card)
            }
            
            Log.d(TAG, "Finished updating thumbnails for all cards")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating thumbnails for all cards", e)
        }
    }
    
    /**
     * Generate a thumbnail for a specific card
     */
    suspend fun updateCardThumbnail(card: Card) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating thumbnail for card: ${card.id}, type: ${card.type}")
            
            // Skip if the card already has a thumbnail
            if (!card.thumbnailUrl.isNullOrEmpty()) {
                Log.d(TAG, "Card already has a thumbnail: ${card.thumbnailUrl}")
                return@withContext
            }
            
            // Generate a thumbnail based on the card type
            val thumbnailUrl = thumbnailService.getThumbnailForUrl(
                url = card.source,
                type = card.type,
                title = card.title
            )
            
            if (!thumbnailUrl.isNullOrEmpty()) {
                Log.d(TAG, "Generated thumbnail: $thumbnailUrl for card: ${card.id}")
                
                // Update the card with the new thumbnail URL
                val result = cardRepository.updateCardThumbnail(card.id, thumbnailUrl)
                result.onSuccess {
                    Log.d(TAG, "Successfully updated thumbnail for card: ${card.id}")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to update thumbnail for card: ${card.id}", error)
                }
            } else {
                Log.e(TAG, "Failed to generate thumbnail for card: ${card.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating thumbnail for card: ${card.id}", e)
        }
    }
}
