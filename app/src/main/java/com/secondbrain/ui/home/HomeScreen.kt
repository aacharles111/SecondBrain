package com.secondbrain.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.model.Card
import com.secondbrain.ui.card.CreateCardActivity
import android.content.Intent

enum class ViewType {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
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
                    ViewType.LIST -> CardListView(cards = cards.value)
                    ViewType.GRID -> CardGridView(cards = cards.value)
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
fun CardListView(cards: List<Card>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(cards) { card ->
            CardListItem(card = card)
        }
    }
}

@Composable
fun CardGridView(cards: List<Card>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(cards) { card ->
            CardGridItem(card = card)
        }
    }
}

@Composable
fun CardListItem(card: Card) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Thumbnail
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                // If there's a thumbnail URL, load it here
                // For now, just show a placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🖼️")
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = card.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tags
                Row {
                    card.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { /* Tag click action */ },
                            label = { Text("#$tag", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardGridItem(card: Card) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column {
            // Thumbnail
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                // If there's a thumbnail URL, load it here
                // For now, just show a placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🖼️")
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = card.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tags (show only 1 in grid view)
                if (card.tags.isNotEmpty()) {
                    AssistChip(
                        onClick = { /* Tag click action */ },
                        label = { Text("#${card.tags.first()}", style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
        }
    }
}
