package com.secondbrain.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ClaudeModelScreen(
    viewModel: ClaudeModelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    ModelSelectorScreen(
        title = "Claude Models",
        viewModel = viewModel,
        onBackClick = onNavigateBack
    )
}
