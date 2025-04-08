package com.secondbrain.data.repository

import android.content.Context
import com.secondbrain.data.db.NoteDao
import com.secondbrain.data.model.Note
import com.secondbrain.util.MarkdownUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    @ApplicationContext private val context: Context
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getNoteById(id: String): Flow<Note?> = noteDao.getNoteById(id)

    suspend fun createNote(title: String, content: String, sourceUrl: String? = null): Note {
        val now = Date()

        // Save the note as a Markdown file
        val filePath = MarkdownUtils.saveMarkdownFile(
            context = context,
            title = title,
            content = content,
            sourceUrl = sourceUrl
        )

        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            filePath = filePath,
            sourceUrl = sourceUrl,
            createdAt = now,
            updatedAt = now
        )
        noteDao.insertNote(note)
        return note
    }

    suspend fun updateNote(note: Note): Note {
        val updatedNote = note.copy(updatedAt = Date())

        // Update the Markdown file if it exists
        if (updatedNote.filePath != null) {
            MarkdownUtils.saveMarkdownFile(
                context = context,
                title = updatedNote.title,
                content = updatedNote.content,
                sourceUrl = updatedNote.sourceUrl
            )
        }

        noteDao.updateNote(updatedNote)
        return updatedNote
    }

    suspend fun deleteNote(note: Note) {
        // Delete the Markdown file if it exists
        if (note.filePath != null) {
            MarkdownUtils.deleteMarkdownFile(note.filePath)
        }

        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(id: String) {
        // Get the note first to get the file path
        val note = noteDao.getNoteById(id).value
        if (note != null) {
            deleteNote(note)
        } else {
            noteDao.deleteNoteById(id)
        }
    }

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    fun getBacklinks(title: String): Flow<List<Note>> = noteDao.getBacklinks(title)
}
