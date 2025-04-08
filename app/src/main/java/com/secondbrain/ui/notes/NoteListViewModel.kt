package com.secondbrain.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.Note
import com.secondbrain.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    private val _notesState = MutableStateFlow<NotesState>(NotesState.Loading)
    val notesState: StateFlow<NotesState> = _notesState
    
    init {
        loadNotes()
    }
    
    private fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes()
                .catch { e ->
                    _notesState.value = NotesState.Error(e.message ?: "Unknown error")
                }
                .collect { notes ->
                    _notesState.value = NotesState.Success(notes)
                }
        }
    }
    
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNoteById(noteId)
        }
    }
}

sealed class NotesState {
    object Loading : NotesState()
    data class Success(val notes: List<Note>) : NotesState()
    data class Error(val message: String) : NotesState()
}
