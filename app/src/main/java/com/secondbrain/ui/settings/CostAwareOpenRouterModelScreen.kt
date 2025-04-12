package com.secondbrain.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Cost-aware OpenRouter model selection screen
 */
@Composable
fun CostAwareOpenRouterModelScreen(
    viewModel: CostAwareModelSelectorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    CostAwareModelSelectorScreen(
        title = "OpenRouter Models",
        viewModel = viewModel,
        onBackClick = onNavigateBack
    )
}
