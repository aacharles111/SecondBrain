package com.secondbrain.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.secondbrain.data.model.ai.ContentType
import com.secondbrain.data.model.ai.CostPreference
import com.secondbrain.data.model.ai.CostTier
import com.secondbrain.data.model.ai.Feature
import com.secondbrain.data.model.ai.ModelCapability
import com.secondbrain.data.model.ai.TaskType

/**
 * Cost-aware model selector screen that prioritizes free models
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostAwareModelSelectorScreen(
    title: String,
    viewModel: CostAwareModelSelectorViewModel,
    onBackClick: () -> Unit
) {
    val models by viewModel.models.collectAsState()
    val filteredModels by viewModel.filteredModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val costPreference by viewModel.costPreference.collectAsState()
    val selectedContentType by viewModel.selectedContentType.collectAsState()

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
                            imageVector = Icons.Default.ArrowBack,
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Cost preference selector
                CostPreferenceSelector(
                    costPreference = costPreference,
                    onCostPreferenceChange = { viewModel.updateCostPreference(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Content type filter
                ContentTypeSelector(
                    selectedContentType = selectedContentType,
                    onContentTypeSelected = { viewModel.updateContentType(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search models...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Recommended models section
                val contentType = selectedContentType
                if (contentType != null) {
                    Text(
                        text = "✨ Recommended for ${contentType.name.lowercase().capitalize()} Content",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Get recommended models for the selected content type
                    val recommendedModels = filteredModels.filter { model ->
                        model.supportedContentTypes.contains(contentType) &&
                        model.recommendedFor.contains(mapContentTypeToTaskType(contentType))
                    }.take(3)

                    if (recommendedModels.isNotEmpty()) {
                        recommendedModels.forEach { model ->
                            CostAwareModelCard(
                                model = model,
                                isSelected = model.id == selectedModel?.id,
                                onSelect = { viewModel.selectModel(model) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "No recommended models found for this content type",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // All models section
                Text(
                    text = "All Models",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                                Text("No models found matching '$searchQuery'")
                            }
                        }
                    } else {
                        // Group models by cost tier
                        val groupedModels = filteredModels.groupBy { it.costTier }

                        // Free models first
                        if (groupedModels.containsKey(CostTier.FREE)) {
                            item {
                                Text(
                                    text = "Free Models",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(groupedModels[CostTier.FREE] ?: emptyList()) { model ->
                                CostAwareModelCard(
                                    model = model,
                                    isSelected = model.id == selectedModel?.id,
                                    onSelect = { viewModel.selectModel(model) }
                                )
                            }
                        }

                        // Low cost models
                        if (groupedModels.containsKey(CostTier.LOW_COST)) {
                            item {
                                Text(
                                    text = "Low Cost Models",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                        .padding(top = 16.dp)
                                )
                            }

                            items(groupedModels[CostTier.LOW_COST] ?: emptyList()) { model ->
                                CostAwareModelCard(
                                    model = model,
                                    isSelected = model.id == selectedModel?.id,
                                    onSelect = { viewModel.selectModel(model) }
                                )
                            }
                        }

                        // Medium cost models
                        if (groupedModels.containsKey(CostTier.MEDIUM_COST)) {
                            item {
                                Text(
                                    text = "Medium Cost Models",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                        .padding(top = 16.dp)
                                )
                            }

                            items(groupedModels[CostTier.MEDIUM_COST] ?: emptyList()) { model ->
                                CostAwareModelCard(
                                    model = model,
                                    isSelected = model.id == selectedModel?.id,
                                    onSelect = { viewModel.selectModel(model) }
                                )
                            }
                        }

                        // High cost models
                        if (groupedModels.containsKey(CostTier.HIGH_COST)) {
                            item {
                                Text(
                                    text = "High Cost Models",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                        .padding(top = 16.dp)
                                )
                            }

                            items(groupedModels[CostTier.HIGH_COST] ?: emptyList()) { model ->
                                CostAwareModelCard(
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
}

@Composable
fun CostPreferenceSelector(
    costPreference: CostPreference,
    onCostPreferenceChange: (CostPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Cost Preference",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = costPreference == CostPreference.FREE_ONLY,
                onClick = { onCostPreferenceChange(CostPreference.FREE_ONLY) }
            )
            Text(
                text = "Free Only",
                modifier = Modifier.clickable { onCostPreferenceChange(CostPreference.FREE_ONLY) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = costPreference == CostPreference.PREFER_FREE,
                onClick = { onCostPreferenceChange(CostPreference.PREFER_FREE) }
            )
            Text(
                text = "Prefer Free",
                modifier = Modifier.clickable { onCostPreferenceChange(CostPreference.PREFER_FREE) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = costPreference == CostPreference.BALANCED,
                onClick = { onCostPreferenceChange(CostPreference.BALANCED) }
            )
            Text(
                text = "Balanced",
                modifier = Modifier.clickable { onCostPreferenceChange(CostPreference.BALANCED) }
            )
        }
    }
}

@Composable
fun ContentTypeSelector(
    selectedContentType: ContentType?,
    onContentTypeSelected: (ContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Filter by Content Type",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContentType.values().forEach { contentType ->
                FilterChip(
                    selected = contentType == selectedContentType,
                    onClick = { onContentTypeSelected(contentType) },
                    label = {
                        Text(
                            text = contentType.name.lowercase().capitalize(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = if (contentType == selectedContentType) {
                        { Icon(Icons.Default.Check, contentDescription = "Selected") }
                    } else null
                )
            }
        }
    }
}

@Composable
fun CostAwareModelCard(
    model: ModelCapability,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Cost indicator
                val (costText, costColor) = when (model.costTier) {
                    CostTier.FREE -> "FREE" to Color(0xFF4CAF50)
                    CostTier.LOW_COST -> "$" to Color(0xFF2196F3)
                    CostTier.MEDIUM_COST -> "$$" to Color(0xFFFFC107)
                    CostTier.HIGH_COST -> "$$$" to Color(0xFFF44336)
                }

                Text(
                    text = costText,
                    style = MaterialTheme.typography.labelMedium,
                    color = costColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Capability icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                model.supportedContentTypes.forEach { contentType ->
                    val icon = when (contentType) {
                        ContentType.TEXT -> "📄"
                        ContentType.IMAGE -> "🖼️"
                        ContentType.PDF -> "📑"
                        ContentType.AUDIO -> "🔊"
                        ContentType.WEB_LINK -> "🔗"
                        ContentType.YOUTUBE -> "▶️"
                    }
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                if (model.features.contains(Feature.TAG_GENERATION)) {
                    Text(
                        text = "🏷️",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Token limit and reliability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Up to ${formatTokenCount(model.maxTokens)} tokens",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${(model.reliabilityScore * 100).toInt()}% reliable",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Cost estimate for paid models
            if (model.costTier != CostTier.FREE && model.estimatedCostPer1KTokens != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Est. cost: $${model.estimatedCostPer1KTokens} per 1K tokens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Format token count for display (e.g., 8K, 16K, etc.)
 */
private fun formatTokenCount(tokens: Int): String {
    return when {
        tokens >= 1_000_000 -> "${tokens / 1_000_000}M"
        tokens >= 1_000 -> "${tokens / 1_000}K"
        else -> tokens.toString()
    }
}

/**
 * Map content type to task type
 */
private fun mapContentTypeToTaskType(contentType: ContentType): TaskType {
    return when (contentType) {
        ContentType.TEXT -> TaskType.SHORT_TEXT_SUMMARY
        ContentType.IMAGE -> TaskType.IMAGE_ANALYSIS
        ContentType.PDF -> TaskType.LONG_DOCUMENT_SUMMARY
        ContentType.AUDIO -> TaskType.AUDIO_TRANSCRIPTION
        ContentType.WEB_LINK -> TaskType.WEB_CONTENT_EXTRACTION
        ContentType.YOUTUBE -> TaskType.YOUTUBE_SUMMARY
    }
}

/**
 * Extension function to capitalize the first letter of a string
 */
private fun String.capitalize(): String {
    return if (this.isNotEmpty()) {
        this.first().uppercase() + this.substring(1)
    } else {
        this
    }
}
