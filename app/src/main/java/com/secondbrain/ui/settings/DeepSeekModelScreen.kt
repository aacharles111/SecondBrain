package com.secondbrain.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DeepSeekModelScreen(
    viewModel: DeepSeekModelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    ModelSelectorScreen(
        title = "DeepSeek Models",
        viewModel = viewModel,
        onBackClick = onNavigateBack
    )
}
