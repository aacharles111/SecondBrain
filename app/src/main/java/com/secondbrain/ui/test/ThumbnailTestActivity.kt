package com.secondbrain.ui.test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.secondbrain.data.model.CardType
import com.secondbrain.data.service.ThumbnailService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ThumbnailTestActivity : ComponentActivity() {

    @Inject
    lateinit var thumbnailService: ThumbnailService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThumbnailTestScreen(thumbnailService)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailTestScreen(thumbnailService: ThumbnailService) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var thumbnailUrl by remember { mutableStateOf<String?>(null) }
    var testUrl by remember { mutableStateOf("https://www.youtube.com/watch?v=ps7ZpXGIuBA") }
    var cardType by remember { mutableStateOf(CardType.URL) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thumbnail Test") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = testUrl,
                    onValueChange = { testUrl = it },
                    label = { Text("URL to test") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardType.values().forEach { type ->
                        Button(
                            onClick = { cardType = type },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cardType == type) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(type.name)
                        }
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val url = thumbnailService.getThumbnailForUrl(
                                    url = testUrl,
                                    type = cardType,
                                    title = "Test Title"
                                )
                                thumbnailUrl = url
                                Log.d("ThumbnailTest", "Generated thumbnail URL: $url")
                                isLoading = false
                            } catch (e: Exception) {
                                Log.e("ThumbnailTest", "Error generating thumbnail", e)
                                errorMessage = e.message
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Thumbnail")
                }
            }
            
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (!thumbnailUrl.isNullOrEmpty()) {
                    Text("Thumbnail URL: $thumbnailUrl")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Generated Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        onSuccess = { 
                            Log.d("ThumbnailTest", "Thumbnail loaded successfully") 
                        },
                        onError = { 
                            Log.e("ThumbnailTest", "Error loading thumbnail: $thumbnailUrl") 
                        }
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
