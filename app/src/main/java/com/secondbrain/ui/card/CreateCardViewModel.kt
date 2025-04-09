package com.secondbrain.ui.card

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.Card
import com.secondbrain.data.model.CardType
import com.secondbrain.data.repository.CardRepository
import com.secondbrain.data.service.AiService
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.util.ContentExtractor
import com.secondbrain.util.PdfContent
import com.secondbrain.util.PdfProcessor
import com.secondbrain.util.UrlContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import android.content.Context
import android.net.Uri

@HiltViewModel
class CreateCardViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val aiService: AiService,
    private val aiServiceManager: AiServiceManager
) : ViewModel() {

    // Common states
    var selectedLanguage by mutableStateOf("English")
    var languageMenuExpanded by mutableStateOf(false)
    val supportedLanguages = listOf("English", "தமிழ் (Tamil)", "हिंदी (Hindi)")

    // Summary options
    var selectedSummaryType by mutableStateOf("Concise summary")
    var summaryOptionsExpanded by mutableStateOf(false)
    val summaryTypes = listOf(
        "Concise summary",
        "Detailed summary",
        "Bullet points",
        "Question & Answer",
        "Key facts"
    )

    // URL tab states
    var urlInput by mutableStateOf("")

    // Search tab states
    var searchQuery by mutableStateOf("")
    val searchSources = listOf("Google", "Wikipedia", "WikiData")
    val selectedSearchSources = mutableStateListOf("Google", "Wikipedia", "WikiData")

    // PDF tab states
    var extractAllText by mutableStateOf(true)
    var extractSpecificPages by mutableStateOf(false)
    var pageRanges by mutableStateOf("")
    val uploadedPdfFiles = mutableStateListOf<Uri>()
    var extractedPdfContent by mutableStateOf<PdfContent?>(null)

    // PDF upload function
    fun uploadPdf(uri: Uri, context: Context) {
        viewModelScope.launch {
            isLoading = true
            PdfProcessor.extractText(context, uri).onSuccess { pdfContent ->
                extractedPdfContent = pdfContent
                uploadedPdfFiles.add(uri)
                isLoading = false
            }.onFailure { error ->
                android.util.Log.e("CreateCardViewModel", "Error extracting PDF content", error)
                errorMessage = "Error extracting PDF content: ${error.message}"
                isLoading = false
            }
        }
    }

    // Note tab states
    var noteTitle by mutableStateOf("")
    var noteContent by mutableStateOf("")
    val noteTags = mutableStateListOf<String>()

    // Audio tab states
    var audioTitle by mutableStateOf("")
    var selectedTranscriptionLanguage by mutableStateOf("English")
    var transcriptionLanguageMenuExpanded by mutableStateOf(false)
    val uploadedAudioFiles = mutableStateListOf<String>()

    // AI model states
    var selectedAiModel by mutableStateOf("Gemini")
    var aiModelMenuExpanded by mutableStateOf(false)
    val supportedAiModels = listOf("Gemini", "OpenAI", "Claude", "DeepSeek", "Custom")

    // Error state
    var errorMessage by mutableStateOf<String?>(null)

    // Navigation state
    var navigateToSummaryReview by mutableStateOf<String?>(null)

    // Loading state
    var isLoading by mutableStateOf(false)

    // URL content state
    var extractedUrlContent by mutableStateOf<UrlContent?>(null)

    // Create card function
    fun createCard() {
        viewModelScope.launch {
            try {
                isLoading = true

                // Extract content based on the active tab
                when {
                    urlInput.isNotEmpty() -> {
                        // Extract content from URL
                        ContentExtractor.extractFromUrl(urlInput).onSuccess { urlContent ->
                            extractedUrlContent = urlContent
                            generateSummaryAndCreateCard(urlContent.content)
                        }.onFailure { error ->
                            android.util.Log.e("CreateCardViewModel", "Error extracting URL content", error)
                            errorMessage = "Error extracting URL content: ${error.message}"
                            isLoading = false
                        }
                    }
                    searchQuery.isNotEmpty() -> {
                        // For now, just use the search query as content
                        val content = "Search results for: $searchQuery"
                        generateSummaryAndCreateCard(content)
                    }
                    uploadedPdfFiles.isNotEmpty() -> {
                        // For now, just use a placeholder for PDF content
                        val content = "Content extracted from PDF"
                        generateSummaryAndCreateCard(content)
                    }
                    noteContent.isNotEmpty() -> {
                        // Use the note content directly
                        generateSummaryAndCreateCard(noteContent)
                    }
                    uploadedAudioFiles.isNotEmpty() -> {
                        // For now, just use a placeholder for audio transcription
                        val content = "Transcription of audio"
                        generateSummaryAndCreateCard(content)
                    }
                    else -> {
                        errorMessage = "Please enter some content"
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error creating card", e)
                errorMessage = "Error creating card: ${e.message}"
                isLoading = false
            }
        }
    }

    private fun generateSummaryAndCreateCard(content: String) {
        viewModelScope.launch {
            try {
                if (content.isNotEmpty()) {
                    // Generate summary using AI
                    aiService.summarize(
                        content = content,
                        summaryType = selectedSummaryType,
                        language = selectedLanguage,
                        aiModel = selectedAiModel
                    ).onSuccess { summary ->
                        // Create card with summary
                        val card = when {
                            urlInput.isNotEmpty() -> createUrlCard(summary)
                            searchQuery.isNotEmpty() -> createSearchCard(summary)
                            uploadedPdfFiles.isNotEmpty() -> createPdfCard(summary)
                            noteContent.isNotEmpty() -> createNoteCard(summary)
                            uploadedAudioFiles.isNotEmpty() -> createAudioCard(summary)
                            else -> null
                        }

                        card?.let {
                            val result = cardRepository.saveCard(it)
                            result.onSuccess { cardId ->
                                // Navigate to summary review screen with the card ID
                                navigateToSummaryReview = cardId
                            }.onFailure { error ->
                                android.util.Log.e("CreateCardViewModel", "Error saving card", error)
                                errorMessage = "Error saving card: ${error.message}"
                            }
                        }
                    }.onFailure { error ->
                        // Handle error
                        android.util.Log.e("CreateCardViewModel", "Error generating summary", error)
                        errorMessage = "Error generating summary: ${error.message}"
                    }
                } else {
                    errorMessage = "No content to summarize"
                }
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error generating summary", e)
                errorMessage = "Error generating summary: ${e.message}"
                isLoading = false
            }
        }
    }

    private fun createUrlCard(summary: String): Card {
        val urlContent = extractedUrlContent

        return Card(
            id = UUID.randomUUID().toString(),
            title = urlContent?.title ?: "URL Content",
            content = urlContent?.content ?: "URL content from: $urlInput",
            summary = summary,
            type = CardType.URL,
            source = urlInput,
            tags = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            thumbnailUrl = urlContent?.thumbnailUrl
        )
    }

    private fun createSearchCard(summary: String): Card {
        return Card(
            id = UUID.randomUUID().toString(),
            title = "Search: $searchQuery",
            content = "Search results for: $searchQuery", // This would be the search results
            summary = summary,
            type = CardType.SEARCH,
            source = "Search: $searchQuery (Sources: ${selectedSearchSources.joinToString()})",
            tags = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType
        )
    }

    private fun createPdfCard(summary: String): Card {
        val pdfContent = extractedPdfContent

        return Card(
            id = UUID.randomUUID().toString(),
            title = pdfContent?.title ?: "PDF Document",
            content = pdfContent?.content ?: "Content extracted from PDF",
            summary = summary,
            type = CardType.PDF,
            source = uploadedPdfFiles.firstOrNull()?.toString() ?: "",
            tags = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            pageCount = pdfContent?.pageCount
        )
    }

    private fun createNoteCard(summary: String): Card {
        val title = if (noteTitle.isNotEmpty()) noteTitle else "Untitled Note"
        return Card(
            id = UUID.randomUUID().toString(),
            title = title,
            content = noteContent,
            summary = summary,
            type = CardType.NOTE,
            source = "User created note",
            tags = noteTags.toList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType
        )
    }

    private fun createAudioCard(summary: String): Card {
        val title = if (audioTitle.isNotEmpty()) audioTitle else "Audio Recording"
        return Card(
            id = UUID.randomUUID().toString(),
            title = title,
            content = "Transcription of audio", // This would be the transcription
            summary = summary,
            type = CardType.AUDIO,
            source = uploadedAudioFiles.joinToString(),
            tags = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedTranscriptionLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType
        )
    }
}
