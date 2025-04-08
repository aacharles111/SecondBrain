package com.secondbrain.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.secondbrain.ui.theme.SecondBrainTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the intent that started this activity
        val intent = intent
        val action = intent.action
        val type = intent.type
        
        // Handle shared text (URLs or plain text)
        val sharedText = if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            } else null
        } else null
        
        setContent {
            SecondBrainTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SecondBrainApp(sharedText = sharedText)
                }
            }
        }
    }
}
