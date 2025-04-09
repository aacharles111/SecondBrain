package com.secondbrain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.secondbrain.data.db.Converters

@Entity(tableName = "cards")
@TypeConverters(Converters::class)
data class Card(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val summary: String,
    val type: CardType,
    val source: String,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val language: String,
    val aiModel: String,
    val summaryType: String,
    val thumbnailUrl: String? = null,
    val pageCount: Int? = null
)

enum class CardType {
    URL, SEARCH, PDF, NOTE, AUDIO
}
