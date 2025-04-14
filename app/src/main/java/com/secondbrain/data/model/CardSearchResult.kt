package com.secondbrain.data.model

import androidx.room.Embedded
import com.secondbrain.data.model.Card

/**
 * Data class to hold a card search result with information about which field matched
 */
data class CardSearchResult(
    @Embedded val card: Card,
    val matchedField: String
)
