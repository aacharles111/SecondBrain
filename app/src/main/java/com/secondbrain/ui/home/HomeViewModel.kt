package com.secondbrain.ui.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.Card
import com.secondbrain.data.repository.CardRepository
import com.secondbrain.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository,
    application: Application
) : AndroidViewModel(application) {

    // Sort options
    var sortMenuExpanded by mutableStateOf(false)
    var selectedSortOption by mutableStateOf("Date (newest first)")
    val sortOptions = listOf(
        "Date (newest first)",
        "Date (oldest first)",
        "Title (A-Z)",
        "Title (Z-A)"
    )

    // Network state
    var isOnline by mutableStateOf(true)

    // Cards data
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: Flow<List<Card>> = _cards

    init {
        loadCards()
        monitorNetworkConnectivity()
    }

    private fun loadCards() {
        viewModelScope.launch {
            try {
                cardRepository.getCards().collect { cardList ->
                    _cards.value = sortCardsList(cardList)
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading cards", e)
                _cards.value = emptyList()
            }
        }
    }

    fun sortCards() {
        _cards.value = sortCardsList(_cards.value)
    }

    private fun sortCardsList(cardList: List<Card>): List<Card> {
        return when (selectedSortOption) {
            "Date (newest first)" -> cardList.sortedByDescending { it.createdAt }
            "Date (oldest first)" -> cardList.sortedBy { it.createdAt }
            "Title (A-Z)" -> cardList.sortedBy { it.title }
            "Title (Z-A)" -> cardList.sortedByDescending { it.title }
            else -> cardList
        }
    }

    private fun monitorNetworkConnectivity() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check initial state
        isOnline = isNetworkAvailable(connectivityManager)

        // Monitor for changes
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }

            override fun onLost(network: Network) {
                isOnline = isNetworkAvailable(connectivityManager)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Card actions
    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard

    private val _pinnedCards = MutableStateFlow<List<String>>(emptyList())
    val pinnedCards: StateFlow<List<String>> = _pinnedCards

    init {
        // Load pinned cards from settings
        viewModelScope.launch {
            settingsRepository.getPinnedCards().collect { pinnedCardIds ->
                _pinnedCards.value = pinnedCardIds
            }
        }
    }

    fun selectCard(card: Card?) {
        _selectedCard.value = card
    }

    fun deleteCard(cardId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = cardRepository.deleteCard(cardId)
                if (result.isSuccess) {
                    // If the card was pinned, remove it from pinned cards
                    if (_pinnedCards.value.contains(cardId)) {
                        unpinCard(cardId)
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error deleting card", e)
            }
        }
    }

    fun duplicateCard(card: Card, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Create a copy with a new ID
                val duplicatedCard = card.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "${card.title} (Copy)",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val result = cardRepository.saveCard(duplicatedCard)
                if (result.isSuccess) {
                    onSuccess(result.getOrNull() ?: "")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error duplicating card", e)
            }
        }
    }

    fun pinCard(cardId: String) {
        viewModelScope.launch {
            try {
                val currentPinnedCards = _pinnedCards.value.toMutableList()
                if (!currentPinnedCards.contains(cardId)) {
                    currentPinnedCards.add(cardId)
                    _pinnedCards.value = currentPinnedCards
                    settingsRepository.savePinnedCards(currentPinnedCards)
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error pinning card", e)
            }
        }
    }

    fun unpinCard(cardId: String) {
        viewModelScope.launch {
            try {
                val currentPinnedCards = _pinnedCards.value.toMutableList()
                if (currentPinnedCards.contains(cardId)) {
                    currentPinnedCards.remove(cardId)
                    _pinnedCards.value = currentPinnedCards
                    settingsRepository.savePinnedCards(currentPinnedCards)
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error unpinning card", e)
            }
        }
    }

    fun isPinned(cardId: String): Boolean {
        return _pinnedCards.value.contains(cardId)
    }

    fun shareCard(card: Card): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, card.title)
            putExtra(Intent.EXTRA_TEXT, "${card.title}\n\n${card.content}")
            type = "text/plain"
        }
    }
}
