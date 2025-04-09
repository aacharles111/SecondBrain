package com.secondbrain.ui.home

import android.app.Application
import android.content.Context
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cardRepository: CardRepository,
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
}
