# Second Brain UI Documentation

## UI Architecture and Design System

### Overview

Second Brain follows a modern, clean UI design based on Material Design 3 principles. The UI is built entirely with Jetpack Compose, allowing for a declarative, reactive approach to UI development. The app supports both light and dark themes with dynamic color adaptation.

### Design System

#### Color Palette

The app uses a consistent color palette derived from Material 3 design:

```kotlin
@Composable
fun SecondBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

The primary colors are:
- **Primary**: Used for primary UI elements, floating action buttons, and selection controls
- **Secondary**: Used for secondary UI elements, less prominent buttons
- **Tertiary**: Used for contrasting accents and highlights
- **Background**: Main background color for the app
- **Surface**: Background color for components like cards
- **Error**: Used for error states and messages

#### Typography

The app uses a consistent typography system with defined text styles:

```kotlin
val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Additional text styles...
)
```

The typography system includes styles for:
- Headings (Large, Medium, Small)
- Body text (Large, Medium, Small)
- Labels and captions
- Button text
- Markdown content

#### Shapes

The app uses a consistent shape system for UI components:

```kotlin
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)
```

These shapes are applied to:
- Cards
- Buttons
- Text fields
- Dialog boxes
- Bottom sheets

#### Spacing

The app uses a consistent spacing system based on 4dp increments:

```kotlin
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

This spacing system is used for:
- Padding
- Margins
- Gaps between elements
- Content insets

### Responsive Design

The app implements responsive design to adapt to different screen sizes and orientations:

```kotlin
@Composable
fun ResponsiveLayout(
    content: @Composable (WindowSizeClass) -> Unit
) {
    val configuration = LocalConfiguration.current
    val windowSizeClass = when {
        configuration.screenWidthDp < 600 -> WindowSizeClass.COMPACT
        configuration.screenWidthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }

    content(windowSizeClass)
}
```

The app adjusts its layout based on three window size classes:
- **Compact**: Phones in portrait mode
- **Medium**: Phones in landscape mode and small tablets
- **Expanded**: Tablets and foldables in unfolded state

### Accessibility

The app implements accessibility features including:

- **Content Descriptions**: All UI elements have proper content descriptions for screen readers
- **Sufficient Contrast**: Text and interactive elements have sufficient contrast ratios
- **Touch Target Sizes**: Interactive elements have touch targets of at least 48x48dp
- **Text Scaling**: UI adapts to system text scaling settings
- **Screen Reader Support**: Compatible with TalkBack and other screen readers

## Navigation Structure

### Bottom Navigation

The app uses a bottom navigation bar for primary navigation between main sections:

```kotlin
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavTab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                BottomNavTab.HOME -> HomeScreen()
                BottomNavTab.SEARCH -> SearchScreen()
                BottomNavTab.KNOWLEDGE -> KnowledgeGraphScreen()
                BottomNavTab.SETTINGS -> SettingsScreen()
            }
        }
    }
}

enum class BottomNavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    KNOWLEDGE("Knowledge", Icons.Default.Lightbulb),
    SETTINGS("Settings", Icons.Default.Settings)
}
```

### Navigation Flow

The app uses the Navigation Compose library for screen navigation:

```kotlin
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("search") { SearchScreen(navController) }
        composable("knowledge") { KnowledgeGraphScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable(
            "card/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")
            CardDetailScreen(cardId = cardId, navController = navController)
        }
        // Additional routes...
    }
}
```

### Navigation Patterns

The app implements several navigation patterns:

1. **Tab-based Navigation**: Primary sections accessible via bottom navigation
2. **Hierarchical Navigation**: Drill-down navigation from lists to details
3. **Modal Navigation**: Full-screen modal dialogs for creation flows
4. **Back Navigation**: Consistent back button behavior throughout the app
5. **Deep Linking**: Support for deep links to specific cards or searches

## Main Screens

### Home Screen

The Home Screen is the primary entry point to the app, displaying the user's cards in either list or grid view.

#### Layout Structure

```
┌─────────────────────────────────┐
│ App Bar                         │
├─────────────────────────────────┤
│ ┌─────┐ ┌─────────────────────┐ │
│ │     │ │ View Toggle         │ │
│ │ Logo│ │ (List/Grid)         │ │
│ │     │ └─────────────────────┘ │
│ └─────┘                         │
├─────────────────────────────────┤
│                                 │
│ Card List/Grid                  │
│ ┌─────────────────────────────┐ │
│ │ Card                        │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Card                        │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Card                        │ │
│ └─────────────────────────────┘ │
│                                 │
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ Create FAB                  │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

#### View Modes

The Home Screen supports two view modes:

1. **List View**: Full-width cards with larger thumbnails and more details
2. **Grid View**: Compact cards in a multi-column grid

Users can toggle between these views using a button in the app bar.

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val viewType by viewModel.viewType.collectAsState()

    Scaffold(
        topBar = {
            HomeTopAppBar(
                title = "Second Brain",
                viewType = viewType,
                onViewTypeChange = { viewModel.setViewType(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Open create card screen */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
    ) { paddingValues ->
        when (viewType) {
            ViewType.LIST -> CardListView(
                cards = uiState.cards,
                onCardClick = { /* Navigate to card detail */ },
                onCardLongClick = { /* Show card options */ },
                modifier = Modifier.padding(paddingValues)
            )
            ViewType.GRID -> CardGridView(
                cards = uiState.cards,
                onCardClick = { /* Navigate to card detail */ },
                onCardLongClick = { /* Show card options */ },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
```

### Search Screen

The Search Screen allows users to search across all their cards and external sources.

#### Layout Structure

```
┌─────────────────────────────────┐
│ ┌─────────────────────────────┐ │
│ │ Search Bar                  │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ Filter Chips                │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│                                 │
│ Search Results                  │
│ ┌─────────────────────────────┐ │
│ │ Result Card                 │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Result Card                 │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Result Card                 │ │
│ └─────────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

#### Search Functionality

The Search Screen provides:

1. **Full-text Search**: Searches across titles, content, summaries, and tags
2. **Filters**: Allows filtering by card type, date, and other criteria
3. **External Search**: Can search web sources (DuckDuckGo) and save results
4. **Real-time Results**: Updates results as the user types
5. **Highlighting**: Highlights matching terms in search results

```kotlin
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column {
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = { viewModel.performSearch() },
            placeholder = { Text("Search cards, content, tags...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Filter chips
        LazyRow {
            items(uiState.filters) { filter ->
                FilterChip(
                    selected = filter.isSelected,
                    onClick = { viewModel.toggleFilter(filter.id) },
                    label = { Text(filter.name) }
                )
            }
        }

        // Search results
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.results.isEmpty() -> EmptySearchResults()
            else -> SearchResultsList(results = uiState.results)
        }
    }
}
```

### Knowledge Graph Screen

The Knowledge Graph Screen visualizes connections between cards and entities.

#### Layout Structure

```
┌─────────────────────────────────┐
│ ┌─────────────────────────────┐ │
│ │ Controls Bar                │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│                                 │
│                                 │
│                                 │
│                                 │
│ Graph Visualization             │
│                                 │
│                                 │
│                                 │
│                                 │
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ Selected Node Details       │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

#### Graph Features

The Knowledge Graph Screen provides:

1. **Interactive Graph**: Zoomable and pannable graph visualization
2. **Node Selection**: Tap on nodes to view details
3. **Filtering**: Filter by node type or relationship
4. **Expansion**: Expand nodes to see more connections
5. **Navigation**: Navigate to related cards

```kotlin
@Composable
fun KnowledgeGraphScreen(viewModel: KnowledgeGraphViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        // Controls bar
        GraphControlsBar(
            onFilterChange = { viewModel.updateFilters(it) },
            onLayoutChange = { viewModel.updateLayout(it) }
        )

        // Graph visualization
        KnowledgeGraphVisualization(
            graph = uiState.graph,
            onNodeClick = { viewModel.selectNode(it) },
            onEdgeClick = { viewModel.selectEdge(it) },
            modifier = Modifier.weight(1f)
        )

        // Selected node details
        uiState.selectedNode?.let { node ->
            NodeDetailPanel(
                node = node,
                onNavigateToCard = { /* Navigate to card */ },
                onExpandNode = { viewModel.expandNode(it) }
            )
        }
    }
}
```

### Settings Screen

The Settings Screen allows users to configure app preferences and manage their data.

#### Layout Structure

```
┌─────────────────────────────────┐
│ ┌─────────────────────────────┐ │
│ │ App Bar                     │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│                                 │
│ Settings Categories             │
│ ┌─────────────────────────────┐ │
│ │ AI Integration              │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Storage                     │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Appearance                  │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ About                       │ │
│ └─────────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

#### Settings Categories

The Settings Screen includes:

1. **AI Integration**: Configure AI providers and API keys
2. **Storage**: Manage storage location and sync settings
3. **Appearance**: Customize theme and display options
4. **About**: App information and open source licenses

```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                SettingsCategory(
                    title = "AI Integration",
                    icon = Icons.Default.SmartToy,
                    onClick = { /* Navigate to AI settings */ }
                )
            }
            item {
                SettingsCategory(
                    title = "Storage",
                    icon = Icons.Default.Storage,
                    onClick = { /* Navigate to storage settings */ }
                )
            }
            item {
                SettingsCategory(
                    title = "Appearance",
                    icon = Icons.Default.Palette,
                    onClick = { /* Navigate to appearance settings */ }
                )
            }
            item {
                SettingsCategory(
                    title = "About",
                    icon = Icons.Default.Info,
                    onClick = { /* Navigate to about screen */ }
                )
            }
        }
    }
}
```

## Card Components

The card components are the fundamental building blocks of the UI, displaying content in a consistent and visually appealing way.

### Card Types

The app supports several card types, each with specialized display components:

1. **URL Cards**: Display web content with thumbnails and metadata
2. **YouTube Cards**: Display YouTube videos with thumbnails, duration, and channel info
3. **PDF Cards**: Display PDF documents with thumbnails and page count
4. **Note Cards**: Display user-created notes with optional images
5. **Audio Cards**: Display audio recordings with waveform visualization

### Card Layout

All cards follow a consistent layout pattern:

```
┌─────────────────────────────────┐
│                                 │
│          Thumbnail              │
│            with                 │
│         Play Button             │
│        (if video)               │
│                                 │
├─────────────────────────────────┤
│ Title                           │
│ Source/Author                   │
│ Metadata (views, duration)      │
│ Summary                         │
│ #tags #tags #tags               │
└─────────────────────────────────┘
```

### Thumbnail Display

The thumbnail display is a key component of the card UI, following these specifications:

1. **Full-width Thumbnails**: All cards display full-width thumbnails when available
2. **Play Button Overlay**: Video content shows a play button overlay in the center
3. **Duration Display**: Videos show duration in the bottom-right corner
4. **Consistent Aspect Ratio**: Thumbnails maintain a consistent aspect ratio (16:9)
5. **Rounded Corners**: Thumbnails have rounded corners matching the card shape

```kotlin
@Composable
fun CardThumbnail(
    thumbnailUrl: String?,
    isVideo: Boolean,
    videoDuration: String?,
    modifier: Modifier = Modifier
) {
    if (thumbnailUrl != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Thumbnail image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            // Play button overlay for video content
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Duration overlay if available
                videoDuration?.let { duration ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF000000).copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
```

### Card List Item

The CardListItem component displays cards in the list view:

```kotlin
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CardListItem(
    card: Card,
    isPinned: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    // Check if this is a YouTube card
    val isYouTubeCard = card.videoId != null && card.thumbnailUrl != null

    if (isYouTubeCard) {
        // Use YouTube-specific card view
        YouTubeCardView(
            card = card,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        // Use standard card view
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column {
                // Thumbnail with play button overlay if available
                if (!card.thumbnailUrl.isNullOrEmpty()) {
                    CardThumbnail(
                        thumbnailUrl = card.thumbnailUrl,
                        isVideo = card.type == CardType.URL && (
                            card.source.contains("youtube") ||
                            card.source.contains("vimeo") ||
                            card.metadata?.contains("video") == true
                        ),
                        videoDuration = card.videoDuration
                    )
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = card.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tags
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        card.tags.take(3).forEach { tag ->
                            SuggestionChip(
                                onClick = { /* Tag click action */ },
                                label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.padding(end = 4.dp, bottom = 0.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### Card Grid Item

The CardGridItem component displays cards in the grid view:

```kotlin
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CardGridItem(
    card: Card,
    isPinned: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    // Check if this is a YouTube card
    val isYouTubeCard = card.videoId != null && card.thumbnailUrl != null

    if (isYouTubeCard) {
        // Use YouTube-specific card view
        YouTubeCardGridItem(
            card = card,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        // Use standard card view
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column {
                // Thumbnail with play button overlay if available
                if (!card.thumbnailUrl.isNullOrEmpty()) {
                    CardThumbnail(
                        thumbnailUrl = card.thumbnailUrl,
                        isVideo = card.type == CardType.URL && (
                            card.source.contains("youtube") ||
                            card.source.contains("vimeo") ||
                            card.metadata?.contains("video") == true
                        ),
                        videoDuration = card.videoDuration,
                        modifier = Modifier.height(130.dp)
                    )
                }

                // Content
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = card.summary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
```

### YouTube Card View

The YouTubeCardView component is a specialized card for YouTube content:

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YouTubeCardView(
    card: Card,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column {
            // Thumbnail with duration overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Thumbnail
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(card.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Video thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )

                // Play button overlay
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Duration overlay
                card.videoDuration?.let { duration ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF000000).copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Channel name
                card.channelTitle?.let { channel ->
                    Text(
                        text = channel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // View count and other metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    card.viewCount?.let { views ->
                        Text(
                            text = "$views views",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Transcript badge
                    if (card.hasTranscript) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = "Transcript",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Summary
                Text(
                    text = card.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
```

## Card Creation Screens

The card creation screens allow users to create new cards from various content types.

### Create Card Activity

The CreateCardActivity serves as the entry point for card creation:

```kotlin
class CreateCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecondBrainTheme {
                CreateCardScreen(
                    onClose = { finish() },
                    onCardCreated = { cardId ->
                        setResult(Activity.RESULT_OK, Intent().apply {
                            putExtra("cardId", cardId)
                        })
                        finish()
                    }
                )
            }
        }
    }
}
```

### Create Card Screen

The CreateCardScreen provides a tabbed interface for different content types:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    viewModel: CreateCardViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onCardCreated: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(CreateCardTab.URL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Card") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tab row
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                CreateCardTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = null) }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                CreateCardTab.URL -> UrlTabContent(
                    uiState = uiState,
                    onUrlChange = viewModel::updateUrl,
                    onFetchContent = viewModel::fetchUrlContent,
                    onGenerateSummary = viewModel::generateSummary,
                    onSaveCard = { viewModel.saveCard(onCardCreated) }
                )
                CreateCardTab.SEARCH -> SearchTabContent(
                    uiState = uiState,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::performSearch,
                    onSelectResult = viewModel::selectSearchResult,
                    onGenerateSummary = viewModel::generateSummary,
                    onSaveCard = { viewModel.saveCard(onCardCreated) }
                )
                CreateCardTab.PDF -> PdfTabContent(
                    uiState = uiState,
                    onPdfSelected = viewModel::processPdf,
                    onGenerateSummary = viewModel::generateSummary,
                    onSaveCard = { viewModel.saveCard(onCardCreated) }
                )
                CreateCardTab.NOTE -> NoteTabContent(
                    uiState = uiState,
                    onTitleChange = viewModel::updateTitle,
                    onContentChange = viewModel::updateContent,
                    onGenerateSummary = viewModel::generateSummary,
                    onSaveCard = { viewModel.saveCard(onCardCreated) }
                )
                CreateCardTab.AUDIO -> AudioTabContent(
                    uiState = uiState,
                    onStartRecording = viewModel::startRecording,
                    onStopRecording = viewModel::stopRecording,
                    onAudioSelected = viewModel::processAudio,
                    onGenerateSummary = viewModel::generateSummary,
                    onSaveCard = { viewModel.saveCard(onCardCreated) }
                )
            }
        }
    }
}

enum class CreateCardTab(val title: String, val icon: ImageVector) {
    URL("URL", Icons.Default.Link),
    SEARCH("Search", Icons.Default.Search),
    PDF("PDF", Icons.Default.PictureAsPdf),
    NOTE("Note", Icons.Default.Edit),
    AUDIO("Audio", Icons.Default.Mic)
}
```

### URL Tab Content

The URL tab allows users to create cards from web content:

```kotlin
@Composable
fun UrlTabContent(
    uiState: CreateCardUiState,
    onUrlChange: (String) -> Unit,
    onFetchContent: () -> Unit,
    onGenerateSummary: () -> Unit,
    onSaveCard: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // URL input
        OutlinedTextField(
            value = uiState.url,
            onValueChange = onUrlChange,
            label = { Text("URL") },
            placeholder = { Text("https://example.com") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onFetchContent) {
                    Icon(Icons.Default.Search, contentDescription = "Fetch")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onFetchContent() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Generate AI summary toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.generateSummary,
                onCheckedChange = { if (it) onGenerateSummary() }
            )
            Text("Generate AI summary")
        }

        // Selected AI model display
        if (uiState.generateSummary) {
            Text(
                text = "Using: ${uiState.selectedAiModel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content preview
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (uiState.content.isNotEmpty()) {
            Column {
                // Title
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Thumbnail if available
                if (uiState.thumbnailUrl != null) {
                    AsyncImage(
                        model = uiState.thumbnailUrl,
                        contentDescription = "Content thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Summary
                if (uiState.summary.isNotEmpty()) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(uiState.summary)

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tags
                if (uiState.tags.isNotEmpty()) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow {
                        uiState.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { /* Edit tag */ },
                                label = { Text("#$tag") },
                                modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = onSaveCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Card")
                }
            }
        }
    }
}
```

## AI Settings Screens

The AI settings screens allow users to configure AI providers and models.

### AI Settings Screen

The main AI settings screen provides access to different AI providers:

```kotlin
@Composable
fun AiSettingsScreen(
    viewModel: AiSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToProvider: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Integration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                Text(
                    text = "Configure AI Providers",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(uiState.providers) { provider ->
                AiProviderItem(
                    provider = provider,
                    isConfigured = provider.isConfigured,
                    isSelected = provider.isSelected,
                    onClick = { onNavigateToProvider(provider.id) },
                    onSelect = { viewModel.selectProvider(provider.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "System Prompts",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(uiState.systemPrompts) { prompt ->
                SystemPromptItem(
                    prompt = prompt,
                    onClick = { /* Navigate to edit prompt */ }
                )
            }
        }
    }
}
```

### AI Provider Settings Screen

The AI provider settings screen allows configuring a specific provider:

```kotlin
@Composable
fun AiProviderSettingsScreen(
    providerId: String,
    viewModel: AiProviderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModelSelector by remember { mutableStateOf(false) }

    // Load provider data
    LaunchedEffect(providerId) {
        viewModel.loadProvider(providerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.provider?.name ?: "AI Provider") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // API Key section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "API Key",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.apiKey,
                        onValueChange = { viewModel.updateApiKey(it) },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        TextButton(
                            onClick = { /* Open API key documentation */ }
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("How to get an API key")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { viewModel.saveApiKey() }
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }

            // Model selection section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Model Selection",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Selected model: ${uiState.selectedModel?.name ?: "None"}",
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { showModelSelector = true }
                        ) {
                            Text("Select Model")
                        }
                    }
                }
            }

            // Model selector dialog
            if (showModelSelector) {
                ModelSelectorDialog(
                    models = uiState.models,
                    selectedModelId = uiState.selectedModel?.id,
                    onModelSelected = {
                        viewModel.selectModel(it)
                        showModelSelector = false
                    },
                    onDismiss = { showModelSelector = false },
                    onSearch = viewModel::searchModels
                )
            }
        }
    }
}
```

### Model Selector Dialog

The model selector dialog allows users to choose an AI model:

```kotlin
@Composable
fun ModelSelectorDialog(
    models: List<AiModel>,
    selectedModelId: String?,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Model") },
        text = {
            Column {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearch(it)
                    },
                    label = { Text("Search models") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Models list
                LazyColumn {
                    items(models) { model ->
                        ModelItem(
                            model = model,
                            isSelected = model.id == selectedModelId,
                            onClick = { onModelSelected(model.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

### Model Item

The ModelItem component displays a model with its capabilities:

```kotlin
@Composable
fun ModelItem(
    model: AiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (model.description != null) {
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Capabilities
            Row(modifier = Modifier.padding(top = 4.dp)) {
                model.capabilities.forEach { capability ->
                    CapabilityChip(capability = capability)
                }
            }
        }
    }
}
```

### Capability Chip

The CapabilityChip component displays a model's capability:

```kotlin
@Composable
fun CapabilityChip(capability: ModelCapability) {
    val (icon, label, color) = when (capability) {
        ModelCapability.TEXT -> Triple(
            Icons.Default.TextFields,
            "Text",
            MaterialTheme.colorScheme.primary
        )
        ModelCapability.IMAGE -> Triple(
            Icons.Default.Image,
            "Image",
            MaterialTheme.colorScheme.secondary
        )
        ModelCapability.DOCUMENT -> Triple(
            Icons.Default.Description,
            "Documents",
            MaterialTheme.colorScheme.tertiary
        )
        ModelCapability.AUDIO -> Triple(
            Icons.Default.AudioFile,
            "Audio",
            MaterialTheme.colorScheme.error
        )
        ModelCapability.WEB_LINK -> Triple(
            Icons.Default.Link,
            "Web Links",
            MaterialTheme.colorScheme.surfaceVariant
        )
    }

    SuggestionChip(
        onClick = { /* No action */ },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color,
            iconContentColor = color
        ),
        modifier = Modifier.padding(end = 4.dp)
    )
}
```

## Conclusion

### UI Design Principles

The Second Brain app follows several key UI design principles:

1. **Consistency**: All UI components follow a consistent design language, with standardized spacing, typography, and color usage.

2. **Hierarchy**: Visual hierarchy guides users through the interface, with clear distinctions between primary and secondary elements.

3. **Feedback**: The UI provides clear feedback for user actions, with loading indicators, transitions, and error messages.

4. **Accessibility**: The app is designed to be accessible to all users, with proper contrast, touch targets, and screen reader support.

5. **Simplicity**: The UI is kept simple and focused, avoiding unnecessary complexity and visual clutter.

### Thumbnail Display Implementation

The thumbnail display implementation is a key feature of the app, providing a consistent and visually appealing way to display content previews. The implementation includes:

1. **Full-width Thumbnails**: All cards display full-width thumbnails when available, providing a visually rich experience.

2. **Play Button Overlay**: Video content shows a play button overlay in the center, clearly indicating that the content is playable.

3. **Duration Display**: Videos show duration in the bottom-right corner, providing important metadata at a glance.

4. **Consistent Styling**: All thumbnails follow consistent styling with rounded corners, proper aspect ratio, and smooth loading transitions.

5. **Fallback Handling**: The UI gracefully handles missing thumbnails without disrupting the overall layout.

This implementation ensures that all content types are displayed in a visually consistent and appealing way, enhancing the user experience and making the app more engaging.
