package com.secondbrain.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GeminiModelScreen(
    viewModel: GeminiModelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    ModelSelectorScreen(
        title = "Gemini Models",
        viewModel = viewModel,
        onBackClick = onNavigateBack
    )
}
