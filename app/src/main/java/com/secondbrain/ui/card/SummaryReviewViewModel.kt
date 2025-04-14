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
    var sourceUrl by mutableStateOf("")
    var language by mutableStateOf("English")
    var aiModel by mutableStateOf("Gemini")
    var thumbnailUrl by mutableStateOf<String?>(null)
    val tags = mutableStateListOf<String>()

    // Original content for regeneration
    private var originalContent = ""
    private var summaryType = ""

    // Error state
    var errorMessage by mutableStateOf<String?>(null)

    // Loading state
    var isLoading by mutableStateOf(false)

    // Last operation state for retry functionality
    private var lastOperation: (() -> Unit)? = null

    fun retryLastOperation() {
        lastOperation?.invoke()
    }

    // This method is called directly from the SummaryReviewScreen
    fun loadCardById(id: String) {
        android.util.Log.d("SummaryReviewViewModel", "loadCardById called with id: $id")
        if (id.isNotEmpty()) {
            loadCard(id)
        } else {
            errorMessage = "Invalid card ID"
        }
    }

    private fun loadCard(id: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                android.util.Log.d("SummaryReviewViewModel", "loadCard: Starting to load card with id: $id")
                cardRepository.getCardById(id).collect { card ->
                    if (card != null) {
                        android.util.Log.d("SummaryReviewViewModel", "loadCard: Card found: ${card.id}, title: ${card.title}, summary length: ${card.summary.length}")
                        cardId = card.id
                        title = card.title
                        summary = card.summary
                        sourceType = card.type.name
                        sourceUrl = card.source
                        language = card.language
                        aiModel = card.aiModel
                        thumbnailUrl = card.thumbnailUrl
                        tags.clear()
                        tags.addAll(card.tags)
                        originalContent = card.content
                        summaryType = card.summaryType

                        android.util.Log.d("SummaryReviewViewModel", "loadCard: Loaded thumbnailUrl: ${card.thumbnailUrl}")

                        // Check if summary is empty and try to regenerate it
                        if (summary.isBlank()) {
                            android.util.Log.d("SummaryReviewViewModel", "loadCard: Summary is empty, attempting to regenerate")
                            regenerateSummary()
                        }

                        // If there are no tags, generate them
                        if (tags.isEmpty()) {
                            generateTags()
                        }

                        // If there's no title, generate one
                        if (title.isEmpty() || title.startsWith("Untitled") || title.startsWith("Search:")) {
                            generateTitle()
                        }
                    } else {
                        android.util.Log.e("SummaryReviewViewModel", "loadCard: Card not found for id: $id")
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
        // Save operation for retry
        lastOperation = { regenerateSummary() }

        viewModelScope.launch {
            try {
                isLoading = true
                android.util.Log.d("SummaryReviewViewModel", "regenerateSummary: Attempting to regenerate summary")

                aiService.summarize(
                    content = originalContent,
                    summaryType = summaryType,
                    language = language,
                    aiModel = aiModel
                ).onSuccess { newSummary ->
                    if (newSummary.isBlank()) {
                        android.util.Log.e("SummaryReviewViewModel", "regenerateSummary: Generated summary is empty")
                        errorMessage = "Failed to generate summary. Please try again or use a different AI model."
                    } else {
                        android.util.Log.d("SummaryReviewViewModel", "regenerateSummary: Successfully generated new summary of length ${newSummary.length}")
                        summary = newSummary

                        // Save the updated card with the new summary
                        saveCard()
                    }
                }.onFailure { error ->
                    // Handle error
                    android.util.Log.e("SummaryReviewViewModel", "Error regenerating summary", error)

                    // Provide more user-friendly error messages
                    errorMessage = when (error) {
                        is com.secondbrain.util.ApiServerOverloadException -> {
                            "The AI server is currently overloaded. Please try again in a few moments."
                        }
                        is com.secondbrain.util.ApiPaymentRequiredException -> {
                            val errorMsg = error.message?.substringAfter("Payment required: ") ?: ""
                            if (errorMsg.contains("max_tokens", ignoreCase = true)) {
                                // This is a token limit issue, provide a more helpful message
                                "OpenRouter token limit exceeded. Try using a different model or reducing the content length."
                            } else {
                                "OpenRouter requires more credits: $errorMsg"
                            }
                        }
                        is com.secondbrain.util.ApiRateLimitException -> {
                            "Rate limit exceeded. Please try again later."
                        }
                        is com.secondbrain.util.ApiAuthenticationException -> {
                            "Authentication error. Please check your API key in settings."
                        }
                        is com.secondbrain.util.ApiTemporaryErrorException -> {
                            "Temporary server error. Please try again in a few moments."
                        }
                        else -> "Error generating summary: ${error.message}"
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SummaryReviewViewModel", "Error in regenerateSummary", e)
                errorMessage = "Error regenerating summary: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Generate tags for the card content
     */
    fun generateTags() {
        viewModelScope.launch {
            try {
                isLoading = true
                android.util.Log.d("SummaryReviewViewModel", "generateTags: Attempting to generate tags")

                aiService.extractTags(
                    content = originalContent,
                    language = language,
                    aiModel = aiModel,
                    maxTags = 15 // Generate between 10-20 tags
                ).onSuccess { newTags ->
                    android.util.Log.d("SummaryReviewViewModel", "generateTags: Successfully generated ${newTags.size} tags")
                    tags.clear()
                    tags.addAll(newTags)

                    // Save the updated card with the new tags
                    saveCard()
                }.onFailure { error ->
                    android.util.Log.e("SummaryReviewViewModel", "Error generating tags", error)
                    errorMessage = "Error generating tags: ${error.message}"
                }
            } catch (e: Exception) {
                android.util.Log.e("SummaryReviewViewModel", "Error generating tags", e)
                errorMessage = "Error generating tags: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Generate a title for the card content
     */
    fun generateTitle() {
        viewModelScope.launch {
            try {
                aiService.generateTitle(
                    content = originalContent,
                    language = language,
                    aiModel = aiModel
                ).onSuccess { newTitle ->
                    title = newTitle
                }.onFailure { error ->
                    android.util.Log.e("SummaryReviewViewModel", "Error generating title", error)
                }
            } catch (e: Exception) {
                android.util.Log.e("SummaryReviewViewModel", "Error generating title", e)
            }
        }
    }

    fun saveCard() {
        // Save operation for retry
        lastOperation = { saveCard() }

        viewModelScope.launch {
            try {
                isLoading = true
                android.util.Log.d("SummaryReviewViewModel", "saveCard: Saving card with thumbnailUrl: $thumbnailUrl")
                val card = Card(
                    id = cardId,
                    title = title,
                    content = originalContent,
                    summary = summary,
                    type = CardType.valueOf(sourceType),
                    source = sourceUrl,
                    tags = tags.toList(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    language = language,
                    aiModel = aiModel,
                    summaryType = summaryType,
                    thumbnailUrl = thumbnailUrl
                )

                cardRepository.updateCard(card).onSuccess {
                    // Card saved successfully
                    android.util.Log.d("SummaryReviewViewModel", "Card saved successfully")
                }.onFailure { error ->
                    // Handle error
                    errorMessage = "Error saving card: ${error.message}"
                    android.util.Log.e("SummaryReviewViewModel", "Error saving card", error)
                }
            } catch (e: Exception) {
                errorMessage = "Error saving card: ${e.message}"
                android.util.Log.e("SummaryReviewViewModel", "Error saving card", e)
            } finally {
                isLoading = false
            }
        }
    }
}
