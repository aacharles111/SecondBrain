package com.secondbrain.ui.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummaryReviewScreen(
    cardId: String,
    viewModel: SummaryReviewViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    // Load the card data when the screen is first composed
    LaunchedEffect(cardId) {
        viewModel.loadCardById(cardId)
    }
    
    // Show error dialog if there's an error
    viewModel.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage = null },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.errorMessage = null
                    onClose()
                }) {
                    Text("OK")
                }
            },
            // Add retry button for retryable errors
            dismissButton = {
                if (error.contains("server is currently overloaded") ||
                    error.contains("Rate limit exceeded") ||
                    error.contains("Temporary server error")) {
                    TextButton(onClick = {
                        viewModel.errorMessage = null
                        viewModel.retryLastOperation()
                    }) {
                        Text("Retry")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Summary") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_cancel)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Content
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Source info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Source: ${viewModel.sourceType}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Show clickable URL for URL cards
                    if (viewModel.sourceType == "URL" && viewModel.sourceUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val uriHandler = LocalUriHandler.current
                        TextButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(viewModel.sourceUrl)
                                } catch (e: Exception) {
                                    android.util.Log.e("SummaryReviewScreen", "Error opening URL", e)
                                }
                            },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = viewModel.sourceUrl,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 200.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title with regenerate button
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.title = it },
                        label = { Text("Title") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.generateTitle() },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate Title")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tags with regenerate button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tags: ",
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    IconButton(onClick = { viewModel.generateTags() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate Tags")
                    }
                }

                // Tag chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    viewModel.tags.forEach { tag ->
                        AssistChip(
                            onClick = { /* Tag click action */ },
                            label = { Text("#$tag") },
                            modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                        )
                    }
                    TextButton(onClick = { /* Add tag logic */ }) {
                        Text("#add_tag")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language and AI model info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Language: ${viewModel.language}")
                    Text("AI Model: ${viewModel.aiModel}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary content
                OutlinedTextField(
                    value = viewModel.summary,
                    onValueChange = { viewModel.summary = it },
                    label = { Text("Summary") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    minLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.regenerateSummary() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.action_regenerate))
                    }

                    Button(
                        onClick = { /* Edit action */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.action_edit))
                    }

                    Button(
                        onClick = {
                            viewModel.saveCard()
                            onSave()
                        }
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
                
                // Add some bottom padding for better scrolling
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
