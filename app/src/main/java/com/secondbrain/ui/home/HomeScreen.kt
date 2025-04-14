package com.secondbrain.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.model.Card
import com.secondbrain.data.model.CardType
import com.secondbrain.ui.card.CreateCardActivity
import com.secondbrain.ui.components.CardContextMenu
import com.secondbrain.ui.components.YouTubeCardView
import com.secondbrain.ui.components.YouTubeCardGridItem
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

enum class ViewType {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCardDetail: (String) -> Unit = {},
    onNavigateToCardEdit: (String) -> Unit = {}
) {
    // Add error handling
    val errorState = remember { mutableStateOf<String?>(null) }

    // Show error dialog if there's an error
    errorState.value?.let { error ->
        AlertDialog(
            onDismissRequest = { errorState.value = null },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { errorState.value = null }) {
                    Text("OK")
                }
            }
        )
    }
    var viewType by remember { mutableStateOf(ViewType.LIST) }
    val cards = remember { mutableStateOf<List<Card>>(emptyList()) }
    val context = LocalContext.current

    // Safely collect cards
    LaunchedEffect(Unit) {
        try {
            viewModel.cards.collect {
                cards.value = it
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Error collecting cards", e)
            errorState.value = "Error loading cards: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Second Brain") },
                actions = {
                    // View toggle
                    IconButton(onClick = { viewType = if (viewType == ViewType.LIST) ViewType.GRID else ViewType.LIST }) {
                        Icon(
                            imageVector = if (viewType == ViewType.LIST) Icons.Default.GridView else Icons.Default.List,
                            contentDescription = "Toggle view"
                        )
                    }

                    // Sort options
                    DropdownMenu(
                        expanded = viewModel.sortMenuExpanded,
                        onDismissRequest = { viewModel.sortMenuExpanded = false }
                    ) {
                        viewModel.sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.selectedSortOption = option
                                    viewModel.sortMenuExpanded = false
                                    viewModel.sortCards()
                                }
                            )
                        }
                    }

                    TextButton(onClick = { viewModel.sortMenuExpanded = true }) {
                        Text("Sort: ${viewModel.selectedSortOption}")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val intent = Intent(context, CreateCardActivity::class.java)
                    context.startActivity(intent)
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create card") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cards.value.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No cards yet",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your first card by tapping the button below",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Content
                when (viewType) {
                    ViewType.LIST -> CardListView(
                        cards = cards.value,
                        viewModel = viewModel,
                        onCardClick = { card -> onNavigateToCardDetail(card.id) },
                        onCardLongClick = { card -> viewModel.selectCard(card) }
                    )
                    ViewType.GRID -> CardGridView(
                        cards = cards.value,
                        viewModel = viewModel,
                        onCardClick = { card -> onNavigateToCardDetail(card.id) },
                        onCardLongClick = { card -> viewModel.selectCard(card) }
                    )
                }
            }

            // No internet indicator
            if (!viewModel.isOnline) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "No internet connection",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun CardListView(
    cards: List<Card>,
    viewModel: HomeViewModel,
    onCardClick: (Card) -> Unit,
    onCardLongClick: (Card) -> Unit
) {
    val context = LocalContext.current
    val selectedCard by viewModel.selectedCard.collectAsState()
    val pinnedCards by viewModel.pinnedCards.collectAsState()

    // Show context menu for selected card
    selectedCard?.let { card ->
        CardContextMenu(
            card = card,
            expanded = true,
            onDismiss = { viewModel.selectCard(null) },
            onDuplicate = {
                viewModel.duplicateCard(it) { newCardId ->
                    Toast.makeText(context, "Card duplicated", Toast.LENGTH_SHORT).show()
                }
            },
            onPin = {
                if (viewModel.isPinned(it.id)) {
                    viewModel.unpinCard(it.id)
                    Toast.makeText(context, "Card unpinned", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.pinCard(it.id)
                    Toast.makeText(context, "Card pinned", Toast.LENGTH_SHORT).show()
                }
            },
            onShare = {
                val intent = viewModel.shareCard(it)
                context.startActivity(Intent.createChooser(intent, "Share Card"))
            },
            onDelete = {
                viewModel.deleteCard(it.id) {
                    Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Pinned cards section
        if (pinnedCards.isNotEmpty()) {
            item {
                Text(
                    text = "Pinned",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            items(
                items = cards.filter { card -> pinnedCards.contains(card.id) },
                key = { it.id }
            ) { card ->
                CardListItem(
                    card = card,
                    isPinned = true,
                    onClick = { onCardClick(card) },
                    onLongClick = { onCardLongClick(card) }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "All Cards",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // All cards (or unpinned cards if there are pinned cards)
        val unpinnedCards = if (pinnedCards.isNotEmpty()) {
            cards.filter { card -> !pinnedCards.contains(card.id) }
        } else {
            cards
        }

        items(
            items = unpinnedCards,
            key = { it.id }
        ) { card ->
            CardListItem(
                card = card,
                isPinned = false,
                onClick = { onCardClick(card) },
                onLongClick = { onCardLongClick(card) }
            )
        }
    }
}

@Composable
fun CardGridView(
    cards: List<Card>,
    viewModel: HomeViewModel,
    onCardClick: (Card) -> Unit,
    onCardLongClick: (Card) -> Unit
) {
    val context = LocalContext.current
    val selectedCard by viewModel.selectedCard.collectAsState()
    val pinnedCards by viewModel.pinnedCards.collectAsState()

    // Show context menu for selected card
    selectedCard?.let { card ->
        CardContextMenu(
            card = card,
            expanded = true,
            onDismiss = { viewModel.selectCard(null) },
            onDuplicate = {
                viewModel.duplicateCard(it) { newCardId ->
                    Toast.makeText(context, "Card duplicated", Toast.LENGTH_SHORT).show()
                }
            },
            onPin = {
                if (viewModel.isPinned(it.id)) {
                    viewModel.unpinCard(it.id)
                    Toast.makeText(context, "Card unpinned", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.pinCard(it.id)
                    Toast.makeText(context, "Card pinned", Toast.LENGTH_SHORT).show()
                }
            },
            onShare = {
                val intent = viewModel.shareCard(it)
                context.startActivity(Intent.createChooser(intent, "Share Card"))
            },
            onDelete = {
                viewModel.deleteCard(it.id) {
                    Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Pinned cards section
        if (pinnedCards.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Pinned",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            items(
                items = cards.filter { card -> pinnedCards.contains(card.id) },
                key = { it.id }
            ) { card ->
                CardGridItem(
                    card = card,
                    isPinned = true,
                    onClick = { onCardClick(card) },
                    onLongClick = { onCardLongClick(card) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "All Cards",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // All cards (or unpinned cards if there are pinned cards)
        val unpinnedCards = if (pinnedCards.isNotEmpty()) {
            cards.filter { card -> !pinnedCards.contains(card.id) }
        } else {
            cards
        }

        items(
            items = unpinnedCards,
            key = { it.id }
        ) { card ->
            CardGridItem(
                card = card,
                isPinned = false,
                onClick = { onCardClick(card) },
                onLongClick = { onCardLongClick(card) }
            )
        }
    }
}

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
                    val context = LocalContext.current
                    // Log the thumbnail URL for debugging
                    android.util.Log.d("CardListItem", "Displaying thumbnail: ${card.thumbnailUrl}")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        // Thumbnail
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(card.thumbnailUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                            onSuccess = {
                                android.util.Log.d("CardListItem", "Thumbnail loaded successfully")
                            },
                            onError = {
                                android.util.Log.e("CardListItem", "Error loading thumbnail: ${card.thumbnailUrl}")
                            }
                        )

                        // Play button overlay for video content
                        if (card.type == CardType.URL && (card.source.contains("youtube") ||
                            card.source.contains("vimeo") || card.metadata?.contains("video") == true)) {
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
                    }
                } else {
                    android.util.Log.d("CardListItem", "No thumbnail for card: ${card.id}, type: ${card.type}")
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
                    val context = LocalContext.current
                    // Log the thumbnail URL for debugging
                    android.util.Log.d("CardGridItem", "Displaying thumbnail: ${card.thumbnailUrl}")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        // Thumbnail
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(card.thumbnailUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                            onSuccess = {
                                android.util.Log.d("CardGridItem", "Thumbnail loaded successfully")
                            },
                            onError = {
                                android.util.Log.e("CardGridItem", "Error loading thumbnail: ${card.thumbnailUrl}")
                            }
                        )

                        // Play button overlay for video content
                        if (card.type == CardType.URL && (card.source.contains("youtube") ||
                            card.source.contains("vimeo") || card.metadata?.contains("video") == true)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF000000).copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Duration overlay if available
                            card.videoDuration?.let { duration ->
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF000000).copy(alpha = 0.7f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = duration,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    android.util.Log.d("CardGridItem", "No thumbnail for card: ${card.id}, type: ${card.type}")
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tags (show up to 2 in grid view)
                    if (card.tags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            card.tags.take(2).forEach { tag ->
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
}
