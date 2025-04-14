# Second Brain - Developer Context

## Project Overview

Second Brain is an Android application designed to serve as a personal knowledge management (PKM) system. It allows users to capture, organize, and retrieve information from various sources using AI-powered summarization and knowledge graph visualization. The app is being built as a free, open-source alternative to commercial PKM tools, with a focus on user privacy, data ownership, and flexibility in AI model selection.

The project was initiated to address several limitations in existing PKM solutions:

1. **Lack of Android-native solutions**: Most powerful PKM tools are web-based or iOS-first
2. **Limited AI integration flexibility**: Commercial tools often lock users into specific AI providers
3. **Privacy concerns**: Many tools store user data on their servers with limited transparency
4. **Cost barriers**: Premium features in commercial tools are often behind subscription paywalls

Second Brain aims to solve these issues by providing a fully-featured, open-source PKM system that runs natively on Android, allows users to choose their AI providers, gives complete data ownership through local and user-controlled cloud storage, and remains free to use.

## Technical Stack

### Core Technologies
- **Language**: Kotlin (v1.8.0+)
  - Using Kotlin's latest features including coroutines, flow, and serialization
  - Extensive use of extension functions for clean, readable code
  - Sealed classes for type-safe state management

- **UI Framework**: Jetpack Compose (v1.4.0+)
  - Declarative UI approach with recomposition optimization
  - Custom theming with Material 3 design system
  - State hoisting pattern for clean separation of concerns
  - Custom composables for specialized UI components

- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles
  - Unidirectional data flow (UI State → Events → ViewModel → Repository → Data Sources)
  - UI State objects for representing screen state
  - Use Cases for business logic encapsulation
  - Repository pattern for data access abstraction

- **Database**: Room (v2.5.0+) for local storage
  - Complex relational schema with foreign key constraints
  - Custom type converters for complex objects
  - Full-text search capabilities
  - Migration strategies for schema evolution

- **Dependency Injection**: Hilt
  - Module-based injection for better testability
  - Scoped components (Application, Activity, ViewModel)
  - Qualifiers for disambiguating similar dependencies

- **Concurrency**: Kotlin Coroutines and Flow
  - Structured concurrency with coroutine scopes
  - Cold flows for reactive data streams
  - StateFlow and SharedFlow for UI state management
  - Flow operators for complex data transformations

- **Background Processing**: WorkManager
  - Chained work requests for complex processing pipelines
  - Periodic work for sync operations
  - Constraints for network-dependent operations
  - Progress tracking with WorkInfo updates

- **Image Loading**: Coil
  - Memory and disk caching strategies
  - Custom image transformations
  - Placeholder and error handling
  - Crossfade animations for smooth loading

- **Network**: Retrofit with OkHttp
  - Custom interceptors for authentication and logging
  - Connection pooling for performance
  - Timeout configurations for different API endpoints
  - Retry mechanisms for transient failures

- **JSON Parsing**: Moshi
  - Custom adapters for complex types
  - Polymorphic adapters for handling inheritance
  - Lazy initialization for performance

- **Markdown Rendering**: Custom implementation with Compose
  - Markdown parser with support for GitHub Flavored Markdown
  - Custom rendering for code blocks, tables, and math expressions
  - Interactive elements (clickable links, expandable sections)
  - Syntax highlighting for code blocks

### External APIs
- **AI Providers**:
  - **OpenAI**: GPT-3.5 and GPT-4 models for text generation and summarization
  - **Google (Gemini)**: Gemini Pro and Ultra models for multimodal content processing
  - **Anthropic (Claude)**: Claude 2 and Opus models for long-context summarization
  - **DeepSeek**: DeepSeek-Coder and other specialized models
  - **OpenRouter**: Aggregation service providing access to multiple models
  - Each provider requires specific request formatting and response handling

- **Cloud Storage**:
  - **Google Drive API**: REST API v3 with OAuth 2.0 authentication
  - **Microsoft OneDrive API**: Microsoft Graph API with OAuth 2.0 (planned)
  - Custom synchronization logic for conflict resolution

- **Content Extraction**:
  - **YouTube Data API**: v3 API for metadata, thumbnails, and captions
  - **Custom Web Scraping**: JSoup-based extraction with fallback mechanisms
  - **PDF Processing**: Apache PDFBox for text extraction and metadata
  - **Audio Processing**: FFmpeg for transcoding and metadata extraction

## Project Structure

The project follows a modular architecture with clear separation of concerns, adhering to Clean Architecture principles. This structure facilitates testability, maintainability, and scalability.

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/secondbrain/
│   │   │   ├── data/                      # Data Layer
│   │   │   │   ├── api/                   # API clients for external services
│   │   │   │   │   ├── ai/                # AI provider API clients
│   │   │   │   │   │   ├── openai/        # OpenAI-specific implementation
│   │   │   │   │   │   ├── gemini/        # Gemini-specific implementation
│   │   │   │   │   │   ├── claude/        # Claude-specific implementation
│   │   │   │   │   │   ├── deepseek/      # DeepSeek-specific implementation
│   │   │   │   │   │   └── openrouter/    # OpenRouter-specific implementation
│   │   │   │   │   ├── storage/           # Cloud storage API clients
│   │   │   │   │   │   ├── googledrive/   # Google Drive implementation
│   │   │   │   │   │   └── onedrive/      # OneDrive implementation (planned)
│   │   │   │   │   └── content/           # Content extraction APIs
│   │   │   │   │       ├── youtube/       # YouTube API client
│   │   │   │   │       ├── web/           # Web scraping utilities
│   │   │   │   │       ├── pdf/           # PDF processing utilities
│   │   │   │   │       └── audio/         # Audio processing utilities
│   │   │   │   ├── db/                    # Local database
│   │   │   │   │   ├── dao/               # Data Access Objects
│   │   │   │   │   ├── entity/            # Database entities
│   │   │   │   │   ├── converter/         # Type converters
│   │   │   │   │   ├── relation/          # Entity relationships
│   │   │   │   │   └── AppDatabase.kt     # Database configuration
│   │   │   │   ├── model/                 # Data models
│   │   │   │   │   ├── ai/                # AI-related models
│   │   │   │   │   ├── card/              # Card-related models
│   │   │   │   │   ├── knowledge/         # Knowledge graph models
│   │   │   │   │   └── user/              # User preference models
│   │   │   │   ├── repository/            # Repositories
│   │   │   │   │   ├── ai/                # AI repository implementations
│   │   │   │   │   ├── card/              # Card repository implementation
│   │   │   │   │   ├── knowledge/         # Knowledge graph repository
│   │   │   │   │   └── storage/           # Storage repository
│   │   │   │   └── source/                # Data sources
│   │   │   │       ├── local/             # Local data sources
│   │   │   │       └── remote/            # Remote data sources
│   │   │   ├── di/                        # Dependency Injection
│   │   │   │   ├── module/                # Hilt modules
│   │   │   │   │   ├── ApiModule.kt       # API-related dependencies
│   │   │   │   │   ├── DatabaseModule.kt  # Database-related dependencies
│   │   │   │   │   ├── RepositoryModule.kt # Repository bindings
│   │   │   │   │   └── UseCaseModule.kt   # Use case bindings
│   │   │   │   └── qualifier/             # Custom qualifiers
│   │   │   ├── domain/                    # Domain Layer
│   │   │   │   ├── usecase/               # Business logic use cases
│   │   │   │   │   ├── ai/                # AI-related use cases
│   │   │   │   │   ├── card/              # Card-related use cases
│   │   │   │   │   ├── knowledge/         # Knowledge graph use cases
│   │   │   │   │   └── storage/           # Storage-related use cases
│   │   │   │   ├── repository/            # Repository interfaces
│   │   │   │   └── model/                 # Domain models
│   │   │   ├── ui/                        # Presentation Layer
│   │   │   │   ├── card/                  # Card creation and editing
│   │   │   │   │   ├── create/            # Card creation screens
│   │   │   │   │   ├── detail/            # Card detail screen
│   │   │   │   │   ├── edit/              # Card editing screen
│   │   │   │   │   └── component/         # Card-specific components
│   │   │   │   ├── components/            # Reusable UI components
│   │   │   │   │   ├── button/            # Custom buttons
│   │   │   │   │   ├── card/              # Card display components
│   │   │   │   │   ├── input/             # Input components
│   │   │   │   │   ├── loading/           # Loading indicators
│   │   │   │   │   └── markdown/          # Markdown rendering components
│   │   │   │   ├── home/                  # Home screen
│   │   │   │   │   ├── list/              # List view implementation
│   │   │   │   │   ├── grid/              # Grid view implementation
│   │   │   │   │   └── filter/            # Filtering components
│   │   │   │   ├── knowledge/             # Knowledge graph visualization
│   │   │   │   │   ├── graph/             # Graph rendering
│   │   │   │   │   ├── node/              # Node components
│   │   │   │   │   └── detail/            # Node detail display
│   │   │   │   ├── search/                # Search functionality
│   │   │   │   │   ├── result/            # Search results display
│   │   │   │   │   └── filter/            # Search filters
│   │   │   │   ├── settings/              # App settings
│   │   │   │   │   ├── ai/                # AI settings screens
│   │   │   │   │   ├── storage/           # Storage settings screens
│   │   │   │   │   ├── appearance/        # Appearance settings
│   │   │   │   │   └── about/             # About screen
│   │   │   │   └── theme/                 # Theming components
│   │   │   ├── util/                      # Utility classes
│   │   │   │   ├── extension/             # Kotlin extensions
│   │   │   │   ├── formatter/             # Formatting utilities
│   │   │   │   ├── parser/                # Parsing utilities
│   │   │   │   └── security/              # Security utilities
│   │   │   └── worker/                    # Background workers
│   │   │       ├── ai/                    # AI processing workers
│   │   │       ├── sync/                  # Synchronization workers
│   │   │       └── content/               # Content processing workers
│   │   └── res/                           # Resources
│   │       ├── drawable/                  # Images and icons
│   │       ├── layout/                    # XML layouts (minimal, mostly Compose)
│   │       ├── values/                    # Resource values
│   │       │   ├── colors.xml             # Color definitions
│   │       │   ├── strings.xml            # String resources
│   │       │   ├── themes.xml             # Theme definitions
│   │       │   └── dimens.xml             # Dimension values
│   │       └── font/                      # Custom fonts
│   ├── test/                              # Unit tests
│   │   ├── java/com/secondbrain/
│   │   │   ├── data/                      # Data layer tests
│   │   │   ├── domain/                    # Domain layer tests
│   │   │   └── ui/                        # UI layer tests
│   └── androidTest/                       # Instrumented tests
│       └── java/com/secondbrain/          # UI and integration tests
├── build.gradle                           # App-level build configuration
└── proguard-rules.pro                     # ProGuard rules for release builds
```

This structure follows several key architectural principles:

1. **Separation of Concerns**: Clear boundaries between data, domain, and presentation layers
2. **Dependency Rule**: Inner layers (domain) don't depend on outer layers (data, presentation)
3. **Feature Modularity**: Features are organized into cohesive packages
4. **Testability**: Each component can be tested in isolation
5. **Scalability**: New features can be added without modifying existing code

## Key Components

### 1. Card System

The fundamental unit of information in Second Brain is a "Card". A Card can represent various types of content and serves as the central data model for the application.

#### Card Data Model

```kotlin
data class Card(
    @PrimaryKey
    val id: String,                     // UUID for unique identification
    val title: String,                  // Card title
    val content: String,                // Full content (markdown format)
    val summary: String,                // AI-generated summary
    val type: CardType,                 // URL, SEARCH, PDF, NOTE, AUDIO
    val source: String,                 // Original source (URL, file path, etc.)
    val tags: List<String>,             // User and AI-generated tags
    val createdAt: Long,                // Creation timestamp
    val updatedAt: Long,                // Last update timestamp
    val language: String,               // Content language
    val aiModel: String,                // AI model used for summarization
    val summaryType: String,            // Type of summary (concise, detailed, etc.)
    val thumbnailUrl: String? = null,   // URL or file path to thumbnail image
    val pageCount: Int? = null,         // For PDF documents
    // YouTube-specific fields
    val videoId: String? = null,        // YouTube video ID
    val channelTitle: String? = null,   // Channel name
    val videoDuration: String? = null,  // Duration in format "MM:SS" or "HH:MM:SS"
    val viewCount: String? = null,      // View count as formatted string
    val hasTranscript: Boolean = false, // Whether transcript is available
    // Additional metadata as JSON string
    val metadata: String? = null        // Flexible storage for additional data
)
```

#### Card Type Enumeration

```kotlin
enum class CardType {
    URL,     // Web content, articles, YouTube videos
    SEARCH,  // Search results saved as cards
    PDF,     // PDF documents
    NOTE,    // User-created notes
    AUDIO    // Audio recordings or files
}
```

#### Database Schema

The Card entity is stored in Room with the following schema:

```kotlin
@Entity(tableName = "cards")
@TypeConverters(Converters::class)
data class Card(...) // As above
```

With custom type converters for complex types:

```kotlin
class Converters {
    @TypeConverter
    fun fromTagsList(tags: List<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toTagsList(tagsString: String): List<String> =
        if (tagsString.isBlank()) emptyList()
        else tagsString.split(",")

    // Additional converters for other complex types
}
```

#### Card Repository

The CardRepository provides a clean API for card operations:

```kotlin
interface CardRepository {
    fun getAllCards(): Flow<List<Card>>
    fun getCardById(id: String): Flow<Card?>
    fun getCardsByType(type: CardType): Flow<List<Card>>
    fun searchCards(query: String): Flow<List<Card>>
    suspend fun insertCard(card: Card)
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(id: String)
    suspend fun getCardCount(): Int
}
```

#### Card Display

Cards are displayed in either a list or grid view on the home screen:

1. **List View**: Full-width cards with larger thumbnails and more details
2. **Grid View**: Compact cards in a multi-column grid

Both views implement:
- Full-width thumbnails for visual content
- Play button overlay for video content
- Duration display in the bottom-right corner for videos
- Consistent styling with rounded corners and elevation
- Interactive elements (tap to open, long press for options)

#### Card Creation Flow

The card creation process follows these steps:

1. User selects content type (URL, PDF, Note, etc.)
2. Content is processed based on type:
   - URLs: Web scraping for content, metadata, images
   - YouTube: API calls for metadata, thumbnails, transcripts
   - PDFs: Text extraction, thumbnail generation
   - Notes: Direct user input
   - Audio: Transcription and processing
3. AI summarization is applied (if enabled)
4. Tags are generated automatically and can be edited by user
5. Card is saved to the database
6. Card appears in the home screen

### 2. AI Integration Framework

The app implements a flexible AI integration framework that allows users to configure their own API keys for various AI providers. This framework is designed to be extensible, allowing easy addition of new AI providers and models.

#### Core Interfaces

```kotlin
interface BaseAiProvider {
    val name: String                                      // Provider name (OpenAI, Gemini, etc.)
    val models: List<AiModel>                            // Available models
    val baseUrl: String                                  // API base URL
    val apiKey: String                                   // User's API key
    val defaultModel: String                             // Default model ID

    suspend fun fetchModels(): List<AiModel>            // Get available models from API
    suspend fun generateSummary(                         // Generate summary
        prompt: String,                                  // Input prompt
        model: String,                                   // Model ID
        systemPrompt: String? = null,                    // Optional system prompt
        maxTokens: Int = 1000,                          // Response length limit
        temperature: Float = 0.7f                        // Creativity parameter
    ): Result<String>                                    // Result wrapper with success/error

    suspend fun isValidApiKey(): Boolean                // Validate API key
    fun formatPrompt(content: String, type: String): String  // Format prompt based on content type
    fun supportsSystemPrompt(): Boolean                 // Whether provider supports system prompts
    fun getSystemPromptFormat(): SystemPromptFormat     // How system prompts should be formatted
}
```

```kotlin
data class AiModel(
    val id: String,                                     // Model identifier
    val name: String,                                   // Display name
    val provider: String,                               // Provider name
    val maxTokens: Int,                                 // Maximum context length
    val capabilities: List<ModelCapability>,            // What the model can process
    val description: String? = null,                    // Optional description
    val pricing: String? = null                         // Optional pricing info
)
```

```kotlin
enum class ModelCapability {
    TEXT,                                               // Text processing
    IMAGE,                                              // Image understanding
    DOCUMENT,                                           // Document processing
    AUDIO,                                              // Audio transcription
    WEB_LINK                                            // Web content processing
}
```

```kotlin
enum class SystemPromptFormat {
    SEPARATE_FIELD,                                     // Separate system field (OpenAI)
    CONVERSATION_FORMAT,                                // First message in conversation (Gemini)
    PREPENDED,                                          // Prepended to user prompt (Claude)
    CUSTOM                                              // Custom implementation
}
```

#### Provider Implementations

Each AI provider implements the BaseAiProvider interface with provider-specific logic:

```kotlin
class OpenAiProvider(
    override val apiKey: String,
    private val apiKeyStorage: ApiKeyStorage,
    private val httpClient: OkHttpClient
) : BaseAiProvider {
    override val name = "OpenAI"
    override val baseUrl = "https://api.openai.com/v1"
    override val defaultModel = "gpt-3.5-turbo"
    override var models = emptyList<AiModel>()

    override suspend fun fetchModels(): List<AiModel> {
        // Implementation using Retrofit to fetch models from OpenAI API
        // Handles authentication, parsing, and mapping to AiModel objects
    }

    override suspend fun generateSummary(
        prompt: String,
        model: String,
        systemPrompt: String?,
        maxTokens: Int,
        temperature: Float
    ): Result<String> {
        // Implementation using OpenAI Chat Completions API
        // Formats messages with system prompt if provided
        // Handles errors and returns Result wrapper
    }

    override suspend fun isValidApiKey(): Boolean {
        // Validates API key by making a test request
    }

    override fun supportsSystemPrompt(): Boolean = true

    override fun getSystemPromptFormat(): SystemPromptFormat =
        SystemPromptFormat.SEPARATE_FIELD
}
```

Similar implementations exist for other providers (Gemini, Claude, DeepSeek, OpenRouter), each handling the specific requirements and formats of their respective APIs.

#### AI Settings Management

The AI settings are managed through dedicated ViewModels and repositories:

```kotlin
class AiSettingsViewModel(
    private val aiRepository: AiRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    // State management for AI settings
    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()

    // Provider selection
    fun selectProvider(provider: String) { ... }

    // API key management
    fun saveApiKey(provider: String, key: String) { ... }

    // Model selection
    fun fetchModels(provider: String) { ... }
    fun selectModel(provider: String, modelId: String) { ... }

    // System prompt customization
    fun saveSystemPrompt(type: String, prompt: String) { ... }

    // Search functionality
    fun searchModels(query: String) { ... }
}
```

#### Key Features

The AI Integration Framework supports:

1. **Dynamic Model Fetching**: Models are fetched directly from provider APIs rather than being hardcoded
2. **Model Search**: Users can search for specific models within each provider
3. **Content Type Capability Display**: Models show which content types they support (text, images, documents, etc.)
4. **System Prompt Customization**: Different prompts for different summary types (concise, detailed, bullet points, etc.)
5. **Provider-Specific Formatting**: Each provider handles the specific requirements of its API
6. **Secure API Key Storage**: Keys are stored securely using Android Keystore
7. **Error Handling**: Comprehensive error handling with user-friendly messages

### 3. Content Processing Pipeline

The content processing pipeline handles different types of input through a series of specialized processors and workers. This system is designed to be extensible and maintainable, with clear separation of concerns.

#### Architecture Overview

The content processing pipeline follows these general steps:

1. **Content Detection**: Identify the type of content (URL, PDF, audio, etc.)
2. **Content Extraction**: Extract relevant information based on content type
3. **Content Processing**: Process the extracted information (transcription, chunking, etc.)
4. **AI Summarization**: Generate summary using selected AI provider
5. **Card Creation**: Create a card with all processed information

#### Core Components

```kotlin
interface ContentProcessor {
    suspend fun process(input: ContentInput): Result<ProcessedContent>
}

data class ContentInput(
    val source: String,                 // URL, file path, or raw text
    val type: ContentType,              // URL, PDF, AUDIO, IMAGE, TEXT
    val metadata: Map<String, Any>? = null  // Additional information
)

data class ProcessedContent(
    val title: String,                  // Extracted or generated title
    val content: String,                // Full content (may be markdown)
    val summary: String,                // AI-generated summary
    val thumbnailUrl: String? = null,   // URL or file path to thumbnail
    val tags: List<String>,             // Generated tags
    val metadata: Map<String, Any>,     // Additional extracted metadata
    val type: CardType                  // Type for card creation
)
```

#### Specialized Processors

##### 1. URL Processing

```kotlin
class UrlProcessor(
    private val webScraper: WebScraper,
    private val youtubeExtractor: YoutubeExtractor,
    private val aiService: AiService,
    private val imageDownloader: ImageDownloader
) : ContentProcessor {
    override suspend fun process(input: ContentInput): Result<ProcessedContent> {
        return when {
            isYoutubeUrl(input.source) -> processYoutubeUrl(input.source)
            else -> processGenericUrl(input.source)
        }
    }

    private suspend fun processYoutubeUrl(url: String): Result<ProcessedContent> {
        // 1. Extract video ID
        // 2. Fetch metadata (title, channel, duration, views) using YouTube API
        // 3. Fetch thumbnail
        // 4. Get transcript if available
        // 5. Generate AI summary based on transcript or description
        // 6. Generate tags
        // 7. Return processed content
    }

    private suspend fun processGenericUrl(url: String): Result<ProcessedContent> {
        // 1. Scrape web content (title, text, images)
        // 2. Extract main article content
        // 3. Download primary image as thumbnail
        // 4. Generate AI summary
        // 5. Generate tags
        // 6. Return processed content
    }
}
```

##### 2. PDF Processing

```kotlin
class PdfProcessor(
    private val pdfTextExtractor: PdfTextExtractor,
    private val pdfThumbnailGenerator: PdfThumbnailGenerator,
    private val aiService: AiService
) : ContentProcessor {
    override suspend fun process(input: ContentInput): Result<ProcessedContent> {
        // 1. Extract text from PDF with smart chunking
        // 2. Generate thumbnail from first page
        // 3. Extract metadata (title, author, page count)
        // 4. Generate AI summary
        // 5. Generate tags
        // 6. Return processed content
    }
}
```

##### 3. Audio Processing

```kotlin
class AudioProcessor(
    private val audioTranscriber: AudioTranscriber,
    private val metadataExtractor: AudioMetadataExtractor,
    private val aiService: AiService
) : ContentProcessor {
    override suspend fun process(input: ContentInput): Result<ProcessedContent> {
        // 1. Extract audio metadata (duration, format, etc.)
        // 2. Transcribe audio content
        // 3. Generate thumbnail (waveform or album art if available)
        // 4. Generate AI summary from transcript
        // 5. Generate tags
        // 6. Return processed content
    }
}
```

##### 4. Image Processing

```kotlin
class ImageProcessor(
    private val imageAnalyzer: ImageAnalyzer,
    private val aiService: AiService
) : ContentProcessor {
    override suspend fun process(input: ContentInput): Result<ProcessedContent> {
        // 1. Analyze image content using AI
        // 2. Generate description
        // 3. Create thumbnail (resized version of original)
        // 4. Generate tags
        // 5. Return processed content
    }
}
```

##### 5. Text/Note Processing

```kotlin
class TextProcessor(
    private val aiService: AiService
) : ContentProcessor {
    override suspend fun process(input: ContentInput): Result<ProcessedContent> {
        // 1. Process raw text input
        // 2. Generate title if not provided
        // 3. Generate AI summary if requested
        // 4. Generate tags
        // 5. Return processed content
    }
}
```

#### Background Processing

Content processing is performed in the background using WorkManager:

```kotlin
class ContentProcessingWorker(
    context: Context,
    params: WorkerParameters,
    private val processorFactory: ContentProcessorFactory,
    private val cardRepository: CardRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // 1. Get input data (source, type, etc.)
        // 2. Get appropriate processor from factory
        // 3. Process content
        // 4. Create and save card
        // 5. Return success or failure
    }
}
```

#### Progress Tracking

Progress is tracked and reported to the UI:

```kotlin
class ProcessingProgressTracker {
    private val _progress = MutableStateFlow<ProcessingProgress>(ProcessingProgress.Idle)
    val progress: StateFlow<ProcessingProgress> = _progress.asStateFlow()

    fun updateProgress(stage: ProcessingStage, progress: Float) {
        _progress.value = ProcessingProgress.InProgress(stage, progress)
    }

    fun setComplete(result: ProcessedContent) {
        _progress.value = ProcessingProgress.Complete(result)
    }

    fun setError(error: Throwable) {
        _progress.value = ProcessingProgress.Error(error)
    }
}

sealed class ProcessingProgress {
    object Idle : ProcessingProgress()
    data class InProgress(val stage: ProcessingStage, val progress: Float) : ProcessingProgress()
    data class Complete(val result: ProcessedContent) : ProcessingProgress()
    data class Error(val error: Throwable) : ProcessingProgress()
}

enum class ProcessingStage {
    CONTENT_EXTRACTION,
    TRANSCRIPTION,
    THUMBNAIL_GENERATION,
    AI_SUMMARIZATION,
    TAG_GENERATION,
    CARD_CREATION
}
```

This comprehensive pipeline ensures that all content types are processed efficiently and consistently, with appropriate error handling and progress reporting.

### 4. Knowledge Graph

The knowledge graph visualizes connections between cards and entities, providing users with a visual representation of their knowledge network. This feature helps users discover relationships and patterns in their information.

#### Data Model

```kotlin
data class KnowledgeNode(
    val id: String,                      // Unique identifier
    val label: String,                   // Display name
    val type: NodeType,                  // CARD, ENTITY, TAG, CONCEPT
    val properties: Map<String, String>, // Additional properties
    val importance: Float = 1.0f,        // Node importance (affects visualization)
    val color: String? = null            // Optional custom color
)

data class KnowledgeEdge(
    val id: String,                      // Unique identifier
    val source: String,                  // Source node ID
    val target: String,                  // Target node ID
    val type: EdgeType,                  // CONTAINS, MENTIONS, RELATED, LINKED
    val weight: Float = 1.0f,            // Edge weight (affects visualization)
    val label: String? = null,           // Optional relationship label
    val properties: Map<String, String>? = null // Additional properties
)

enum class NodeType {
    CARD,      // Represents a card in the system
    ENTITY,    // Named entity (person, place, organization)
    TAG,       // User or AI-generated tag
    CONCEPT    // Abstract concept extracted from content
}

enum class EdgeType {
    CONTAINS,  // Card contains entity/concept
    MENTIONS,  // Card mentions entity/concept
    RELATED,   // Entities/concepts are related
    LINKED     // Explicit link between cards
}
```

#### Knowledge Graph Service

```kotlin
interface KnowledgeGraphService {
    suspend fun buildGraphForCard(cardId: String, depth: Int = 1): KnowledgeGraph
    suspend fun buildGraphForEntity(entityId: String, depth: Int = 1): KnowledgeGraph
    suspend fun buildGraphForQuery(query: String, maxNodes: Int = 50): KnowledgeGraph
    suspend fun getRelatedCards(cardId: String, maxResults: Int = 10): List<Card>
    suspend fun getRelatedEntities(entityId: String, maxResults: Int = 10): List<Entity>
    suspend fun extractEntities(text: String): List<Entity>
}

data class KnowledgeGraph(
    val nodes: List<KnowledgeNode>,
    val edges: List<KnowledgeEdge>,
    val metadata: Map<String, Any>? = null
)

data class Entity(
    val id: String,
    val name: String,
    val type: EntityType,
    val confidence: Float,
    val metadata: Map<String, Any>? = null
)

enum class EntityType {
    PERSON,
    LOCATION,
    ORGANIZATION,
    DATE,
    CONCEPT,
    PRODUCT,
    EVENT,
    OTHER
}
```

#### Entity Extraction

Entities are extracted from card content using a combination of techniques:

1. **Named Entity Recognition (NER)**: Identifies people, places, organizations, dates, etc.
2. **Keyword Extraction**: Identifies important keywords and phrases
3. **Topic Modeling**: Identifies abstract topics and concepts
4. **AI-assisted Extraction**: Uses AI models to identify domain-specific entities

```kotlin
class EntityExtractor(
    private val nerProcessor: NerProcessor,
    private val keywordExtractor: KeywordExtractor,
    private val aiService: AiService
) {
    suspend fun extractEntities(text: String): List<Entity> {
        // Combine results from different extraction methods
        val nerEntities = nerProcessor.extractEntities(text)
        val keywords = keywordExtractor.extractKeywords(text)
        val aiEntities = aiService.extractEntities(text)

        // Merge and deduplicate entities
        return mergeEntities(nerEntities, keywords, aiEntities)
    }
}
```

#### Graph Visualization

The knowledge graph is visualized using a custom Compose-based graph rendering engine:

```kotlin
@Composable
fun KnowledgeGraphVisualization(
    graph: KnowledgeGraph,
    onNodeClick: (KnowledgeNode) -> Unit,
    onEdgeClick: (KnowledgeEdge) -> Unit,
    modifier: Modifier = Modifier
) {
    // State for graph layout and interaction
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNode by remember { mutableStateOf<KnowledgeNode?>(null) }

    // Force-directed layout calculation
    val nodePositions = calculateNodePositions(graph)

    // Render graph with interactions
    Box(modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            // Handle zoom and pan gestures
        }
    ) {
        // Draw edges
        graph.edges.forEach { edge ->
            val source = nodePositions[edge.source] ?: return@forEach
            val target = nodePositions[edge.target] ?: return@forEach
            DrawEdge(source, target, edge, scale, offset)
        }

        // Draw nodes
        graph.nodes.forEach { node ->
            val position = nodePositions[node.id] ?: return@forEach
            DrawNode(
                node = node,
                position = position,
                scale = scale,
                offset = offset,
                isSelected = node.id == selectedNode?.id,
                onClick = {
                    selectedNode = node
                    onNodeClick(node)
                }
            )
        }

        // Node detail panel when a node is selected
        selectedNode?.let { node ->
            NodeDetailPanel(node = node)
        }
    }
}
```

#### Interactive Features

The graph is interactive, allowing users to:

1. **Zoom and Pan**: Navigate the graph using pinch-to-zoom and drag gestures
2. **Select Nodes**: Tap on nodes to view details and actions
3. **Navigate**: Tap on card nodes to open the corresponding card
4. **Expand**: Double-tap on nodes to expand their connections
5. **Filter**: Filter the graph by node type, edge type, or other criteria
6. **Search**: Search for specific nodes within the graph
7. **Customize**: Adjust layout, colors, and visualization parameters

#### Knowledge Discovery

The knowledge graph facilitates discovery through several features:

1. **Related Cards**: Suggests cards related to the current card based on shared entities and concepts
2. **Entity Exploration**: Allows users to explore all cards containing a specific entity
3. **Concept Clustering**: Groups cards by common concepts or themes
4. **Connection Paths**: Shows how different cards or entities are connected through intermediate nodes
5. **Importance Highlighting**: Emphasizes important nodes based on centrality and connection density

### 5. Storage System

The storage system is designed to be flexible and user-controlled, with support for both local and cloud storage options. This system ensures data portability and gives users full control over where their information is stored.

#### Storage Architecture

The storage system follows a layered architecture:

```
Application Layer
      ↓
Storage Repository (Interface)
      ↓
┌─────┴─────┐
│           │
Local      Cloud
Storage    Storage
           ┌───┴───┐
           │       │
       Google    OneDrive
       Drive
```

#### Core Interfaces

```kotlin
interface StorageRepository {
    // Card operations
    suspend fun saveCard(card: Card): Result<String>
    suspend fun getCard(id: String): Result<Card?>
    suspend fun getAllCards(): Result<List<Card>>
    suspend fun deleteCard(id: String): Result<Boolean>

    // File operations
    suspend fun saveFile(file: File, metadata: Map<String, Any>? = null): Result<String>
    suspend fun getFile(id: String): Result<File?>
    suspend fun deleteFile(id: String): Result<Boolean>

    // Sync operations
    suspend fun syncAll(): Result<SyncStats>
    suspend fun syncCard(id: String): Result<Boolean>

    // Storage management
    suspend fun getStorageStats(): Result<StorageStats>
    suspend fun cleanupFiles(olderThan: Long? = null, types: List<String>? = null): Result<Int>
}

data class SyncStats(
    val cardsUploaded: Int,
    val cardsDownloaded: Int,
    val filesUploaded: Int,
    val filesDownloaded: Int,
    val conflicts: Int,
    val errors: Int,
    val timestamp: Long
)

data class StorageStats(
    val totalCards: Int,
    val totalFiles: Int,
    val localStorageUsed: Long,  // In bytes
    val cloudStorageUsed: Long,  // In bytes
    val cloudStorageAvailable: Long,  // In bytes
    val lastSyncTime: Long?
)
```

#### 1. Local Storage

Local storage is implemented using a combination of Room database and file system:

```kotlin
class LocalStorageRepository(
    private val cardDao: CardDao,
    private val fileManager: FileManager,
    private val context: Context
) : StorageRepository {
    // Implementation of StorageRepository interface for local storage

    // Uses Room database for card metadata and relationships
    // Uses file system for original content (PDFs, images, etc.)
    // Stores note content as Markdown files
    // Implements efficient querying and indexing
}
```

##### Room Database Schema

The Room database includes several related entities:

```kotlin
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val type: String,
    val source: String,
    val createdAt: Long,
    val updatedAt: Long,
    val language: String,
    val aiModel: String,
    val summaryType: String,
    val thumbnailUrl: String?,
    val contentPath: String,  // Path to content file
    val syncStatus: Int       // 0: Synced, 1: Pending Upload, 2: Pending Download, 3: Conflict
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(
    tableName = "card_tag_cross_ref",
    primaryKeys = ["cardId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardTagCrossRef(
    val cardId: String,
    val tagId: String
)
```

##### File Management

```kotlin
class FileManager(
    private val context: Context,
    private val baseDir: File = context.getExternalFilesDir(null) ?: context.filesDir
) {
    // Directory structure
    private val contentDir = File(baseDir, "content")
    private val thumbnailDir = File(baseDir, "thumbnails")
    private val originalDir = File(baseDir, "originals")

    init {
        // Ensure directories exist
        contentDir.mkdirs()
        thumbnailDir.mkdirs()
        originalDir.mkdirs()
    }

    // File operations
    suspend fun saveContentFile(id: String, content: String): String { ... }
    suspend fun getContentFile(id: String): String? { ... }
    suspend fun saveThumbnail(id: String, bitmap: Bitmap): String { ... }
    suspend fun saveOriginalFile(id: String, file: File): String { ... }
    suspend fun deleteFiles(id: String): Boolean { ... }

    // Storage management
    suspend fun getStorageSize(): Long { ... }
    suspend fun cleanupOriginalFiles(olderThan: Long? = null): Int { ... }
}
```

#### 2. Cloud Storage

Cloud storage is implemented with provider-specific adapters:

```kotlin
interface CloudStorageProvider {
    val name: String
    val isAuthenticated: Boolean

    suspend fun authenticate(): Result<Boolean>
    suspend fun signOut(): Result<Boolean>

    suspend fun uploadFile(localFile: File, remotePath: String): Result<String>
    suspend fun downloadFile(remotePath: String, localFile: File): Result<Boolean>
    suspend fun deleteFile(remotePath: String): Result<Boolean>
    suspend fun listFiles(remotePath: String): Result<List<CloudFile>>

    suspend fun getStorageStats(): Result<CloudStorageStats>
}

data class CloudFile(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val modifiedTime: Long,
    val mimeType: String?
)

data class CloudStorageStats(
    val usedSpace: Long,
    val totalSpace: Long,
    val trashSpace: Long?
)
```

##### Google Drive Implementation

```kotlin
class GoogleDriveProvider(
    private val context: Context,
    private val googleAuthManager: GoogleAuthManager
) : CloudStorageProvider {
    // Implementation of CloudStorageProvider interface for Google Drive

    // Uses Google Drive REST API v3
    // Handles OAuth 2.0 authentication
    // Implements efficient upload/download with proper error handling
    // Manages app-specific folder structure
}
```

##### OneDrive Implementation (Planned)

```kotlin
class OneDriveProvider(
    private val context: Context,
    private val microsoftAuthManager: MicrosoftAuthManager
) : CloudStorageProvider {
    // Implementation of CloudStorageProvider interface for OneDrive

    // Uses Microsoft Graph API
    // Handles OAuth 2.0 authentication
    // Implements efficient upload/download with proper error handling
    // Manages app-specific folder structure
}
```

#### Synchronization

The synchronization system handles bidirectional sync between local and cloud storage:

```kotlin
class SyncManager(
    private val localRepository: LocalStorageRepository,
    private val cloudProvider: CloudStorageProvider,
    private val conflictResolver: ConflictResolver
) {
    suspend fun syncAll(): Result<SyncStats> {
        // 1. Get list of local cards with pending changes
        // 2. Get list of remote cards with changes
        // 3. Identify conflicts
        // 4. Resolve conflicts using conflict resolver
        // 5. Upload local changes
        // 6. Download remote changes
        // 7. Update sync status
        // 8. Return sync statistics
    }

    suspend fun syncCard(id: String): Result<Boolean> {
        // Sync a specific card and its associated files
    }
}

class ConflictResolver(
    private val strategy: ConflictStrategy = ConflictStrategy.LAST_MODIFIED_WINS
) {
    suspend fun resolveConflict(localCard: Card, remoteCard: Card): Resolution {
        // Apply conflict resolution strategy
        return when (strategy) {
            ConflictStrategy.LAST_MODIFIED_WINS -> {
                if (localCard.updatedAt > remoteCard.updatedAt) {
                    Resolution.USE_LOCAL
                } else {
                    Resolution.USE_REMOTE
                }
            }
            ConflictStrategy.LOCAL_WINS -> Resolution.USE_LOCAL
            ConflictStrategy.REMOTE_WINS -> Resolution.USE_REMOTE
            ConflictStrategy.KEEP_BOTH -> Resolution.KEEP_BOTH
        }
    }
}

enum class ConflictStrategy {
    LAST_MODIFIED_WINS,
    LOCAL_WINS,
    REMOTE_WINS,
    KEEP_BOTH
}

enum class Resolution {
    USE_LOCAL,
    USE_REMOTE,
    KEEP_BOTH
}
```

#### Storage Settings

Users can configure storage options through the settings UI:

```kotlin
data class StorageSettings(
    val storageMode: StorageMode = StorageMode.LOCAL_ONLY,
    val cloudProvider: CloudProvider = CloudProvider.GOOGLE_DRIVE,
    val syncScope: SyncScope = SyncScope.ALL,
    val autoSyncEnabled: Boolean = true,
    val autoSyncInterval: Long = 3600000, // 1 hour in milliseconds
    val wifiOnlySync: Boolean = true,
    val keepOriginalFiles: Boolean = true,
    val originalFileRetentionPeriod: Long = 604800000, // 7 days in milliseconds
    val conflictStrategy: ConflictStrategy = ConflictStrategy.LAST_MODIFIED_WINS
)

enum class StorageMode {
    LOCAL_ONLY,
    CLOUD_ONLY,
    HYBRID
}

enum class CloudProvider {
    GOOGLE_DRIVE,
    ONEDRIVE,
    NONE
}

enum class SyncScope {
    ALL,                // Sync everything
    MARKDOWN_ONLY,      // Sync only .md files
    SELECTED_CARDS      // Sync only selected cards
}
```

This comprehensive storage system ensures that users have full control over their data while providing seamless synchronization between devices.

## UI Implementation Details

### Home Screen

The home screen displays cards in either list or grid view, with a toggle to switch between them. Each card shows:

1. **Full-width thumbnail** (if available)
2. **Play button overlay** for video content
3. **Duration display** for videos
4. **Title**
5. **Source/author**
6. **Metadata** (views, creation date)
7. **Summary preview**
8. **Tags**

Cards support:
- Tap to open
- Long press to show options (duplicate, pin, share, delete)

### Card Creation

The card creation flow includes:

1. **Source Selection**:
   - URL input
   - Web search
   - PDF upload
   - Audio recording/upload
   - Text note

2. **Processing Options**:
   - AI summary toggle (on by default)
   - AI model selection
   - Summary type selection

3. **Editing**:
   - Title editing
   - Summary editing
   - Tag management

### AI Settings

The AI settings screen allows users to:

1. **Configure API Keys**:
   - Input fields for each provider
   - Links to obtain API keys
   - Key validation

2. **Select Models**:
   - Provider selection
   - Model search
   - Model capability display
   - Default model selection

3. **Customize System Prompts**:
   - Different prompts for summary types
   - Template variables
   - Preview functionality

## Current Implementation Status

### Completed Features

1. **Core Infrastructure**:
   - Project setup with Kotlin and Jetpack Compose
   - MVVM architecture implementation
   - Room database for local storage
   - Navigation framework
   - Dark mode and theme support

2. **Note Management**:
   - Card model and database schema
   - Card repository
   - Card list/grid views
   - Card detail view
   - Card editing
   - Markdown rendering
   - Card deletion with confirmation

3. **AI Integration**:
   - AI service interface
   - OpenAI integration
   - Gemini integration
   - Claude integration
   - DeepSeek integration
   - OpenRouter integration
   - AI settings screen
   - Secure API key storage
   - Model selection for providers
   - Dynamic model fetching
   - Model search functionality
   - Content type capability display
   - System prompt customization

4. **Content Processing**:
   - Content type detection
   - Specialized prompt generation
   - Entity extraction
   - Content-aware summarization
   - Background processing with WorkManager
   - Progress tracking for AI processing
   - YouTube metadata extraction
   - Thumbnail fetching and caching
   - Automatic tag generation
   - Summary toggle in card creation

5. **UI/UX Implementation**:
   - Card layout design
   - Consistent thumbnail display
   - Play button overlay for videos
   - Duration display for videos
   - Interactive card elements
   - Responsive grid and list views

6. **Knowledge Graph**:
   - Knowledge graph data model
   - Knowledge graph service
   - Graph visualization component
   - Entity relationship visualization
   - Interactive graph with zoom and pan
   - Node selection and details display
   - Navigation between graph and cards

### In-Progress Features

1. **AI Integration Enhancements**:
   - Fixing OpenRouter model listing issues
   - Improving error handling for API rejections
   - Adding unit tests for provider-specific formatters
   - Optimizing model selection UI
   - Implementing consistent system prompt handling

2. **Content Processing Improvements**:
   - Enhancing YouTube content extraction with YouTube Data API
   - Implementing full video transcript-based summarization
   - Improving PDF processing with smart chunking
   - Enhancing image extraction from web content
   - Implementing audio transcription and processing
   - Optimizing automatic tag generation

3. **Storage Integration**:
   - Google Drive integration
   - OneDrive integration
   - Sync configuration options
   - Storage management tools

## Implementation Challenges and Solutions

### 1. Thumbnail Display Consistency

**Challenge**: Ensuring consistent thumbnail display across different card types and content sources.

**Solution**: Implemented a unified thumbnail display system that:
- Uses full-width thumbnails for all content types
- Adds play button overlay for video content
- Displays duration for videos
- Handles different aspect ratios consistently
- Properly caches images for performance

Implementation in `CardListItem` and `CardGridItem` components ensures visual consistency across the app.

### 2. AI Provider Integration

**Challenge**: Supporting multiple AI providers with different APIs, authentication methods, and capabilities.

**Solution**: Created a flexible `BaseAiProvider` interface with provider-specific implementations that:
- Handle API key validation
- Fetch available models dynamically
- Format requests according to provider requirements
- Parse responses consistently
- Handle errors gracefully with user-friendly messages

### 3. Content Extraction

**Challenge**: Extracting useful content from various sources (web pages, YouTube, PDFs).

**Solution**: Implemented specialized extractors for different content types:
- Web page extractor that handles article content, images, and metadata
- YouTube extractor for video information, thumbnails, and transcripts
- PDF extractor with smart chunking for better summarization
- Image analyzer for visual content

### 4. Performance Optimization

**Challenge**: Ensuring smooth performance with potentially large collections of cards and complex operations.

**Solution**:
- Implemented efficient database queries with Room
- Used paging for large collections
- Offloaded heavy processing to background workers
- Implemented proper caching for network requests and images
- Optimized Compose UI rendering with keys and remember

## Code Conventions and Best Practices

1. **Naming Conventions**:
   - Classes: PascalCase
   - Functions/Variables: camelCase
   - Constants: UPPER_SNAKE_CASE
   - Resource IDs: lowercase_with_underscores

2. **Architecture Guidelines**:
   - Follow MVVM architecture
   - Use repositories to abstract data sources
   - Keep ViewModels focused on UI logic
   - Use Use Cases for complex business logic
   - Implement unidirectional data flow

3. **Testing Strategy**:
   - Unit tests for ViewModels, Repositories, and Use Cases
   - UI tests for critical user flows
   - Mock external dependencies for testing

4. **Error Handling**:
   - Use Result class for operation outcomes
   - Provide user-friendly error messages
   - Log detailed errors for debugging
   - Implement retry mechanisms for network operations

## Getting Started for New Developers

1. **Environment Setup**:
   - Android Studio Arctic Fox or newer
   - JDK 11 or newer
   - Gradle 7.0+

2. **Building the Project**:
   - Clone the repository
   - Open in Android Studio
   - Sync Gradle files
   - Build the project

3. **Running the App**:
   - Use an emulator or physical device with Android 8.0+
   - For AI features, you'll need to provide your own API keys in settings

4. **Key Files to Understand**:
   - `Card.kt`: Core data model
   - `BaseAiProvider.kt`: AI integration interface
   - `HomeScreen.kt`: Main UI entry point
   - `CardRepository.kt`: Data access layer
   - `ContentProcessor.kt`: Content extraction and processing

5. **Common Workflows**:
   - Adding a new card type: Extend `CardType` enum, update UI components
   - Adding a new AI provider: Implement `BaseAiProvider` interface
   - Modifying card display: Update `CardListItem` and `CardGridItem`
   - Adding settings: Update `SettingsScreen` and `UserPreferences`

## Future Development Roadmap

1. **Short-term Goals**:
   - Complete cloud storage integration
   - Enhance YouTube content extraction
   - Implement full-text search
   - Fix OpenRouter model listing issues
   - Improve error handling

2. **Medium-term Goals**:
   - Add tag management interface
   - Implement link auto-completion
   - Add unlinked mentions detection
   - Enhance PDF processing
   - Implement audio transcription

3. **Long-term Vision**:
   - Implement spaced repetition features
   - Add advanced AI chat with notes
   - Create cross-platform synchronization
   - Develop plugin system for extensibility
   - Build community around the open-source project

## Troubleshooting Common Issues

1. **AI API Issues**:
   - Check API key validity
   - Verify network connectivity
   - Check for rate limiting or quota issues
   - Ensure proper formatting of requests

2. **UI Rendering Problems**:
   - Check for Compose recomposition issues
   - Verify proper state management
   - Look for memory leaks with large collections

3. **Database Errors**:
   - Check migration paths
   - Verify schema consistency
   - Look for threading issues with database access

4. **Performance Issues**:
   - Profile with Android Studio tools
   - Check for unnecessary network requests
   - Verify proper image caching
   - Look for excessive recompositions in Compose

## Conclusion

Second Brain is an ambitious project that combines modern Android development practices with AI integration to create a powerful personal knowledge management tool. The app's focus on user privacy, data ownership, and flexibility sets it apart from commercial alternatives.

As a developer joining this project, you'll be working with cutting-edge technologies and tackling interesting challenges in content processing, AI integration, and knowledge visualization. The modular architecture and clear separation of concerns make it easy to understand and extend the codebase.

We welcome your contributions and ideas to help make Second Brain the best open-source PKM tool for Android users.
