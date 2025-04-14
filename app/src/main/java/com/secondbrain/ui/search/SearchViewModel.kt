package com.secondbrain.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secondbrain.data.model.SearchResult
import com.secondbrain.data.model.SearchResultType
import com.secondbrain.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedFilter = MutableStateFlow<SearchResultType?>(null)
    val selectedFilter: StateFlow<SearchResultType?> = _selectedFilter.asStateFlow()

    private val _filteredResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val filteredResults: StateFlow<List<SearchResult>> = _filteredResults.asStateFlow()

    // Count of results by type
    private val _resultCounts = MutableStateFlow<Map<SearchResultType, Int>>(emptyMap())
    val resultCounts: StateFlow<Map<SearchResultType, Int>> = _resultCounts.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait for 300ms of inactivity before processing
                .distinctUntilChanged() // Only proceed if the query changed
                .filter { it.length >= 2 } // Only search if query is at least 2 chars
                .onEach { _isSearching.value = true }
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(emptyList())
                    } else {
                        searchRepository.search(query)
                    }
                }
                .collect { results ->
                    _searchResults.value = results
                    updateFilteredResults()
                    updateResultCounts(results)
                    _isSearching.value = false
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _filteredResults.value = emptyList()
            _resultCounts.value = emptyMap()
        }
    }

    /**
     * Set the filter for search results
     */
    fun setFilter(type: SearchResultType?) {
        _selectedFilter.value = type
        updateFilteredResults()
    }

    /**
     * Update filtered results based on the selected filter
     */
    private fun updateFilteredResults() {
        val filter = _selectedFilter.value
        val results = _searchResults.value

        _filteredResults.value = if (filter == null) {
            results
        } else {
            results.filter { it.type == filter }
        }
    }

    /**
     * Update the counts of results by type
     */
    private fun updateResultCounts(results: List<SearchResult>) {
        val counts = SearchResultType.values().associateWith { type ->
            results.count { it.type == type }
        }.filter { it.value > 0 }

        _resultCounts.value = counts
    }

    /**
     * Clear the search and reset all filters
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _filteredResults.value = emptyList()
        _resultCounts.value = emptyMap()
        _selectedFilter.value = null
    }
}
