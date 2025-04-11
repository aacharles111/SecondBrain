package com.secondbrain.ui.card

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.secondbrain.R
import com.secondbrain.data.model.WebSearchResult
import com.secondbrain.ui.theme.SecondBrainTheme
import dagger.hilt.android.AndroidEntryPoint
import android.net.Uri
import android.widget.Toast

@AndroidEntryPoint
class CreateCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up error handling
        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CreateCardActivity", "Uncaught exception in thread: ${thread.name}", throwable)
            // Show a toast and finish the activity
            android.widget.Toast.makeText(this, "An error occurred: ${throwable.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }

        try {
            setContent {
                SecondBrainTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CreateCardScreen(
                            onClose = { finish() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CreateCardActivity", "Error in onCreate", e)
            android.widget.Toast.makeText(this, "Error initializing UI: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    viewModel: CreateCardViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_url),
        stringResource(R.string.tab_search),
        stringResource(R.string.tab_pdf),
        stringResource(R.string.tab_note),
        stringResource(R.string.tab_audio)
    )

    // Show error dialog if there's an error
    viewModel.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage = null },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.errorMessage = null }) {
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

    // Handle navigation to summary review
    val context = LocalContext.current
    viewModel.navigateToSummaryReview?.let { cardId ->
        LaunchedEffect(cardId) {
            android.util.Log.d("CreateCardScreen", "Navigating to SummaryReviewActivity with card ID: $cardId")
            val intent = android.content.Intent(context, SummaryReviewActivity::class.java).apply {
                putExtra(SummaryReviewActivity.EXTRA_CARD_ID, cardId)
            }
            context.startActivity(intent)
            // Reset navigation state
            viewModel.navigateToSummaryReview = null
            // Close this activity
            onClose()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = { Text(stringResource(R.string.action_create_card)) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_cancel)
                    )
                }
            }
        )

        // Tab row
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                    icon = {
                        when (index) {
                            0 -> Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null
                            )
                            1 -> Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                            2 -> Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null
                            )
                            3 -> Icon(
                                imageVector = Icons.Default.Note,
                                contentDescription = null
                            )
                            4 -> Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }

        // Tab content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> UrlTab(viewModel)
                1 -> SearchTab(viewModel)
                2 -> PdfTab(viewModel)
                3 -> NoteTab(viewModel)
                4 -> AudioTab(viewModel)
            }
        }

        // Bottom bar with summary options and create button
        BottomBar(viewModel)
    }
}

@Composable
fun UrlTab(viewModel: CreateCardViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.urlInput,
            onValueChange = { viewModel.urlInput = it },
            label = { Text(stringResource(R.string.placeholder_url)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (viewModel.urlInput.isNotEmpty()) {
                    IconButton(onClick = { viewModel.urlInput = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show loading indicator or extracted content
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (viewModel.extractedUrlContent != null) {
            // Show extracted content preview
            val urlContent = viewModel.extractedUrlContent!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = urlContent.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Source: ${urlContent.sourceType}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = urlContent.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            // Show supported sources
            Text(
                text = "Supports:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            SupportedSourceItem(icon = "📺", text = stringResource(R.string.content_type_youtube))
            SupportedSourceItem(icon = "🌐", text = stringResource(R.string.content_type_websites))
            SupportedSourceItem(icon = "📑", text = stringResource(R.string.content_type_online_pdfs))
            SupportedSourceItem(icon = "📄", text = stringResource(R.string.content_type_google_docs))
            SupportedSourceItem(icon = "📊", text = stringResource(R.string.content_type_google_slides))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Language: ")
            DropdownMenu(
                expanded = viewModel.languageMenuExpanded,
                onDismissRequest = { viewModel.languageMenuExpanded = false }
            ) {
                viewModel.supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            viewModel.selectedLanguage = language
                            viewModel.languageMenuExpanded = false
                        }
                    )
                }
            }
            TextButton(onClick = { viewModel.languageMenuExpanded = true }) {
                Text(viewModel.selectedLanguage)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Generate AI Summary",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = viewModel.urlSummaryEnabled,
                onCheckedChange = { viewModel.urlSummaryEnabled = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Tip card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Tip: Use your browser's share option to quickly add content.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTab(viewModel: CreateCardViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar with search button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                label = { Text("Search anything...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.performSearch() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.performSearch() },
                enabled = viewModel.searchQuery.isNotEmpty() && !viewModel.isSearching
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sources selection
        Text(
            text = "Sources:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Source checkboxes in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.searchSources.forEach { source ->
                FilterChip(
                    selected = viewModel.selectedSearchSources.contains(source),
                    onClick = {
                        if (viewModel.selectedSearchSources.contains(source)) {
                            if (viewModel.selectedSearchSources.size > 1) { // Ensure at least one source is selected
                                viewModel.selectedSearchSources.remove(source)
                            }
                        } else {
                            viewModel.selectedSearchSources.add(source)
                        }
                    },
                    label = { Text(source) },
                    leadingIcon = {
                        if (viewModel.selectedSearchSources.contains(source)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            }
        }

        // Language selector
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Language: ")
            DropdownMenu(
                expanded = viewModel.languageMenuExpanded,
                onDismissRequest = { viewModel.languageMenuExpanded = false }
            ) {
                viewModel.supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            viewModel.selectedLanguage = language
                            viewModel.languageMenuExpanded = false
                        }
                    )
                }
            }
            TextButton(onClick = { viewModel.languageMenuExpanded = true }) {
                Text(viewModel.selectedLanguage)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search results or placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                viewModel.isSearching -> {
                    // Loading indicator
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Searching...")
                    }
                }
                viewModel.searchError != null -> {
                    // Error message
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.searchError ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.searchError = null }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
                viewModel.searchResults.isNotEmpty() -> {
                    // Search results
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.searchResults) { result ->
                            SearchResultItem(
                                result = result,
                                isSelected = result == viewModel.selectedSearchResult,
                                onSelect = { viewModel.selectSearchResult(result) }
                            )
                        }
                    }
                }
                viewModel.searchQuery.isNotEmpty() -> {
                    // No results for query
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try a different search term or select different sources.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                else -> {
                    // Initial state
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Search the web",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enter a query above to search from selected sources.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Summary toggle
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Generate AI Summary",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = viewModel.searchSummaryEnabled,
                onCheckedChange = { viewModel.searchSummaryEnabled = it }
            )
        }

        // Selected result info
        viewModel.selectedSearchResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selected: ${result.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Source: ${result.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.selectedSearchResult = null }
                        ) {
                            Text("Deselect")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val uriHandler = LocalUriHandler.current
                        TextButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(result.url)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Open URL")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultItem(
    result: WebSearchResult,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source icon or thumbnail
            if (result.thumbnailUrl != null) {
                AsyncImage(
                    model = result.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Source icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when (result.source) {
                                "Google" -> MaterialTheme.colorScheme.primary
                                "Wikipedia" -> Color(0xFF3366CC)
                                "WikiData" -> Color(0xFF339966)
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (result.source) {
                            "Google" -> Icons.Default.Search
                            "Wikipedia" -> Icons.Default.Info
                            "WikiData" -> Icons.Default.Storage
                            else -> Icons.Default.Link
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = result.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = result.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PdfTab(viewModel: CreateCardViewModel) {
    val context = LocalContext.current
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadPdf(it, context) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // PDF upload button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📄 Upload PDF (Max 50MB)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to select a PDF file",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                        Text("Select PDF")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show uploaded PDF info
        if (viewModel.extractedPdfContent != null) {
            val pdfContent = viewModel.extractedPdfContent!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = pdfContent.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Author: ${pdfContent.author}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Pages: ${pdfContent.pageCount}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = pdfContent.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Text(
                text = "Uploaded: ${viewModel.uploadedPdfFiles.size}/1 files",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PDF Options:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // PDF options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = viewModel.extractAllText,
                onCheckedChange = { viewModel.extractAllText = it }
            )
            Text(
                text = "Extract all text",
                modifier = Modifier.padding(start = 8.dp, top = 12.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = viewModel.extractSpecificPages,
                onCheckedChange = { viewModel.extractSpecificPages = it }
            )
            Text(
                text = "Extract specific pages:",
                modifier = Modifier.padding(start = 8.dp, top = 12.dp)
            )
        }

        if (viewModel.extractSpecificPages) {
            OutlinedTextField(
                value = viewModel.pageRanges,
                onValueChange = { viewModel.pageRanges = it },
                label = { Text("e.g., 1-5, 8, 10") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Language: ")
            DropdownMenu(
                expanded = viewModel.languageMenuExpanded,
                onDismissRequest = { viewModel.languageMenuExpanded = false }
            ) {
                viewModel.supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            viewModel.selectedLanguage = language
                            viewModel.languageMenuExpanded = false
                        }
                    )
                }
            }
            TextButton(onClick = { viewModel.languageMenuExpanded = true }) {
                Text(viewModel.selectedLanguage)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Generate AI Summary",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = viewModel.pdfSummaryEnabled,
                onCheckedChange = { viewModel.pdfSummaryEnabled = it }
            )
        }
    }
}

@Composable
fun NoteTab(viewModel: CreateCardViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.noteTitle,
            onValueChange = { viewModel.noteTitle = it },
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
            TextButton(onClick = { /* Add tag logic */ }) {
                Text("#add_tag")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Language selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Language: ")
            DropdownMenu(
                expanded = viewModel.languageMenuExpanded,
                onDismissRequest = { viewModel.languageMenuExpanded = false }
            ) {
                viewModel.supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            viewModel.selectedLanguage = language
                            viewModel.languageMenuExpanded = false
                        }
                    )
                }
            }
            TextButton(onClick = { viewModel.languageMenuExpanded = true }) {
                Text(viewModel.selectedLanguage)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note content
        OutlinedTextField(
            value = viewModel.noteContent,
            onValueChange = { viewModel.noteContent = it },
            label = { Text("Write something...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Generate AI Summary",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = viewModel.noteSummaryEnabled,
                onCheckedChange = { viewModel.noteSummaryEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Formatting toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { /* Bold */ }) { Text("B", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
            TextButton(onClick = { /* Italic */ }) { Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
            TextButton(onClick = { /* Underline */ }) { Text("U") }
            TextButton(onClick = { /* List */ }) { Text("≡") }
            TextButton(onClick = { /* Bullet */ }) { Text("•") }
            TextButton(onClick = { /* Checkbox */ }) { Text("[]") }
            TextButton(onClick = { /* Code */ }) { Text("<>") }
            TextButton(onClick = { /* Link */ }) { Text("🔗") }
            TextButton(onClick = { /* Image */ }) { Text("📷") }
            TextButton(onClick = { /* Color */ }) { Text("🎨") }
        }
    }
}

@Composable
fun AudioTab(viewModel: CreateCardViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.audioTitle,
            onValueChange = { viewModel.audioTitle = it },
            label = { Text("Title (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Record or upload buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* Record logic */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("🎙️ Record")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { /* Upload logic */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("📂 Upload")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Audio files: 0/5 (Max 50MB each)",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Audio placeholder
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No audio recorded or uploaded",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap Record to start or Upload to select an audio file.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transcription language
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Transcription language: ",
                modifier = Modifier.padding(top = 8.dp)
            )
            DropdownMenu(
                expanded = viewModel.transcriptionLanguageMenuExpanded,
                onDismissRequest = { viewModel.transcriptionLanguageMenuExpanded = false }
            ) {
                viewModel.supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            viewModel.selectedTranscriptionLanguage = language
                            viewModel.transcriptionLanguageMenuExpanded = false
                        }
                    )
                }
            }
            TextButton(onClick = { viewModel.transcriptionLanguageMenuExpanded = true }) {
                Text(viewModel.selectedTranscriptionLanguage)
            }
        }

        Text(
            text = "(Also supports: Tamil, Hindi)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Generate AI Summary",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = viewModel.audioSummaryEnabled,
                onCheckedChange = { viewModel.audioSummaryEnabled = it }
            )
        }
    }
}

@Composable
fun BottomBar(viewModel: CreateCardViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Summary type dropdown
        Box {
            TextButton(
                onClick = { viewModel.summaryOptionsExpanded = true },
                enabled = !viewModel.isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                    Text(viewModel.selectedSummaryType)
                }
            }

            DropdownMenu(
                expanded = viewModel.summaryOptionsExpanded,
                onDismissRequest = { viewModel.summaryOptionsExpanded = false }
            ) {
                viewModel.summaryTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            viewModel.selectedSummaryType = type
                            viewModel.summaryOptionsExpanded = false
                        }
                    )
                }
            }
        }

        // Create button
        Button(
            onClick = { viewModel.createCardFromActiveTab() },
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(stringResource(R.string.action_create_card))
        }
    }
}

@Composable
fun SupportedSourceItem(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = icon,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text)
    }
}
