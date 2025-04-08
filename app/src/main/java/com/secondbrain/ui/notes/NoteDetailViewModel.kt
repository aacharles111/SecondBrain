package com.secondbrain.ui.notes

import androidx.lifecycle.SavedStateHandle
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
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val noteId: String = checkNotNull(savedStateHandle["noteId"])
    
    private val _noteState = MutableStateFlow<NoteDetailState>(NoteDetailState.Loading)
    val noteState: StateFlow<NoteDetailState> = _noteState
    
    private val _backlinksState = MutableStateFlow<BacklinksState>(BacklinksState.Loading)
    val backlinksState: StateFlow<BacklinksState> = _backlinksState
    
    init {
        loadNote()
    }
    
    private fun loadNote() {
        viewModelScope.launch {
            noteRepository.getNoteById(noteId)
                .catch { e ->
                    _noteState.value = NoteDetailState.Error(e.message ?: "Unknown error")
                }
                .collect { note ->
                    if (note != null) {
                        _noteState.value = NoteDetailState.Success(note)
                        loadBacklinks(note.title)
                    } else {
                        _noteState.value = NoteDetailState.Error("Note not found")
                    }
                }
        }
    }
    
    private fun loadBacklinks(title: String) {
        viewModelScope.launch {
            noteRepository.getBacklinks(title)
                .catch { e ->
                    _backlinksState.value = BacklinksState.Error(e.message ?: "Unknown error")
                }
                .collect { backlinks ->
                    _backlinksState.value = BacklinksState.Success(backlinks)
                }
        }
    }
    
    fun deleteNote() {
        viewModelScope.launch {
            noteRepository.deleteNoteById(noteId)
        }
    }
}

sealed class NoteDetailState {
    object Loading : NoteDetailState()
    data class Success(val note: Note) : NoteDetailState()
    data class Error(val message: String) : NoteDetailState()
}

sealed class BacklinksState {
    object Loading : BacklinksState()
    data class Success(val backlinks: List<Note>) : BacklinksState()
    data class Error(val message: String) : BacklinksState()
}
