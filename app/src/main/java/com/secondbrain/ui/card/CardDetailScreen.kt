package com.secondbrain.ui.card

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.model.Card
import com.secondbrain.data.model.CardType
import com.secondbrain.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    viewModel: CardDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val cardState by viewModel.cardState.collectAsState()

    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val card = (cardState as? CardDetailState.Success)?.card
                        if (card != null) {
                            onNavigateToEdit(card.id)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = {
                        val card = (cardState as? CardDetailState.Success)?.card
                        if (card != null) {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, card.title)
                                putExtra(Intent.EXTRA_TEXT, "${card.title}\n\n${card.content}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Card"))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (cardState) {
                is CardDetailState.Loading -> {
                    CircularProgressIndicator()
                }
                is CardDetailState.Success -> {
                    val card = (cardState as CardDetailState.Success).card
                    CardDetailContent(card = card)
                }
                is CardDetailState.Error -> {
                    val errorMessage = (cardState as CardDetailState.Error).message
                    Text("Error: $errorMessage")
                }
            }
        }
    }
}

@Composable
fun CardDetailContent(card: Card) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Text(
            text = card.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tags
        if (card.tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Start
            ) {
                card.tags.forEach { tag ->
                    AssistChip(
                        onClick = { /* Tag click action */ },
                        label = { Text("#$tag") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Display thumbnail for all card types if available
        if (!card.thumbnailUrl.isNullOrEmpty()) {
            // Log the thumbnail URL for debugging
            android.util.Log.d("CardDetailScreen", "Displaying thumbnail: ${card.thumbnailUrl}")

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(card.thumbnailUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                onSuccess = {
                    android.util.Log.d("CardDetailScreen", "Thumbnail loaded successfully")
                },
                onError = {
                    android.util.Log.e("CardDetailScreen", "Error loading thumbnail: ${card.thumbnailUrl}")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        } else {
            android.util.Log.d("CardDetailScreen", "No thumbnail available for card: ${card.id}, type: ${card.type}")
        }

        // Source information (if available)
        if (card.source.isNotEmpty() && card.source != "Note") {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Source",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = card.source,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Summary
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Use MarkdownText for summary to properly render markdown formatting
                MarkdownText(
                    markdown = card.summary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display content based on card type
                when (card.type) {
                    CardType.PDF, CardType.URL, CardType.SEARCH -> {
                        MarkdownText(
                            markdown = card.content,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    CardType.NOTE -> {
                        MarkdownText(
                            markdown = card.content,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    CardType.AUDIO -> {
                        Text(
                            text = card.content,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // TODO: Add audio player if needed
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metadata
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Metadata",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Use a grid-like layout for metadata
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = card.type.name,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Language",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = card.language,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Model",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = card.aiModel,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Summary Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = card.summaryType,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (card.pageCount != null) {
                    Text(
                        text = "Pages: ${card.pageCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
