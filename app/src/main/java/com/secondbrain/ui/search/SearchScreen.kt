package com.secondbrain.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.secondbrain.R
import com.secondbrain.data.model.SearchResult
import com.secondbrain.data.model.SearchResultType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredResults by viewModel.filteredResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val resultCounts by viewModel.resultCounts.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar with clear button
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text(stringResource(R.string.placeholder_search)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Filter chips
        if (resultCounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All results filter
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("All (${filteredResults.size})") },
                    leadingIcon = { Icon(Icons.Default.FilterAlt, contentDescription = null, Modifier.size(16.dp)) }
                )

                // Type-specific filters
                resultCounts.forEach { (type, count) ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { viewModel.setFilter(type) },
                        label = { Text("${getTypeDisplayName(type)} ($count)") },
                        leadingIcon = {
                            Icon(
                                imageVector = getIconForType(type),
                                contentDescription = null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isSearching) {
                CircularProgressIndicator()
            } else if (filteredResults.isEmpty() && searchQuery.isNotEmpty()) {
                Text(stringResource(R.string.msg_no_results))
            } else if (filteredResults.isNotEmpty()) {
                SearchResultsList(filteredResults, navController, searchQuery)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Search across all your content",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Find cards, notes, links, PDFs, and more",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsList(results: List<SearchResult>, navController: NavController, searchQuery: String) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(results) { result ->
            SearchResultItem(result, navController, searchQuery)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultItem(result: SearchResult, navController: NavController, searchQuery: String) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (result.type) {
                    SearchResultType.NOTE -> navController.navigate("note/${result.id}")
                    SearchResultType.CARD_NOTE -> navController.navigate("card/${result.id}")
                    SearchResultType.CARD_URL -> navController.navigate("card/${result.id}")
                    SearchResultType.CARD_PDF -> navController.navigate("card/${result.id}")
                    SearchResultType.CARD_AUDIO -> navController.navigate("card/${result.id}")
                    SearchResultType.CARD_SEARCH -> navController.navigate("card/${result.id}")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Type icon or thumbnail
            if (result.thumbnailUrl != null) {
                AsyncImage(
                    model = result.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getColorForType(result.type)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForType(result.type),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title with highlighting
                Text(
                    text = highlightSearchTerms(result.title, searchQuery),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Matched field indicator
                result.matchedField?.let { field ->
                    Text(
                        text = "Matched in: $field",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Snippet with highlighting
                Text(
                    text = highlightSearchTerms(result.snippet, searchQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tags
                if (result.tags.isNotEmpty()) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        result.tags.take(3).forEach { tag ->
                            AssistChip(
                                onClick = { /* Handle tag click */ },
                                label = { Text("#$tag") },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        if (result.tags.size > 3) {
                            Text(
                                text = "+${result.tags.size - 3} more",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }

                // Source link for URL cards
                if (result.type == SearchResultType.CARD_URL && result.source != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri(result.source)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Highlight search terms in text
 */
@Composable
fun highlightSearchTerms(text: String, searchQuery: String): androidx.compose.ui.text.AnnotatedString {
    if (searchQuery.isBlank()) return androidx.compose.ui.text.AnnotatedString(text)

    val terms = searchQuery.split(" ").filter { it.length > 2 }
    if (terms.isEmpty()) return androidx.compose.ui.text.AnnotatedString(text)

    return buildAnnotatedString {
        var lastIndex = 0

        // Find all occurrences of search terms
        val matches = mutableListOf<Pair<Int, Int>>()
        terms.forEach { term ->
            var startIndex = text.indexOf(term, ignoreCase = true)
            while (startIndex >= 0) {
                matches.add(startIndex to startIndex + term.length)
                startIndex = text.indexOf(term, startIndex + 1, ignoreCase = true)
            }
        }

        // Sort matches by start index
        val sortedMatches = matches.sortedBy { it.first }

        // Merge overlapping matches
        val mergedMatches = mutableListOf<Pair<Int, Int>>()
        sortedMatches.forEach { (start, end) ->
            if (mergedMatches.isEmpty() || start > mergedMatches.last().second) {
                mergedMatches.add(start to end)
            } else if (end > mergedMatches.last().second) {
                val lastMatch = mergedMatches.removeLast()
                mergedMatches.add(lastMatch.first to end)
            }
        }

        // Build the annotated string
        mergedMatches.forEach { (start, end) ->
            append(text.substring(lastIndex, start))
            withStyle(SpanStyle(background = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)) {
                append(text.substring(start, end))
            }
            lastIndex = end
        }

        // Append the rest of the text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

/**
 * Get the icon for a search result type
 */
@Composable
fun getIconForType(type: SearchResultType): ImageVector {
    return when (type) {
        SearchResultType.NOTE -> Icons.Default.Note
        SearchResultType.CARD_NOTE -> Icons.Default.Description
        SearchResultType.CARD_URL -> Icons.Default.Link
        SearchResultType.CARD_PDF -> Icons.Default.PictureAsPdf
        SearchResultType.CARD_AUDIO -> Icons.Default.AudioFile
        SearchResultType.CARD_SEARCH -> Icons.Default.Search
    }
}

/**
 * Get the display name for a search result type
 */
fun getTypeDisplayName(type: SearchResultType): String {
    return when (type) {
        SearchResultType.NOTE -> "Notes"
        SearchResultType.CARD_NOTE -> "Card Notes"
        SearchResultType.CARD_URL -> "Links"
        SearchResultType.CARD_PDF -> "PDFs"
        SearchResultType.CARD_AUDIO -> "Audio"
        SearchResultType.CARD_SEARCH -> "Searches"
    }
}

/**
 * Get the color for a search result type
 */
@Composable
fun getColorForType(type: SearchResultType): Color {
    return when (type) {
        SearchResultType.NOTE -> MaterialTheme.colorScheme.primary
        SearchResultType.CARD_NOTE -> MaterialTheme.colorScheme.secondary
        SearchResultType.CARD_URL -> MaterialTheme.colorScheme.tertiary
        SearchResultType.CARD_PDF -> Color(0xFFE91E63) // Pink
        SearchResultType.CARD_AUDIO -> Color(0xFF9C27B0) // Purple
        SearchResultType.CARD_SEARCH -> Color(0xFF2196F3) // Blue
    }
}
