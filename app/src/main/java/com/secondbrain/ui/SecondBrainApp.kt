package com.secondbrain.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.secondbrain.R
import com.secondbrain.data.repository.SettingsRepository
import com.secondbrain.ui.card.CardDetailScreen
import com.secondbrain.ui.card.CardEditScreen
import com.secondbrain.ui.home.HomeScreen
import com.secondbrain.ui.knowledge.KnowledgeGraphScreen
import com.secondbrain.ui.notes.NoteDetailScreen
import com.secondbrain.ui.notes.NoteEditScreen
import com.secondbrain.ui.notes.NoteListScreen
import com.secondbrain.ui.search.SearchScreen
import com.secondbrain.ui.settings.AiSettingsScreen
import com.secondbrain.ui.settings.SystemPromptSettingsScreen
import com.secondbrain.ui.settings.ClaudeModelScreen
import com.secondbrain.ui.settings.DeepSeekModelScreen
import com.secondbrain.ui.settings.GeminiModelScreen
import com.secondbrain.ui.settings.OpenAiModelScreen
import com.secondbrain.ui.settings.OpenRouterModelScreen
import com.secondbrain.ui.settings.CostAwareOpenRouterModelScreen
import com.secondbrain.ui.settings.SettingsScreen
import com.secondbrain.ui.settings.SettingsViewModel
import com.secondbrain.ui.theme.ThemeManager

@Composable
fun SecondBrainApp(
    sharedText: String? = null,
    settingsRepository: SettingsRepository = hiltViewModel<SettingsViewModel>().settingsRepository
) {
    // Apply theme based on settings
    ThemeManager.AppTheme(settingsRepository = settingsRepository) {
        val navController = rememberNavController()

        Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(
                    Screen.Notes,
                    Screen.Search,
                    Screen.Settings
                )

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Notes.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Notes.route) {
                HomeScreen(
                    onNavigateToCardDetail = { cardId ->
                        navController.navigate("card/$cardId")
                    },
                    onNavigateToCardEdit = { cardId ->
                        navController.navigate("card/edit/$cardId")
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToAiSettings = {
                        navController.navigate("settings/ai")
                    },
                    onNavigateToSystemPromptSettings = {
                        navController.navigate("settings/system-prompts")
                    }
                )
            }
            composable("settings/ai") {
                AiSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToOpenRouterModels = {
                        navController.navigate("settings/ai/openrouter")
                    },
                    onNavigateToGeminiModels = {
                        navController.navigate("settings/ai/gemini")
                    },
                    onNavigateToOpenAiModels = {
                        navController.navigate("settings/ai/openai")
                    },
                    onNavigateToClaudeModels = {
                        navController.navigate("settings/ai/claude")
                    },
                    onNavigateToDeepSeekModels = {
                        navController.navigate("settings/ai/deepseek")
                    }
                )
            }
            composable("settings/ai/openrouter") {
                // Use the cost-aware model selector instead of the regular one
                CostAwareOpenRouterModelScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("settings/ai/gemini") {
                GeminiModelScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("settings/ai/openai") {
                OpenAiModelScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("settings/ai/claude") {
                ClaudeModelScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("settings/ai/deepseek") {
                DeepSeekModelScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("note/{noteId}") { _ ->
                NoteDetailScreen(
                    navController = navController,
                    onNavigateToKnowledgeGraph = { noteId ->
                        navController.navigate("knowledge-graph/$noteId")
                    }
                )
            }

            composable("knowledge-graph/{cardId}") { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                KnowledgeGraphScreen(
                    cardId = cardId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCard = { relatedCardId ->
                        navController.navigate("note/$relatedCardId")
                    }
                )
            }
            composable("note/edit/{noteId}") { _ ->
                NoteEditScreen(navController = navController)
            }
            composable("note/new") {
                NoteEditScreen(navController = navController, initialContent = sharedText)
            }

            // Card routes
            composable("card/{cardId}") { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                CardDetailScreen(
                    cardId = cardId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { id ->
                        navController.navigate("card/edit/$id")
                    }
                )
            }

            composable("card/edit/{cardId}") { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                CardEditScreen(
                    cardId = cardId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("settings/system-prompts") {
                SystemPromptSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
    }
}

sealed class Screen(val route: String, val resourceId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Notes : Screen("notes", R.string.nav_notes, Icons.Filled.Home)
    object Search : Screen("search", R.string.nav_search, Icons.Filled.Search)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
}
