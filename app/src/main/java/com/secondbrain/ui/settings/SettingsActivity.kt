package com.secondbrain.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.secondbrain.ui.theme.SecondBrainTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for settings screens
 */
@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SecondBrainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "settings/ai"
                    ) {
                        composable("settings/ai") {
                            AiSettingsScreen(
                                onNavigateBack = { finish() },
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
                    }
                }
            }
        }
    }
}
