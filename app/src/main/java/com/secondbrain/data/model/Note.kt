package com.secondbrain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val filePath: String?, // Path to the .md file
    val sourceUrl: String?, // URL source if captured from web
    val createdAt: Date,
    val updatedAt: Date,
    val tags: List<String> = emptyList()
)
