package com.secondbrain.ui.card

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.Card
import com.secondbrain.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardEditViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {
    
    private val _cardState = MutableStateFlow<CardEditState>(CardEditState.Loading)
    val cardState: StateFlow<CardEditState> = _cardState
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving
    
    // Card properties
    var title by mutableStateOf("")
        internal set
    
    var summary by mutableStateOf("")
        internal set
    
    var content by mutableStateOf("")
        internal set
    
    var tagsText by mutableStateOf("")
        internal set
    
    private var originalCard: Card? = null
    
    fun loadCard(cardId: String) {
        viewModelScope.launch {
            try {
                _cardState.value = CardEditState.Loading
                
                cardRepository.getCardById(cardId).collect { card ->
                    if (card != null) {
                        originalCard = card
                        title = card.title
                        summary = card.summary
                        content = card.content
                        tagsText = card.tags.joinToString(", ")
                        
                        _cardState.value = CardEditState.Success(card)
                    } else {
                        _cardState.value = CardEditState.Error("Card not found")
                    }
                }
            } catch (e: Exception) {
                _cardState.value = CardEditState.Error("Error loading card: ${e.message}")
            }
        }
    }
    
    fun updateTags(newTagsText: String) {
        tagsText = newTagsText
    }
    
    fun saveCard(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                
                val card = originalCard
                if (card != null) {
                    // Parse tags from comma-separated text
                    val tags = tagsText.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    val updatedCard = card.copy(
                        title = title,
                        summary = summary,
                        content = content,
                        tags = tags,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    val result = cardRepository.updateCard(updatedCard)
                    if (result.isSuccess) {
                        onSuccess()
                    } else {
                        _cardState.value = CardEditState.Error("Error saving card: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    _cardState.value = CardEditState.Error("No card to save")
                }
            } catch (e: Exception) {
                _cardState.value = CardEditState.Error("Error saving card: ${e.message}")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

sealed class CardEditState {
    object Loading : CardEditState()
    data class Success(val card: Card) : CardEditState()
    data class Error(val message: String) : CardEditState()
}
