package com.secondbrain.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.secondbrain.R
import com.secondbrain.data.model.Note
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val noteState by viewModel.noteState.collectAsState()
    val backlinksState by viewModel.backlinksState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (noteState) {
                        is NoteDetailState.Success -> {
                            Text((noteState as NoteDetailState.Success).note.title)
                        }
                        else -> {
                            Text(stringResource(R.string.app_name))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (noteState is NoteDetailState.Success) {
                        IconButton(onClick = {
                            val note = (noteState as NoteDetailState.Success).note
                            navController.navigate("note/edit/${note.id}")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_edit)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_delete)
                            )
                        }
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
            when (noteState) {
                is NoteDetailState.Loading -> {
                    CircularProgressIndicator()
                }
                is NoteDetailState.Success -> {
                    val note = (noteState as NoteDetailState.Success).note
                    NoteDetailContent(
                        note = note,
                        backlinksState = backlinksState,
                        onBacklinkClick = { backlink ->
                            navController.navigate("note/${backlink.id}")
                        }
                    )
                }
                is NoteDetailState.Error -> {
                    val errorMessage = (noteState as NoteDetailState.Error).message
                    Text("Error: $errorMessage")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote()
                        showDeleteDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
fun NoteDetailContent(
    note: Note,
    backlinksState: BacklinksState,
    onBacklinkClick: (Note) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Note content
        MarkdownText(
            markdown = note.content,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metadata
        if (note.sourceUrl != null) {
            Text(
                text = "Source: ${note.sourceUrl}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Backlinks section
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "Linked Mentions",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (backlinksState) {
            is BacklinksState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is BacklinksState.Success -> {
                val backlinks = backlinksState.backlinks
                if (backlinks.isEmpty()) {
                    Text("No linked mentions yet")
                } else {
                    Column {
                        backlinks.forEach { backlink ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBacklinkClick(backlink) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = backlink.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            is BacklinksState.Error -> {
                val errorMessage = backlinksState.message
                Text("Error loading backlinks: $errorMessage")
            }
        }
    }
}
