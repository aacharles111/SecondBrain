package com.secondbrain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.secondbrain.data.repository.SettingsRepository

/**
 * Theme manager that handles dark mode and dynamic colors
 */
object ThemeManager {
    
    /**
     * Apply the app theme based on user preferences
     */
    @Composable
    fun AppTheme(
        settingsRepository: SettingsRepository,
        content: @Composable () -> Unit
    ) {
        // Collect theme preferences
        val darkModePreference by settingsRepository.darkModeFlow.collectAsState(initial = "system")
        val useDynamicColors by settingsRepository.useDynamicColorsFlow.collectAsState(initial = true)
        
        // Determine if dark mode should be used
        val isDarkMode = when (darkModePreference) {
            "dark" -> true
            "light" -> false
            else -> isSystemInDarkTheme()
        }
        
        // Get color scheme
        val colorScheme = getColorScheme(isDarkMode, useDynamicColors)
        
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
    
    /**
     * Get the color scheme based on dark mode and dynamic colors preferences
     */
    @Composable
    private fun getColorScheme(isDarkMode: Boolean, useDynamicColors: Boolean): ColorScheme {
        val context = LocalContext.current
        
        return when {
            useDynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            isDarkMode -> darkColorScheme()
            else -> lightColorScheme()
        }
    }
}
