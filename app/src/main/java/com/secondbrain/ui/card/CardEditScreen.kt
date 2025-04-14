package com.secondbrain.ui.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.model.Card

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    cardId: String,
    viewModel: CardEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val cardState by viewModel.cardState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    
    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveCard(onSuccess = onNavigateBack) },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save"
                        )
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
            when (cardState) {
                is CardEditState.Loading -> {
                    CircularProgressIndicator()
                }
                is CardEditState.Success -> {
                    CardEditContent(
                        viewModel = viewModel,
                        isSaving = isSaving
                    )
                }
                is CardEditState.Error -> {
                    val errorMessage = (cardState as CardEditState.Error).message
                    Text("Error: $errorMessage")
                }
            }
            
            if (isSaving) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun CardEditContent(
    viewModel: CardEditViewModel,
    isSaving: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tags
        OutlinedTextField(
            value = viewModel.tagsText,
            onValueChange = { viewModel.updateTags(it) },
            label = { Text("Tags (comma separated)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary
        OutlinedTextField(
            value = viewModel.summary,
            onValueChange = { viewModel.summary = it },
            label = { Text("Summary") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            enabled = !isSaving
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        OutlinedTextField(
            value = viewModel.content,
            onValueChange = { viewModel.content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
    }
}
