package com.secondbrain.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.service.ai.AiProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    viewModel: AiSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToOpenRouterModels: () -> Unit = {},
    onNavigateToGeminiModels: () -> Unit = {},
    onNavigateToOpenAiModels: () -> Unit = {},
    onNavigateToClaudeModels: () -> Unit = {},
    onNavigateToDeepSeekModels: () -> Unit = {}
) {
    val providers by viewModel.providers.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Provider selection
            Text(
                text = "Select Default AI Provider",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            providers.forEach { provider ->
                ProviderOption(
                    provider = provider,
                    isSelected = provider.name == selectedProvider?.name,
                    onClick = { viewModel.selectProvider(provider) },
                    onNavigateToOpenRouterModels = onNavigateToOpenRouterModels,
                    onNavigateToGeminiModels = onNavigateToGeminiModels,
                    onNavigateToOpenAiModels = onNavigateToOpenAiModels,
                    onNavigateToClaudeModels = onNavigateToClaudeModels,
                    onNavigateToDeepSeekModels = onNavigateToDeepSeekModels
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // API key management
            Text(
                text = "API Keys",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            providers.forEach { provider ->
                ApiKeyInput(
                    provider = provider,
                    apiKey = apiKeys[provider.name] ?: "",
                    onApiKeyChange = { viewModel.updateApiKey(provider.name, it) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}

@Composable
fun ProviderOption(
    provider: AiProvider,
    isSelected: Boolean,
    onClick: () -> Unit,
    onNavigateToOpenRouterModels: () -> Unit = {},
    onNavigateToGeminiModels: () -> Unit = {},
    onNavigateToOpenAiModels: () -> Unit = {},
    onNavigateToClaudeModels: () -> Unit = {},
    onNavigateToDeepSeekModels: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = provider.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Show model selection button when provider is selected
        if (isSelected) {
            TextButton(
                onClick = when (provider.name) {
                    "Gemini" -> onNavigateToGeminiModels
                    "OpenAI" -> onNavigateToOpenAiModels
                    "Claude" -> onNavigateToClaudeModels
                    "DeepSeek" -> onNavigateToDeepSeekModels
                    "OpenRouter" -> onNavigateToOpenRouterModels
                    else -> { {} }
                }
            ) {
                Text("Select Model")
            }
        }
    }
}

@Composable
fun ApiKeyInput(
    provider: AiProvider,
    apiKey: String,
    onApiKeyChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${provider.name} API Key",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter API key") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (passwordVisible) "Hide API key" else "Show API key"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (apiKey.isNotEmpty()) "API key set" else "No API key set",
            style = MaterialTheme.typography.bodySmall,
            color = if (apiKey.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
