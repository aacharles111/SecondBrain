package com.secondbrain.ui.knowledge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.secondbrain.data.service.ai.content.Entity
import com.secondbrain.data.service.knowledge.NodeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeGraphScreen(
    cardId: String,
    viewModel: KnowledgeGraphViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCard: (String) -> Unit
) {
    val knowledgeGraph by viewModel.knowledgeGraph.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedEntity by viewModel.selectedEntity.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsState()
    
    // Load knowledge graph
    LaunchedEffect(cardId) {
        viewModel.loadKnowledgeGraph(cardId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Graph") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Knowledge graph visualization
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        knowledgeGraph?.let { graph ->
                            val context = LocalContext.current
                            
                            AndroidView(
                                factory = { ctx ->
                                    KnowledgeGraphView(ctx).apply {
                                        setKnowledgeGraph(graph)
                                        onNodeClickListener = { id, type ->
                                            when (type) {
                                                NodeType.ENTITY -> {
                                                    val entity = graph.entities.find { it.name == id }
                                                    if (entity != null) {
                                                        viewModel.selectEntity(entity)
                                                    }
                                                }
                                                NodeType.CARD -> {
                                                    if (id == graph.centralCard.id) {
                                                        viewModel.selectCard(graph.centralCard)
                                                    } else {
                                                        val card = graph.relatedCards.find { it.id == id }
                                                        if (card != null) {
                                                            viewModel.selectCard(card)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    
                    // Selected entity or card details
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                    ) {
                        selectedEntity?.let { entity ->
                            EntityDetailCard(
                                entity = entity,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } ?: selectedCard?.let { card ->
                            CardDetailCard(
                                card = card,
                                modifier = Modifier.fillMaxWidth(),
                                onCardClick = { onNavigateToCard(card.id) }
                            )
                        } ?: run {
                            // No selection
                            Text(
                                text = "Tap on a node to see details",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EntityDetailCard(
    entity: Entity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = entity.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "Type: ${entity.type.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                text = entity.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailCard(
    card: com.secondbrain.data.model.Card,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onCardClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "Tags: ${card.tags.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = card.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
