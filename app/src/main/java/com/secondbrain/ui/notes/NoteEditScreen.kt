package com.secondbrain.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.secondbrain.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    navController: NavController,
    initialContent: String? = null,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val noteState by viewModel.noteState.collectAsState()
    val title by viewModel.titleState.collectAsState()
    val content by viewModel.contentState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle initial content from shared text
    LaunchedEffect(initialContent) {
        if (!initialContent.isNullOrEmpty() && noteState is NoteEditState.NewNote) {
            viewModel.updateContent(initialContent)
        }
    }

    // Handle save state changes
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                val noteId = (saveState as SaveState.Success).noteId
                scope.launch {
                    snackbarHostState.showSnackbar(message = "Note saved")
                }
                navController.navigate("note/$noteId") {
                    popUpTo("notes") {
                        inclusive = false
                    }
                }
            }
            is SaveState.Error -> {
                val errorMessage = (saveState as SaveState.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(message = "Error: $errorMessage")
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (noteState) {
                            is NoteEditState.NewNote -> stringResource(R.string.action_new_note)
                            is NoteEditState.ExistingNote -> stringResource(R.string.action_edit)
                            else -> ""
                        }
                    )
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
                    IconButton(
                        onClick = { viewModel.saveNote() },
                        enabled = saveState !is SaveState.Saving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.action_save)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (noteState) {
                is NoteEditState.Loading -> {
                    CircularProgressIndicator()
                }
                is NoteEditState.NewNote, is NoteEditState.ExistingNote -> {
                    NoteEditContent(
                        title = title,
                        content = content,
                        onTitleChange = { viewModel.updateTitle(it) },
                        onContentChange = { viewModel.updateContent(it) },
                        isSaving = saveState is SaveState.Saving
                    )
                }
                is NoteEditState.Error -> {
                    val errorMessage = (noteState as NoteEditState.Error).message
                    Text("Error: $errorMessage")
                }
            }
        }
    }
}

@Composable
fun NoteEditContent(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    isSaving: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.placeholder_note_title)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text(stringResource(R.string.placeholder_note_content)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp),
            enabled = !isSaving
        )

        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
    }
}
