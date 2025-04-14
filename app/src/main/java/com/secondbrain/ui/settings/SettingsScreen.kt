package com.secondbrain.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import com.secondbrain.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAiSettings: () -> Unit = {},
    onNavigateToSystemPromptSettings: () -> Unit = {}
) {
    // Collect settings
    val darkMode by viewModel.darkMode.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val defaultAiModel by viewModel.defaultAiModel.collectAsState()
    val defaultLanguage by viewModel.defaultLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                Text(
                    text = "Customize the app's appearance",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Dark mode options
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Radio buttons for dark mode options
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    DarkModeOption(
                        text = "System default",
                        selected = darkMode == "system",
                        onClick = { viewModel.setDarkMode("system") }
                    )
                    DarkModeOption(
                        text = "Light",
                        selected = darkMode == "light",
                        onClick = { viewModel.setDarkMode("light") }
                    )
                    DarkModeOption(
                        text = "Dark",
                        selected = darkMode == "dark",
                        onClick = { viewModel.setDarkMode("dark") }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                // Dynamic colors toggle
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Use dynamic colors",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Uses colors from your wallpaper (Android 12+)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = useDynamicColors,
                        onCheckedChange = { viewModel.setUseDynamicColors(it) },
                        enabled = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Integration Section
            SettingsSection(title = stringResource(R.string.settings_ai_integration)) {
                Text(
                    text = "Configure your AI providers and API keys here.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // AI provider settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAiSettings() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Providers & API Keys",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Configure AI providers and API keys",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to AI settings"
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // System prompt settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSystemPromptSettings() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "System Prompts",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Customize AI system prompts for different content types",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate to system prompt settings"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Section
            SettingsSection(title = stringResource(R.string.settings_storage)) {
                Text(
                    text = "Configure where your notes are stored.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Placeholder for storage settings
                Text(
                    text = "Storage settings coming soon",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Management Section
            SettingsSection(title = stringResource(R.string.settings_storage_management)) {
                Text(
                    text = "Manage your storage space.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Delete originals toggle
                var deleteOriginals by remember { mutableStateOf(false) }
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.storage_delete_originals),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = deleteOriginals,
                        onCheckedChange = { deleteOriginals = it }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Placeholder for more storage management settings
                Text(
                    text = "More storage management options coming soon",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            SettingsSection(title = "About") {
                Text(
                    text = "Second Brain v1.0",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Text(
                    text = "An open-source personal knowledge management app",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()
            content()
        }
    }
}

@Composable
fun DarkModeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
