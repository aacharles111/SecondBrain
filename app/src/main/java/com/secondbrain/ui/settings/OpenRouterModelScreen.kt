package com.secondbrain.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.service.ai.AiModel
import com.secondbrain.data.service.ai.ModelCapability

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenRouterModelScreen(
    viewModel: OpenRouterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val models by viewModel.models.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OpenRouter Models") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshModels() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh models")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(models) { model ->
                        ModelItem(
                            model = model,
                            isSelected = model.id == selectedModel?.id,
                            onClick = { viewModel.selectModel(model) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelItem(
    model: AiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "ID: ${model.id}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Capabilities: ${model.capabilities.joinToString(", ") { formatCapability(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "Context: ${formatTokenCount(model.contextWindow)} tokens",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatCapability(capability: ModelCapability): String {
    return when (capability) {
        ModelCapability.TEXT_SUMMARIZATION -> "Summarization"
        ModelCapability.AUDIO_TRANSCRIPTION -> "Audio"
        ModelCapability.IMAGE_UNDERSTANDING -> "Images"
        ModelCapability.TAG_GENERATION -> "Tags"
        ModelCapability.TITLE_GENERATION -> "Titles"
    }
}

private fun formatTokenCount(tokens: Int): String {
    return when {
        tokens >= 1000000 -> "${tokens / 1000000}M"
        tokens >= 1000 -> "${tokens / 1000}K"
        else -> tokens.toString()
    }
}
