package com.secondbrain.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OpenAiModelScreen(
    viewModel: OpenAiModelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    ModelSelectorScreen(
        title = "OpenAI Models",
        viewModel = viewModel,
        onBackClick = onNavigateBack
    )
}
