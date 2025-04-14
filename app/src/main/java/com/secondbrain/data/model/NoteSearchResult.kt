package com.secondbrain.data.model

import androidx.room.Embedded
import com.secondbrain.data.model.Note

/**
 * Data class to hold a note search result with information about which field matched
 */
data class NoteSearchResult(
    @Embedded val note: Note,
    val matchedField: String
)
