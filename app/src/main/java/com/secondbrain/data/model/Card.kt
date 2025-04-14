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
    val pageCount: Int? = null,
    // YouTube-specific fields
    val videoId: String? = null,
    val channelTitle: String? = null,
    val videoDuration: String? = null,
    val viewCount: String? = null,
    val hasTranscript: Boolean = false,
    // Additional metadata as JSON string
    val metadata: String? = null
)

enum class CardType {
    URL, SEARCH, PDF, NOTE, AUDIO
}
