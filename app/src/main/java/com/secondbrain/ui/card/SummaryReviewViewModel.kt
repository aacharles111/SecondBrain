package com.secondbrain.ui.card

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.Card
import com.secondbrain.data.model.CardType
import com.secondbrain.data.repository.CardRepository
import com.secondbrain.data.service.AiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryReviewViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val aiService: AiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Card data
    var cardId by mutableStateOf("")
    var title by mutableStateOf("")
    var summary by mutableStateOf("")
    var sourceType by mutableStateOf("")
    var language by mutableStateOf("English")
    var aiModel by mutableStateOf("Gemini")
    val tags = mutableStateListOf<String>()

    // Original content for regeneration
    private var originalContent = ""
    private var summaryType = ""

    // Error state
    var errorMessage by mutableStateOf<String?>(null)

    // Loading state
    var isLoading by mutableStateOf(false)

    init {
        // Load the card data from the savedStateHandle
        savedStateHandle.get<String>("cardId")?.let { id ->
            if (id.isNotEmpty()) {
                loadCard(id)
            } else {
                errorMessage = "Invalid card ID"
            }
        }
    }

    private fun loadCard(id: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                cardRepository.getCardById(id).collect { card ->
                    if (card != null) {
                        cardId = card.id
                        title = card.title
                        summary = card.summary
                        sourceType = card.type.name
                        language = card.language
                        aiModel = card.aiModel
                        tags.clear()
                        tags.addAll(card.tags)
                        originalContent = card.content
                        summaryType = card.summaryType
                    } else {
                        errorMessage = "Card not found"
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                android.util.Log.e("SummaryReviewViewModel", "Error loading card", e)
                errorMessage = "Error loading card: ${e.message}"
                isLoading = false
            }
        }
    }

    fun regenerateSummary() {
        viewModelScope.launch {
            aiService.summarize(
                content = originalContent,
                summaryType = summaryType,
                language = language,
                aiModel = aiModel
            ).onSuccess { newSummary ->
                summary = newSummary
            }.onFailure { error ->
                // Handle error
                summary += "\n\n[Error regenerating summary: ${error.message}]"
            }
        }
    }

    fun saveCard() {
        viewModelScope.launch {
            val card = Card(
                id = cardId,
                title = title,
                content = originalContent,
                summary = summary,
                type = CardType.valueOf(sourceType),
                source = "Source placeholder", // This would be the actual source
                tags = tags.toList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                language = language,
                aiModel = aiModel,
                summaryType = summaryType
            )

            cardRepository.updateCard(card)
        }
    }
}
