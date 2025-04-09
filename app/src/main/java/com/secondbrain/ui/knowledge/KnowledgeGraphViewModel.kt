package com.secondbrain.ui.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.Card
import com.secondbrain.data.service.ai.content.Entity
import com.secondbrain.data.service.knowledge.KnowledgeGraph
import com.secondbrain.data.service.knowledge.KnowledgeGraphService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the knowledge graph screen
 */
@HiltViewModel
class KnowledgeGraphViewModel @Inject constructor(
    private val knowledgeGraphService: KnowledgeGraphService
) : ViewModel() {
    
    // Knowledge graph data
    private val _knowledgeGraph = MutableStateFlow<KnowledgeGraph?>(null)
    val knowledgeGraph: StateFlow<KnowledgeGraph?> = _knowledgeGraph.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Selected entity
    private val _selectedEntity = MutableStateFlow<Entity?>(null)
    val selectedEntity: StateFlow<Entity?> = _selectedEntity.asStateFlow()
    
    // Selected card
    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()
    
    /**
     * Load knowledge graph for a card
     */
    fun loadKnowledgeGraph(cardId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedEntity.value = null
            _selectedCard.value = null
            
            try {
                val result = knowledgeGraphService.buildKnowledgeGraph(cardId)
                if (result.isSuccess) {
                    _knowledgeGraph.value = result.getOrNull()
                }
            } catch (e: Exception) {
                // Handle error
                android.util.Log.e("KnowledgeGraphViewModel", "Error loading knowledge graph", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Select an entity
     */
    fun selectEntity(entity: Entity) {
        _selectedEntity.value = entity
        _selectedCard.value = null
    }
    
    /**
     * Select a card
     */
    fun selectCard(card: Card) {
        _selectedCard.value = card
        _selectedEntity.value = null
    }
    
    /**
     * Clear selection
     */
    fun clearSelection() {
        _selectedEntity.value = null
        _selectedCard.value = null
    }
}
