package com.secondbrain.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.model.CardType
import com.secondbrain.data.service.ai.SummaryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemPromptSettingsScreen(
    viewModel: SystemPromptSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val selectedContentType by viewModel.selectedContentType.collectAsState()
    val selectedSummaryType by viewModel.selectedSummaryType.collectAsState()
    val currentPrompt by viewModel.currentPrompt.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Prompts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(
                            onClick = { viewModel.savePrompt() },
                            enabled = !isSaving
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.startEditing() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.resetPrompt() },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset to Default"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Content type selector
            Text(
                text = "Content Type",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Use a horizontally scrollable row for content type buttons
            Row(modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
            ) {
                SingleChoiceSegmentedButtonRow {
                    CardType.values().forEach { contentType ->
                        SegmentedButton(
                            selected = contentType == selectedContentType,
                            onClick = { viewModel.selectContentType(contentType) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = contentType.ordinal,
                                count = CardType.values().size
                            ),
                            // Set minimum width to ensure consistent sizing
                            modifier = Modifier.widthIn(min = 80.dp)
                        ) {
                            Text(
                                text = contentType.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary type selector
            Text(
                text = "Summary Type",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Use a horizontally scrollable row for summary type buttons
            Row(modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
            ) {
                SingleChoiceSegmentedButtonRow {
                    SummaryType.values().forEach { summaryType ->
                        SegmentedButton(
                            selected = summaryType == selectedSummaryType,
                            onClick = { viewModel.selectSummaryType(summaryType) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = summaryType.ordinal,
                                count = SummaryType.values().size
                            ),
                            // Set minimum width to ensure consistent sizing
                            modifier = Modifier.widthIn(min = 100.dp)
                        ) {
                            Text(
                                text = summaryType.displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // System prompt editor
            Text(
                text = "System Prompt",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = currentPrompt,
                    onValueChange = { viewModel.updatePrompt(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    enabled = !isSaving
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = currentPrompt,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Help text
            Text(
                text = "System prompts are instructions sent to the AI model to guide how it generates summaries. " +
                      "Different content types and summary types use different prompts to optimize results.",
                style = MaterialTheme.typography.bodySmall
            )

            if (isSaving) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
