package com.secondbrain.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.secondbrain.data.model.Note
import com.secondbrain.data.model.NoteSearchResult
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: String): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    /**
     * Basic search for notes
     */
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<Note>>

    /**
     * Advanced search with field-specific results
     * Returns notes where any field matches the query, and includes information about which field matched
     */
    @Query("SELECT *, "
        + "CASE "
        + "  WHEN title LIKE '%' || :query || '%' THEN 'title' "
        + "  WHEN content LIKE '%' || :query || '%' THEN 'content' "
        + "  ELSE 'unknown' "
        + "END AS matchedField "
        + "FROM notes WHERE "
        + "title LIKE '%' || :query || '%' OR "
        + "content LIKE '%' || :query || '%'")
    fun searchNotesWithMatchInfo(query: String): Flow<List<NoteSearchResult>>

    @Query("SELECT * FROM notes WHERE content LIKE '%[[' || :title || ']]%'")
    fun getBacklinks(title: String): Flow<List<Note>>
}
