package com.secondbrain.ui.card

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryReviewScreen(
    viewModel: SummaryReviewViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSave: () -> Unit
) {
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
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
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

        // Content
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            // Source info
            Text(
                text = "Source: ${viewModel.sourceType}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Tags: ",
                    modifier = Modifier.padding(top = 8.dp)
                )
                viewModel.tags.forEach { tag ->
                    AssistChip(
                        onClick = { /* Tag click action */ },
                        label = { Text("#$tag") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
                    .weight(1f),
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
        }
    }
    }
}
