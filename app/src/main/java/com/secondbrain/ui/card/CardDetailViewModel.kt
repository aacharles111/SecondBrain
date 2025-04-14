package com.secondbrain.ui.card

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
class CardDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {
    
    private val _cardState = MutableStateFlow<CardDetailState>(CardDetailState.Loading)
    val cardState: StateFlow<CardDetailState> = _cardState
    
    fun loadCard(cardId: String) {
        viewModelScope.launch {
            try {
                _cardState.value = CardDetailState.Loading
                
                cardRepository.getCardById(cardId).collect { card ->
                    if (card != null) {
                        _cardState.value = CardDetailState.Success(card)
                    } else {
                        _cardState.value = CardDetailState.Error("Card not found")
                    }
                }
            } catch (e: Exception) {
                _cardState.value = CardDetailState.Error("Error loading card: ${e.message}")
            }
        }
    }
    
    fun deleteCard(cardId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = cardRepository.deleteCard(cardId)
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    _cardState.value = CardDetailState.Error("Error deleting card: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _cardState.value = CardDetailState.Error("Error deleting card: ${e.message}")
            }
        }
    }
    
    fun duplicateCard(card: Card, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Create a copy with a new ID
                val duplicatedCard = card.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "${card.title} (Copy)",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                val result = cardRepository.saveCard(duplicatedCard)
                if (result.isSuccess) {
                    onSuccess(result.getOrNull() ?: "")
                } else {
                    _cardState.value = CardDetailState.Error("Error duplicating card: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _cardState.value = CardDetailState.Error("Error duplicating card: ${e.message}")
            }
        }
    }
}

sealed class CardDetailState {
    object Loading : CardDetailState()
    data class Success(val card: Card) : CardDetailState()
    data class Error(val message: String) : CardDetailState()
}
