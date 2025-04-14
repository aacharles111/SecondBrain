package com.secondbrain.ui.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.secondbrain.ui.components.SearchBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.ModelCapability

/**
 * Generic model selector screen for AI providers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorScreen(
    title: String,
    viewModel: BaseModelSelectorViewModel,
    onBackClick: () -> Unit
) {
    val models by viewModel.models.collectAsState()
    val filteredModels by viewModel.filteredModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Fetch models when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.refreshModels()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (models.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No models available. Please check your API key.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    placeholder = "Search models..."
                )

                // Model list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredModels.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No models found matching '${searchQuery}'")
                            }
                        }
                    } else {
                        items(filteredModels) { model ->
                            ModelCard(
                                model = model,
                                isSelected = model.id == selectedModel?.id,
                                onSelect = { viewModel.selectModel(model) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelCard(
    model: AiModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Context: ${formatTokenCount(model.contextWindow)} tokens",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                val capabilities = formatCapabilities(model.capabilities)
                Text(
                    text = "Supports: ${capabilities}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (capabilities.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Format token count for display (e.g., 32768 -> 32K)
 */
private fun formatTokenCount(tokens: Int): String {
    return when {
        tokens >= 1_000_000 -> "${tokens / 1_000_000}M"
        tokens >= 1_000 -> "${tokens / 1_000}K"
        else -> tokens.toString()
    }
}

/**
 * Format capabilities for display, focusing on supported file formats
 */
private fun formatCapabilities(capabilities: Set<ModelCapability>): String {
    // First, extract content type capabilities
    val contentTypes = capabilities.filter { capability ->
        when (capability) {
            ModelCapability.TEXT_CONTENT,
            ModelCapability.IMAGE_UNDERSTANDING,
            ModelCapability.AUDIO_TRANSCRIPTION,
            ModelCapability.PDF_PROCESSING,
            ModelCapability.WEB_CONTENT -> true
            else -> false
        }
    }

    // If no content types are specified, show feature capabilities instead
    val capabilitiesToShow = if (contentTypes.isEmpty()) capabilities else contentTypes

    val capabilityNames = capabilitiesToShow.map {
        when (it) {
            // Content types
            ModelCapability.TEXT_CONTENT -> "Text"
            ModelCapability.IMAGE_UNDERSTANDING -> "Images"
            ModelCapability.AUDIO_TRANSCRIPTION -> "Audio"
            ModelCapability.PDF_PROCESSING -> "PDFs"
            ModelCapability.WEB_CONTENT -> "Web Links"

            // Features (shown only if no content types are available)
            ModelCapability.TEXT_SUMMARIZATION -> "Summarization"
            ModelCapability.TAG_GENERATION -> "Tags"
            ModelCapability.TITLE_GENERATION -> "Titles"
            ModelCapability.CODE_UNDERSTANDING -> "Code"
        }
    }

    return capabilityNames.joinToString(", ")
}
