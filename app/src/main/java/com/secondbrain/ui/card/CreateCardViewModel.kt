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
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.data.service.AiService
import com.secondbrain.data.service.ThumbnailService
import com.secondbrain.data.service.WebSearchService
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.util.ContentExtractor
import com.secondbrain.util.PdfContent
import com.secondbrain.util.PdfProcessor
import com.secondbrain.util.UrlContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val webSearchService: WebSearchService,
    private val settingsRepository: SettingsRepository,
    private val thumbnailService: ThumbnailService
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

    // Get a display-friendly name for the selected AI model
    val selectedModelDisplayName: String
        get() {
            // Extract provider name if it's in the format "Provider - Model"
            if (selectedAiModel.contains(" - ")) {
                return selectedAiModel
            } else {
                // Try to determine provider from the model name
                return when {
                    selectedAiModel.contains("gpt", ignoreCase = true) -> "OpenAI - $selectedAiModel"
                    selectedAiModel.contains("claude", ignoreCase = true) -> "Claude - $selectedAiModel"
                    selectedAiModel.contains("gemini", ignoreCase = true) -> "Gemini - $selectedAiModel"
                    selectedAiModel.contains("deepseek", ignoreCase = true) -> "DeepSeek - $selectedAiModel"
                    else -> "OpenRouter - $selectedAiModel"
                }
            }
        }

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

    // Text selection state for rich text editing
    var noteSelectionStart by mutableStateOf(0)
    var noteSelectionEnd by mutableStateOf(0)

    /**
     * Apply bold formatting to selected text or insert at cursor position
     */
    fun applyBoldFormatting() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert placeholder
            val newText = noteContent.substring(0, noteSelectionStart) + "**bold text**" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Position cursor inside the bold tags
            noteSelectionStart += 2
            noteSelectionEnd = noteSelectionStart + 8 // "bold text".length
        } else {
            // Apply to selection
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            val newText = noteContent.substring(0, noteSelectionStart) + "**$selectedText**" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Adjust selection to include the formatting marks
            noteSelectionEnd += 4 // Add ** at start and end
        }
    }

    /**
     * Apply italic formatting to selected text or insert at cursor position
     */
    fun applyItalicFormatting() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert placeholder
            val newText = noteContent.substring(0, noteSelectionStart) + "*italic text*" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Position cursor inside the italic tags
            noteSelectionStart += 1
            noteSelectionEnd = noteSelectionStart + 11 // "italic text".length
        } else {
            // Apply to selection
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            val newText = noteContent.substring(0, noteSelectionStart) + "*$selectedText*" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Adjust selection to include the formatting marks
            noteSelectionEnd += 2 // Add * at start and end
        }
    }

    /**
     * Apply underline formatting to selected text or insert at cursor position
     */
    fun applyUnderlineFormatting() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert placeholder
            val newText = noteContent.substring(0, noteSelectionStart) + "__underlined text__" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Position cursor inside the underline tags
            noteSelectionStart += 2
            noteSelectionEnd = noteSelectionStart + 15 // "underlined text".length
        } else {
            // Apply to selection
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            val newText = noteContent.substring(0, noteSelectionStart) + "__" + selectedText + "__" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Adjust selection to include the formatting marks
            noteSelectionEnd += 4 // Add __ at start and end
        }
    }

    /**
     * Insert a bullet point at the current line or convert selected lines to bullet points
     */
    fun applyBulletPoint() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert a bullet point at the current position
            // First check if we're at the start of a line
            val isAtLineStart = noteSelectionStart == 0 ||
                               noteContent.substring(0, noteSelectionStart).endsWith("\n")

            val newText = if (isAtLineStart) {
                noteContent.substring(0, noteSelectionStart) + "- " +
                noteContent.substring(noteSelectionEnd)
            } else {
                noteContent.substring(0, noteSelectionStart) + "\n- " +
                noteContent.substring(noteSelectionEnd)
            }

            noteContent = newText
            // Position cursor after the bullet point
            noteSelectionStart += if (isAtLineStart) 2 else 3
            noteSelectionEnd = noteSelectionStart
        } else {
            // Apply to all selected lines
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            val lines = selectedText.split("\n")
            val bulletedLines = lines.joinToString("\n") { "- $it" }

            val newText = noteContent.substring(0, noteSelectionStart) + bulletedLines +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Adjust selection to include the bullet points
            noteSelectionEnd = noteSelectionStart + bulletedLines.length
        }
    }

    /**
     * Insert a numbered list item or convert selected lines to a numbered list
     */
    fun applyNumberedList() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert a numbered list item at the current position
            // First check if we're at the start of a line
            val isAtLineStart = noteSelectionStart == 0 ||
                               noteContent.substring(0, noteSelectionStart).endsWith("\n")

            val newText = if (isAtLineStart) {
                noteContent.substring(0, noteSelectionStart) + "1. " +
                noteContent.substring(noteSelectionEnd)
            } else {
                noteContent.substring(0, noteSelectionStart) + "\n1. " +
                noteContent.substring(noteSelectionEnd)
            }

            noteContent = newText
            // Position cursor after the list marker
            noteSelectionStart += if (isAtLineStart) 3 else 4
            noteSelectionEnd = noteSelectionStart
        } else {
            // Apply to all selected lines
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            val lines = selectedText.split("\n")
            val numberedLines = lines.mapIndexed { index, line -> "${index + 1}. $line" }.joinToString("\n")

            val newText = noteContent.substring(0, noteSelectionStart) + numberedLines +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Adjust selection to include the numbered list
            noteSelectionEnd = noteSelectionStart + numberedLines.length
        }
    }

    /**
     * Insert a checkbox or convert selected lines to checkboxes
     */
    fun applyCheckbox() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert a checkbox at the current position
            // First check if we're at the start of a line
            val isAtLineStart = noteSelectionStart == 0 ||
                               noteContent.substring(0, noteSelectionStart).endsWith("\n")

            val newText = if (isAtLineStart) {
                noteContent.substring(0, noteSelectionStart) + "- [ ] " +
                noteContent.substring(noteSelectionEnd)
            } else {
                noteContent.substring(0, noteSelectionStart) + "\n- [ ] " +
                noteContent.substring(noteSelectionEnd)
            }

            noteContent = newText
            // Position cursor after the checkbox
            noteSelectionStart += if (isAtLineStart) 6 else 7
            noteSelectionEnd = noteSelectionStart
        } else {
            // Apply to all selected lines
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            val lines = selectedText.split("\n")
            val checkboxLines = lines.joinToString("\n") { "- [ ] $it" }

            val newText = noteContent.substring(0, noteSelectionStart) + checkboxLines +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Adjust selection to include the checkboxes
            noteSelectionEnd = noteSelectionStart + checkboxLines.length
        }
    }

    /**
     * Apply code formatting to selected text or insert at cursor position
     */
    fun applyCodeFormatting() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert placeholder
            val newText = noteContent.substring(0, noteSelectionStart) + "`code`" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Position cursor inside the code tags
            noteSelectionStart += 1
            noteSelectionEnd = noteSelectionStart + 4 // "code".length
        } else {
            // Apply to selection
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            // Check if it's a single line or multiple lines
            val isMultiline = selectedText.contains("\n")

            val newText = if (isMultiline) {
                // Use code block for multiple lines
                noteContent.substring(0, noteSelectionStart) + "```\n$selectedText\n```" +
                noteContent.substring(noteSelectionEnd)
            } else {
                // Use inline code for single line
                noteContent.substring(0, noteSelectionStart) + "`$selectedText`" +
                noteContent.substring(noteSelectionEnd)
            }

            noteContent = newText
            // Adjust selection to include the code formatting
            noteSelectionEnd = if (isMultiline) {
                noteSelectionStart + selectedText.length + 8 // Add ```\n at start and \n``` at end
            } else {
                noteSelectionStart + selectedText.length + 2 // Add ` at start and end
            }
        }
    }

    /**
     * Insert a link at the cursor position or convert selected text to a link
     */
    fun applyLinkFormatting() {
        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert a link template
            val newText = noteContent.substring(0, noteSelectionStart) + "[link text](https://example.com)" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Position cursor at the link text
            noteSelectionStart += 1
            noteSelectionEnd = noteSelectionStart + 9 // "link text".length
        } else {
            // Convert selected text to link text
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            val newText = noteContent.substring(0, noteSelectionStart) + "[$selectedText](https://example.com)" +
                          noteContent.substring(noteSelectionEnd)
            noteContent = newText
            // Position cursor at the URL
            noteSelectionStart = noteSelectionEnd + 3 // After ](
            noteSelectionEnd = noteSelectionStart + 18 // "https://example.com".length
        }
    }

    /**
     * Insert an image reference at the cursor position
     */
    fun applyImageFormatting() {
        val imageTemplate = "![image description](https://example.com/image.jpg)"
        val newText = noteContent.substring(0, noteSelectionStart) + imageTemplate +
                      noteContent.substring(noteSelectionEnd)
        noteContent = newText
        // Position cursor at the image description
        noteSelectionStart += 2
        noteSelectionEnd = noteSelectionStart + 17 // "image description".length
    }

    /**
     * Insert a heading at the current line or convert selected text to a heading
     */
    fun applyHeadingFormatting(level: Int = 2) { // Default to H2
        val prefix = "#".repeat(level) + " "

        if (noteSelectionStart == noteSelectionEnd) {
            // No selection, insert a heading at the current position
            // First check if we're at the start of a line
            val isAtLineStart = noteSelectionStart == 0 ||
                               noteContent.substring(0, noteSelectionStart).endsWith("\n")

            val newText = if (isAtLineStart) {
                noteContent.substring(0, noteSelectionStart) + prefix + "Heading" +
                noteContent.substring(noteSelectionEnd)
            } else {
                noteContent.substring(0, noteSelectionStart) + "\n" + prefix + "Heading" +
                noteContent.substring(noteSelectionEnd)
            }

            noteContent = newText
            // Position cursor after the heading prefix
            noteSelectionStart += if (isAtLineStart) prefix.length else prefix.length + 1
            noteSelectionEnd = noteSelectionStart + 7 // "Heading".length
        } else {
            // Apply to selected text
            val selectedText = noteContent.substring(noteSelectionStart, noteSelectionEnd)
            // Check if the selection spans multiple lines
            val lines = selectedText.split("\n")

            if (lines.size > 1) {
                // Apply heading to first line only
                val firstLine = lines.first()
                val restOfLines = lines.drop(1).joinToString("\n")

                val newText = noteContent.substring(0, noteSelectionStart) +
                              prefix + firstLine + "\n" + restOfLines +
                              noteContent.substring(noteSelectionEnd)
                noteContent = newText
                // Adjust selection
                noteSelectionEnd = noteSelectionStart + prefix.length + selectedText.length
            } else {
                // Apply to single line
                val newText = noteContent.substring(0, noteSelectionStart) +
                              prefix + selectedText +
                              noteContent.substring(noteSelectionEnd)
                noteContent = newText
                // Adjust selection
                noteSelectionEnd = noteSelectionStart + prefix.length + selectedText.length
            }
        }
    }

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
        loadSelectedModelFromSettings()
    }

    /**
     * Navigate to AI model selection screen
     * This will be implemented by the Activity/Fragment
     */
    var onNavigateToModelSelection: () -> Unit = {}

    private fun loadAiModels() {
        viewModelScope.launch {
            isLoadingModels = true
            try {
                // Get available models from the AI service manager
                val providers = aiServiceManager.getAvailableProviders()
                val allModels = mutableListOf<String>()

                // Collect models from all providers
                for (provider in providers) {
                    if (provider.isConfigured()) {
                        // Format model names with provider name
                        val providerModels = provider.availableModels.map { model ->
                            // Make sure we're not duplicating the provider name in the model name
                            val modelName = if (model.name.contains(provider.name, ignoreCase = true)) {
                                model.name
                            } else {
                                model.name
                            }
                            "${provider.name} - ${modelName}"
                        }
                        allModels.addAll(providerModels)
                    }
                }

                // If no models are available, add some defaults
                if (allModels.isEmpty()) {
                    allModels.addAll(listOf(
                        "OpenAI - gpt-3.5-turbo",
                        "OpenAI - gpt-4",
                        "Claude - claude-3-opus",
                        "Claude - claude-3-sonnet",
                        "OpenRouter - mistral-7b"
                    ))
                }

                aiModels.clear()
                aiModels.addAll(allModels)

                // Log the available models
                android.util.Log.d("CreateCardViewModel", "Available models: ${aiModels.joinToString(", ")}")
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error loading AI models", e)
                // Add some default models if loading fails
                aiModels.clear()
                aiModels.addAll(listOf(
                    "OpenAI - gpt-3.5-turbo",
                    "OpenAI - gpt-4"
                ))
            } finally {
                isLoadingModels = false
            }
        }
    }

    /**
     * Load the selected model from settings
     */
    private fun loadSelectedModelFromSettings() {
        viewModelScope.launch {
            try {
                // Get the default model from settings
                val defaultModel = settingsRepository.defaultAiModelFlow.first()
                if (defaultModel.isNotBlank()) {
                    selectedAiModel = defaultModel
                    android.util.Log.d("CreateCardViewModel", "Loaded selected model from settings: $defaultModel")
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error loading selected model from settings", e)
            }
        }
    }

    fun extractUrlContent(context: Context) {
        if (urlInput.isBlank()) {
            errorMessage = "Please enter a URL"
            return
        }

        // Save operation for retry
        lastOperation = { extractUrlContent(context) }

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
        // Save operation for retry
        lastOperation = { extractPdfContent(context, uri) }

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

        // Save operation for retry
        lastOperation = { createNoteCard() }

        isLoading = true
        errorMessage = null
        generateSummaryAndCreateCard(noteContent)
    }

    fun createAudioCard() {
        // Placeholder for audio card creation
        errorMessage = "Audio card creation not implemented yet"
    }

    fun createCardFromActiveTab() {
        // Save operation for retry
        lastOperation = { createCardFromActiveTab() }

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
        val title = urlContent?.title ?: urlInput

        // Get thumbnail URL from content or generate one
        var thumbnailUrl = urlContent?.thumbnailUrl

        // Generate a card ID that will be used for both initial creation and updates
        val cardId = UUID.randomUUID().toString()

        // If no thumbnail URL or it's invalid, try to get one using the ThumbnailService
        if (thumbnailUrl.isNullOrEmpty() || !thumbnailUrl.startsWith("http")) {
            runBlocking {
                try {
                    // Get a thumbnail for the URL
                    val newThumbnailUrl = thumbnailService.getThumbnailForUrl(
                        url = urlInput,
                        type = CardType.URL,
                        title = title
                    )

                    if (!newThumbnailUrl.isNullOrEmpty()) {
                        android.util.Log.d("CreateCardViewModel", "Generated new thumbnail URL: $newThumbnailUrl for card: $cardId")
                        thumbnailUrl = newThumbnailUrl
                    } else {
                        android.util.Log.d("CreateCardViewModel", "Failed to generate thumbnail for card: $cardId")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CreateCardViewModel", "Error getting thumbnail for URL: $urlInput", e)
                }
            }
        }

        // Log the thumbnail URL for debugging
        android.util.Log.d("CreateCardViewModel", "Creating URL card with ID: $cardId and thumbnail: $thumbnailUrl")

        return Card(
            id = cardId,  // Use the same ID we generated earlier
            title = title,
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
            thumbnailUrl = thumbnailUrl
        )
    }

    private fun createSearchCard(summary: String, tags: List<String> = emptyList()): Card {
        val result = selectedSearchResult
        val title = result?.title ?: "Search: $searchQuery"
        val source = result?.url ?: "Search query: $searchQuery"

        // Get thumbnail URL from result or generate one
        var thumbnailUrl = result?.thumbnailUrl

        // Generate a card ID that will be used for both initial creation and updates
        val cardId = UUID.randomUUID().toString()

        // If no thumbnail URL or it's invalid, try to get one using the ThumbnailService
        if (thumbnailUrl.isNullOrEmpty() || !thumbnailUrl.startsWith("http")) {
            runBlocking {
                try {
                    // Get a thumbnail for the URL
                    val newThumbnailUrl = thumbnailService.getThumbnailForUrl(
                        url = source,
                        type = CardType.SEARCH,
                        title = title
                    )

                    if (!newThumbnailUrl.isNullOrEmpty()) {
                        android.util.Log.d("CreateCardViewModel", "Generated new thumbnail URL: $newThumbnailUrl for search card: $cardId")
                        thumbnailUrl = newThumbnailUrl
                    } else {
                        android.util.Log.d("CreateCardViewModel", "Failed to generate thumbnail for search card: $cardId")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CreateCardViewModel", "Error getting thumbnail for search result: $source", e)
                }
            }
        }

        // Log the thumbnail URL for debugging
        android.util.Log.d("CreateCardViewModel", "Creating search card with ID: $cardId and thumbnail: $thumbnailUrl")

        return Card(
            id = cardId,  // Use the same ID we generated earlier
            title = title,
            summary = summary,
            content = result?.snippet ?: "Search results for: $searchQuery",
            source = source,
            type = CardType.SEARCH,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            thumbnailUrl = thumbnailUrl
        )
    }

    private fun createPdfCard(summary: String, tags: List<String> = emptyList()): Card {
        val pdfContent = extractedPdfContent
        val title = pdfContent?.title ?: "PDF Document"
        val source = "PDF: ${pdfContent?.title}"

        // Get thumbnail URL from PDF content or generate one
        var thumbnailUrl = pdfContent?.thumbnailUrl

        // Generate a card ID that will be used for both initial creation and updates
        val cardId = UUID.randomUUID().toString()

        // If no thumbnail URL or it's invalid, try to get one using the ThumbnailService
        if (thumbnailUrl.isNullOrEmpty()) {
            runBlocking {
                try {
                    // Get a thumbnail for the PDF
                    val newThumbnailUrl = thumbnailService.getThumbnailForUrl(
                        url = pdfContent?.uri ?: "",
                        type = CardType.PDF,
                        title = title
                    )

                    if (!newThumbnailUrl.isNullOrEmpty()) {
                        android.util.Log.d("CreateCardViewModel", "Generated new thumbnail URL: $newThumbnailUrl for PDF card: $cardId")
                        thumbnailUrl = newThumbnailUrl
                    } else {
                        android.util.Log.d("CreateCardViewModel", "Failed to generate thumbnail for PDF card: $cardId")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CreateCardViewModel", "Error getting thumbnail for PDF: ${pdfContent?.uri}", e)
                }
            }
        }

        // Log the thumbnail URL for debugging
        android.util.Log.d("CreateCardViewModel", "Creating PDF card with ID: $cardId and thumbnail: $thumbnailUrl")

        return Card(
            id = cardId,  // Use the same ID we generated earlier
            title = title,
            summary = summary,
            content = pdfContent?.content ?: "",
            source = source,
            type = CardType.PDF,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            thumbnailUrl = thumbnailUrl,
            pageCount = pdfContent?.pageCount
        )
    }

    private fun createNoteCard(summary: String, tags: List<String> = emptyList()): Card {
        val title = if (noteTitle.isNotBlank()) noteTitle else "Note: ${noteContent.take(30)}..."

        // Generate a card ID that will be used for both initial creation and updates
        val cardId = UUID.randomUUID().toString()

        // Generate a text-based thumbnail for the note
        var thumbnailUrl: String? = null

        // Generate the thumbnail synchronously before returning the card
        runBlocking {
            try {
                // Generate a text-based thumbnail
                val generatedThumbnail = thumbnailService.getThumbnailForUrl(
                    url = "note://$cardId",  // Use the card ID in the URL to ensure uniqueness
                    type = CardType.NOTE,
                    title = title
                )

                if (!generatedThumbnail.isNullOrEmpty()) {
                    android.util.Log.d("CreateCardViewModel", "Generated new thumbnail URL: $generatedThumbnail for note card: $cardId")
                    thumbnailUrl = generatedThumbnail
                } else {
                    android.util.Log.d("CreateCardViewModel", "Failed to generate thumbnail for note card: $cardId")
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error generating thumbnail for note", e)
            }
        }

        // Log the thumbnail URL for debugging
        android.util.Log.d("CreateCardViewModel", "Creating note card with ID: $cardId and thumbnail: $thumbnailUrl")

        return Card(
            id = cardId,  // Use the same ID we generated earlier
            title = title,
            summary = summary,
            content = noteContent,
            source = "Note",
            type = CardType.NOTE,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            thumbnailUrl = thumbnailUrl
        )
    }

    private fun createAudioCard(summary: String, tags: List<String> = emptyList()): Card {
        val title = if (audioTitle.isNotBlank()) audioTitle else "Audio Note"

        // Generate a text-based thumbnail for the audio note
        var thumbnailUrl: String? = null

        // Generate a card ID that will be used for both initial creation and updates
        val cardId = UUID.randomUUID().toString()

        runBlocking {
            try {
                // Generate a text-based thumbnail
                val generatedThumbnail = thumbnailService.getThumbnailForUrl(
                    url = "audio://$cardId",  // Use the card ID in the URL to ensure uniqueness
                    type = CardType.AUDIO,
                    title = title
                )

                if (!generatedThumbnail.isNullOrEmpty()) {
                    android.util.Log.d("CreateCardViewModel", "Generated new thumbnail URL: $generatedThumbnail for audio card: $cardId")
                    thumbnailUrl = generatedThumbnail
                } else {
                    android.util.Log.d("CreateCardViewModel", "Failed to generate thumbnail for audio card: $cardId")
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCardViewModel", "Error generating thumbnail for audio note", e)
            }
        }

        // Log the thumbnail URL for debugging
        android.util.Log.d("CreateCardViewModel", "Creating audio card with ID: $cardId and thumbnail: $thumbnailUrl")

        return Card(
            id = cardId,  // Use the same ID we generated earlier
            title = title,
            summary = summary,
            content = "Audio transcription placeholder",
            source = "Audio recording",
            type = CardType.AUDIO,
            tags = tags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            language = selectedLanguage,
            aiModel = selectedAiModel,
            summaryType = selectedSummaryType,
            thumbnailUrl = thumbnailUrl
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

    // Last operation state for retry functionality
    private var lastOperation: (() -> Unit)? = null

    fun retryLastOperation() {
        lastOperation?.invoke()
    }
}
