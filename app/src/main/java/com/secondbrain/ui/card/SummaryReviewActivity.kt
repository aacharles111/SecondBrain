package com.secondbrain.ui.card

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
class SummaryReviewActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CARD_ID = "card_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the card ID from the intent
        val cardId = intent.getStringExtra(EXTRA_CARD_ID) ?: ""

        android.util.Log.d("SummaryReviewActivity", "onCreate: Card ID from intent: $cardId")

        if (cardId.isEmpty()) {
            android.util.Log.e("SummaryReviewActivity", "No card ID provided")
            finish()
            return
        }

        try {
            setContent {
                SecondBrainTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SummaryReviewScreen(
                            cardId = cardId,
                            onClose = { finish() },
                            onSave = { finish() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SummaryReviewActivity", "Error in onCreate", e)
            android.widget.Toast.makeText(this, "Error initializing UI: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
