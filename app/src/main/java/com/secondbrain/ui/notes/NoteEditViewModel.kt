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
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val noteId: String? = savedStateHandle["noteId"]
    
    private val _noteState = MutableStateFlow<NoteEditState>(NoteEditState.Loading)
    val noteState: StateFlow<NoteEditState> = _noteState
    
    private val _titleState = MutableStateFlow("")
    val titleState: StateFlow<String> = _titleState
    
    private val _contentState = MutableStateFlow("")
    val contentState: StateFlow<String> = _contentState
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Initial)
    val saveState: StateFlow<SaveState> = _saveState
    
    init {
        if (noteId != null) {
            loadNote()
        } else {
            _noteState.value = NoteEditState.NewNote
        }
    }
    
    private fun loadNote() {
        viewModelScope.launch {
            noteRepository.getNoteById(noteId!!)
                .catch { e ->
                    _noteState.value = NoteEditState.Error(e.message ?: "Unknown error")
                }
                .collect { note ->
                    if (note != null) {
                        _noteState.value = NoteEditState.ExistingNote(note)
                        _titleState.value = note.title
                        _contentState.value = note.content
                    } else {
                        _noteState.value = NoteEditState.Error("Note not found")
                    }
                }
        }
    }
    
    fun updateTitle(title: String) {
        _titleState.value = title
    }
    
    fun updateContent(content: String) {
        _contentState.value = content
    }
    
    fun saveNote() {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            
            try {
                val title = _titleState.value.takeIf { it.isNotBlank() } ?: "Untitled Note"
                val content = _contentState.value
                
                val note = when (val state = _noteState.value) {
                    is NoteEditState.ExistingNote -> {
                        noteRepository.updateNote(state.note.copy(title = title, content = content))
                    }
                    is NoteEditState.NewNote -> {
                        noteRepository.createNote(title = title, content = content)
                    }
                    else -> {
                        _saveState.value = SaveState.Error("Cannot save note in current state")
                        return@launch
                    }
                }
                
                _saveState.value = SaveState.Success(note.id)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class NoteEditState {
    object Loading : NoteEditState()
    data class ExistingNote(val note: Note) : NoteEditState()
    object NewNote : NoteEditState()
    data class Error(val message: String) : NoteEditState()
}

sealed class SaveState {
    object Initial : SaveState()
    object Saving : SaveState()
    data class Success(val noteId: String) : SaveState()
    data class Error(val message: String) : SaveState()
}
