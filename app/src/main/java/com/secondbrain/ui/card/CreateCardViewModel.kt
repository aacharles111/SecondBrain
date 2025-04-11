package com.secondbrain.ui.card

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.Card
import com.secondbrain.data.model.CardType
import com.secondbrain.data.model.WebSearchResult
import com.secondbrain.data.repository.CardRepository
import com.secondbrain.data.service.AiService
import com.secondbrain.data.service.WebSearchService
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
    private val aiServiceManager: AiServiceManager,
    private val contentExtractor: ContentExtractor,
    private val webSearchService: WebSearchService
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

    // AI model options
    var selectedAiModel by mutableStateOf("gpt-3.5-turbo")
    var aiModelOptionsExpanded by mutableStateOf(false)
    var aiModels = mutableStateListOf<String>()
    var isLoadingModels by mutableStateOf(false)

    // Summary toggle states for each tab (default is ON)
    var urlSummaryEnabled by mutableStateOf(true)
    var searchSummaryEnabled by mutableStateOf(true)
    var pdfSummaryEnabled by mutableStateOf(true)
    var noteSummaryEnabled by mutableStateOf(true)
    var audioSummaryEnabled by mutableStateOf(true)

    // URL tab states
    var urlInput by mutableStateOf("")

    // Search tab states
    var searchQuery by mutableStateOf("")
    val searchSources = listOf("Web", "Wikipedia", "WikiData")
    val selectedSearchSources = mutableStateListOf("Web", "Wikipedia", "WikiData")
    val searchResults = mutableStateListOf<WebSearchResult>()
    var isSearching by mutableStateOf(false)
    var selectedSearchResult by mutableStateOf<WebSearchResult?>(null)
    var searchError by mutableStateOf<String?>(null)

    // PDF tab states
    var extractAllText by mutableStateOf(true)
    var extractSpecificPages by mutableStateOf(false)
    var pageRanges by mutableStateOf("")
    val uploadedPdfFiles = mutableStateListOf<Uri>()
    var extractedPdfContent by mutableStateOf<PdfContent?>(null)

    // Note tab states
    var noteTitle by mutableStateOf("")
    var noteContent by mutableStateOf("")
    val noteTags = mutableStateListOf<String>()

    // Audio tab states
    var audioTitle by mutableStateOf("")
    var selectedTranscriptionLanguage by mutableStateOf("English")
    var transcriptionLanguageMenuExpanded by mutableStateOf(false)
    val uploadedAudioFiles = mutableStateListOf<String>()

    // Loading and error states
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var extractedUrlContent by mutableStateOf<UrlContent?>(null)

    // Navigation state
    var navigateToSummaryReview by mutableStateOf<String?>(null)

    init {
        loadAiModels()
    }

    private fun loadAiModels() {
        viewModelScope.launch {
            isLoadingModels = true
            try {
                // This is a placeholder - in the real app, we would call aiServiceManager.getAvailableModels()
                val models = listOf("gpt-3.5-turbo", "gpt-4", "claude-3-opus", "claude-3-sonnet")
                aiModels.clear()
                aiModels.addAll(models)
                if (aiModels.isNotEmpty() && !aiModels.contains(selectedAiModel)) {
                    selectedAiModel = aiModels.first()
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error loading AI models", e)
                // Add some default models if loading fails
                aiModels.clear()
                aiModels.addAll(listOf("gpt-3.5-turbo", "gpt-4"))
            } finally {
                isLoadingModels = false
            }
        }
    }

    fun extractUrlContent(context: Context) {
        if (urlInput.isBlank()) {
            errorMessage = "Please enter a URL"
            return
        }

        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val contentResult = contentExtractor.extractFromUrl(urlInput)
                if (contentResult.isSuccess) {
                    extractedUrlContent = contentResult.getOrNull()
                    if (extractedUrlContent != null) {
                        generateSummaryAndCreateCard(extractedUrlContent!!.content)
                    } else {
                        errorMessage = "Error extracting content: Unable to extract content"
                        isLoading = false
                    }
                } else {
                    errorMessage = "Error extracting content: ${contentResult.exceptionOrNull()?.message ?: "Unknown error"}"
                    isLoading = false
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error extracting URL content", e)
                errorMessage = "Error extracting content: ${e.message}"
                isLoading = false
            }
        }
    }

    fun uploadPdf(uri: Uri, context: Context) {
        uploadedPdfFiles.clear()
        uploadedPdfFiles.add(uri)
        extractPdfContent(context, uri)
    }

    fun extractPdfContent(context: Context, uri: Uri) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val contentResult = if (extractSpecificPages && pageRanges.isNotBlank()) {
                    // This is a placeholder since we don't have a method to extract specific pages
                    PdfProcessor.extractText(context, uri)
                } else {
                    PdfProcessor.extractText(context, uri)
                }

                if (contentResult.isSuccess) {
                    extractedPdfContent = contentResult.getOrNull()
                } else {
                    throw contentResult.exceptionOrNull() ?: Exception("Failed to extract PDF content")
                }
                if (extractedPdfContent != null) {
                    generateSummaryAndCreateCard(extractedPdfContent!!.content)
                } else {
                    throw Exception("Failed to extract PDF content")
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error extracting PDF content", e)
                errorMessage = "Error extracting PDF content: ${e.message}"
                isLoading = false
            }
        }
    }

    private fun parsePageRanges(input: String): List<Int> {
        val pages = mutableListOf<Int>()
        val ranges = input.split(",")

        for (range in ranges) {
            val trimmedRange = range.trim()
            if (trimmedRange.contains("-")) {
                val (start, end) = trimmedRange.split("-").map { it.trim().toIntOrNull() ?: 0 }
                if (start > 0 && end >= start) {
                    pages.addAll(start..end)
                }
            } else {
                val page = trimmedRange.toIntOrNull() ?: 0
                if (page > 0) {
                    pages.add(page)
                }
            }
        }

        return pages
    }

    fun createNoteCard() {
        if (noteContent.isBlank()) {
            errorMessage = "Please enter some content for your note"
            return
        }

        isLoading = true
        errorMessage = null
        generateSummaryAndCreateCard(noteContent)
    }

    fun createAudioCard() {
        // Placeholder for audio card creation
        errorMessage = "Audio card creation not implemented yet"
    }

    fun createCardFromActiveTab() {
        errorMessage = null
        when {
            urlInput.isNotEmpty() -> {
                // URL tab is active
                extractUrlContent(android.app.Application())
            }
            searchQuery.isNotEmpty() -> {
                // Search tab is active
                val selectedResult = selectedSearchResult
                val content = if (selectedResult != null) {
                    "Title: ${selectedResult.title}\n\n" +
                    "Source: ${selectedResult.source}\n\n" +
                    "URL: ${selectedResult.url}\n\n" +
                    "Description: ${selectedResult.snippet}"
                } else {
                    "Search results for: $searchQuery"
                }
                generateSummaryAndCreateCard(content)
            }
            uploadedPdfFiles.isNotEmpty() -> {
                // PDF tab is active
                extractPdfContent(android.app.Application(), uploadedPdfFiles.first())
            }
            noteContent.isNotEmpty() -> {
                // Note tab is active
                createNoteCard()
            }
            uploadedAudioFiles.isNotEmpty() -> {
                // Audio tab is active
                createAudioCard()
            }
            else -> {
                errorMessage = "No content to create card from"
            }
        }
    }

    private fun generateSummaryAndCreateCard(content: String) {
        viewModelScope.launch {
            try {
                if (content.isNotEmpty()) {
                    // Determine content type based on active tab
                    val contentType = when {
                        urlInput.isNotEmpty() -> CardType.URL
                        searchQuery.isNotEmpty() -> CardType.SEARCH
                        uploadedPdfFiles.isNotEmpty() -> CardType.PDF
                        noteContent.isNotEmpty() -> CardType.NOTE
                        uploadedAudioFiles.isNotEmpty() -> CardType.AUDIO
                        else -> null
                    }

                    // Check if summary is enabled for the current tab
                    val isSummaryEnabled = when (contentType) {
                        CardType.URL -> urlSummaryEnabled
                        CardType.SEARCH -> searchSummaryEnabled
                        CardType.PDF -> pdfSummaryEnabled
                        CardType.NOTE -> noteSummaryEnabled
                        CardType.AUDIO -> audioSummaryEnabled
                        else -> true
                    }

                    if (isSummaryEnabled) {
                        // Generate summary using AI
                        aiService.summarize(
                            content = content,
                            summaryType = selectedSummaryType,
                            language = selectedLanguage,
                            aiModel = selectedAiModel,
                            contentType = contentType
                        ).onSuccess { summary ->
                            // Check if summary is empty
                            if (summary.isBlank()) {
                                android.util.Log.e("CreateCardViewModel", "Summary is empty, not creating card")
                                errorMessage = "Failed to generate summary. Please try again or use a different AI model."
                                isLoading = false
                                return@onSuccess
                            }

                            // Generate tags for the content
                            val generatedTags = mutableListOf<String>()
                            aiService.extractTags(
                                content = content,
                                language = selectedLanguage,
                                aiModel = selectedAiModel,
                                maxTags = 15 // Generate between 10-20 tags
                            ).onSuccess { tags ->
                                generatedTags.addAll(tags)
                                android.util.Log.d("CreateCardViewModel", "Generated ${tags.size} tags")
                            }.onFailure { error ->
                                android.util.Log.e("CreateCardViewModel", "Error generating tags", error)
                            }

                            // Create card with summary and tags
                            val card = when {
                                urlInput.isNotEmpty() -> createUrlCard(summary, generatedTags)
                                searchQuery.isNotEmpty() -> createSearchCard(summary, generatedTags)
                                uploadedPdfFiles.isNotEmpty() -> createPdfCard(summary, generatedTags)
                                noteContent.isNotEmpty() -> createNoteCard(summary, generatedTags)
                                uploadedAudioFiles.isNotEmpty() -> createAudioCard(summary, generatedTags)
                                else -> null
                            }

                            card?.let {
                                android.util.Log.d("CreateCardViewModel", "Saving card with id: ${it.id}, title: ${it.title}, summary length: ${it.summary.length}")
                                val result = cardRepository.saveCard(it)
                                result.onSuccess { cardId ->
                                    // Navigate to summary review screen with the card ID
                                    android.util.Log.d("CreateCardViewModel", "Card saved successfully with id: $cardId, navigating to summary review")
                                    navigateToSummaryReview = cardId
                                }.onFailure { error ->
                                    android.util.Log.e("CreateCardViewModel", "Error saving card", error)
                                    errorMessage = "Error saving card: ${error.message}"
                                }
                            }
                        }.onFailure { error ->
                            // Handle error
                            android.util.Log.e("CreateCardViewModel", "Error generating summary", error)

                            // Provide more user-friendly error messages
                            errorMessage = when (error) {
                                is com.secondbrain.util.ApiPaymentRequiredException -> {
                                    "OpenRouter requires more credits: ${error.message?.substringAfter("Payment required: ")}"
                                }
                                is com.secondbrain.util.ApiRateLimitException -> {
                                    "Rate limit exceeded. Please try again later."
                                }
                                is com.secondbrain.util.ApiAuthenticationException -> {
                                    "Authentication error. Please check your API key in settings."
                                }
                                else -> "Error generating summary: ${error.message}"
                            }
                        }
                    } else {
                        // Skip AI summarization and create card with empty summary
                        android.util.Log.d("CreateCardViewModel", "Summary disabled, creating card without summary")

                        // Generate tags for the content
                        val generatedTags = mutableListOf<String>()
                        aiService.extractTags(
                            content = content,
                            language = selectedLanguage,
                            aiModel = selectedAiModel,
                            maxTags = 15 // Generate between 10-20 tags
                        ).onSuccess { tags ->
                            generatedTags.addAll(tags)
                            android.util.Log.d("CreateCardViewModel", "Generated ${tags.size} tags")
                        }.onFailure { error ->
                            android.util.Log.e("CreateCardViewModel", "Error generating tags", error)
                        }

                        // Create card with empty summary and tags
                        val card = when {
                            urlInput.isNotEmpty() -> createUrlCard("", generatedTags)
                            searchQuery.isNotEmpty() -> createSearchCard("", generatedTags)
                            uploadedPdfFiles.isNotEmpty() -> createPdfCard("", generatedTags)
                            noteContent.isNotEmpty() -> createNoteCard("", generatedTags)
                            uploadedAudioFiles.isNotEmpty() -> createAudioCard("", generatedTags)
                            else -> null
                        }

                        card?.let {
                            android.util.Log.d("CreateCardViewModel", "Saving card with id: ${it.id}, title: ${it.title}, no summary")
                            val result = cardRepository.saveCard(it)
                            result.onSuccess { cardId ->
                                // Navigate to summary review screen with the card ID
                                android.util.Log.d("CreateCardViewModel", "Card saved successfully with id: $cardId, navigating to summary review")
                                navigateToSummaryReview = cardId
                            }.onFailure { error ->
                                android.util.Log.e("CreateCardViewModel", "Error saving card", error)
                                errorMessage = "Error saving card: ${error.message}"
                            }
                        }
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

    private fun createUrlCard(summary: String, tags: List<String> = emptyList()): Card {
        val urlContent = extractedUrlContent
        return Card(
            id = UUID.randomUUID().toString(),
            title = urlContent?.title ?: urlInput,
            summary = summary,
            content = urlContent?.content ?: "",
            source = urlInput,
            type = CardType.URL,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            thumbnailUrl = urlContent?.thumbnailUrl
        )
    }

    private fun createSearchCard(summary: String, tags: List<String> = emptyList()): Card {
        val result = selectedSearchResult
        return Card(
            id = UUID.randomUUID().toString(),
            title = result?.title ?: "Search: $searchQuery",
            summary = summary,
            content = result?.snippet ?: "Search results for: $searchQuery",
            source = result?.url ?: "Search query: $searchQuery",
            type = CardType.SEARCH,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            thumbnailUrl = result?.thumbnailUrl
        )
    }

    private fun createPdfCard(summary: String, tags: List<String> = emptyList()): Card {
        val pdfContent = extractedPdfContent
        return Card(
            id = UUID.randomUUID().toString(),
            title = pdfContent?.title ?: "PDF Document",
            summary = summary,
            content = pdfContent?.content ?: "",
            source = "PDF: ${pdfContent?.title}",
            type = CardType.PDF,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            pageCount = pdfContent?.pageCount
        )
    }

    private fun createNoteCard(summary: String, tags: List<String> = emptyList()): Card {
        return Card(
            id = UUID.randomUUID().toString(),
            title = if (noteTitle.isNotBlank()) noteTitle else "Note: ${noteContent.take(30)}...",
            summary = summary,
            content = noteContent,
            source = "Note",
            type = CardType.NOTE,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType
        )
    }

    private fun createAudioCard(summary: String, tags: List<String> = emptyList()): Card {
        return Card(
            id = UUID.randomUUID().toString(),
            title = if (audioTitle.isNotBlank()) audioTitle else "Audio Note",
            summary = summary,
            content = "Audio transcription placeholder",
            source = "Audio recording",
            type = CardType.AUDIO,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType
        )
    }

    fun selectSearchResult(result: WebSearchResult) {
        selectedSearchResult = result
    }

    /**
     * Perform a web search using the selected sources
     */
    fun performSearch() {
        if (searchQuery.isBlank() || selectedSearchSources.isEmpty()) {
            return
        }

        viewModelScope.launch {
            try {
                isSearching = true
                searchError = null
                searchResults.clear()
                selectedSearchResult = null

                android.util.Log.d("CreateCardViewModel", "Performing search for: $searchQuery in sources: $selectedSearchSources")

                webSearchService.search(searchQuery, selectedSearchSources.toList())
                    .onSuccess { results ->
                        android.util.Log.d("CreateCardViewModel", "Search returned ${results.size} results")
                        searchResults.addAll(results)

                        if (results.isEmpty()) {
                            searchError = "No results found. Try a different search term or select different sources."
                        }
                    }
                    .onFailure { error ->
                        searchError = "Error searching: ${error.message}"
                        android.util.Log.e("CreateCardViewModel", "Error searching", error)
                    }
            } catch (e: Exception) {
                searchError = "Error searching: ${e.message}"
                android.util.Log.e("CreateCardViewModel", "Error searching", e)
            } finally {
                isSearching = false
            }
        }
    }

    fun clearNavigationState() {
        navigateToSummaryReview = null
    }

    fun clearErrorMessage() {
        errorMessage = null
    }
}
