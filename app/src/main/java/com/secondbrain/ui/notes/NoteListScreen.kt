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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.secondbrain.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteListScreen(
    navController: NavController,
    sharedText: String? = null,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    // If there's shared text, navigate to create a new note with that text
    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrEmpty()) {
            // TODO: Navigate to new note screen with shared text
            // For now, we'll just print it
            println("Shared text: $sharedText")
        }
    }

    val notesState by viewModel.notesState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("note/new")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_new_note)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (notesState) {
                is NotesState.Loading -> {
                    CircularProgressIndicator()
                }
                is NotesState.Success -> {
                    val notes = (notesState as NotesState.Success).notes
                    if (notes.isEmpty()) {
                        Text("No notes yet. Create one by tapping the + button.")
                    } else {
                        NotesList(notes = notes, navController = navController, onDeleteNote = { noteId ->
                            viewModel.deleteNote(noteId)
                        })
                    }
                }
                is NotesState.Error -> {
                    val errorMessage = (notesState as NotesState.Error).message
                    Text("Error: $errorMessage")
                }
            }
        }
    }
}

@Composable
fun NotesList(
    notes: List<com.secondbrain.data.model.Note>,
    navController: NavController,
    onDeleteNote: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(notes) { note ->
            NoteItem(
                note = note,
                onClick = { navController.navigate("note/${note.id}") },
                onDelete = { onDeleteNote(note.id) }
            )
        }
    }
}

@Composable
fun NoteItem(
    note: com.secondbrain.data.model.Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatDate(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (note.sourceUrl != null) {
                    Text(
                        text = "Source: ${note.sourceUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}
