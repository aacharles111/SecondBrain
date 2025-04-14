package com.secondbrain.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OpenRouterModelScreen(
    viewModel: OpenRouterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    ModelSelectorScreen(
        title = "OpenRouter Models",
        viewModel = viewModel,
        onBackClick = onNavigateBack
    )
}


