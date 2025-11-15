package com.sangeetmind.features.raaglibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.Raag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RaagListUiState(
    val raags: List<Raag> = emptyList(),
    val filteredRaags: List<Raag> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class RaagListViewModel @Inject constructor(
    private val repository: RaagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RaagListUiState())
    val uiState: StateFlow<RaagListUiState> = _uiState.asStateFlow()

    init {
        loadRaags()
    }

    fun loadRaags() {
        viewModelScope.launch {
            repository.getRaags().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                raags = result.data,
                                filteredRaags = filterRaags(result.data, it.searchQuery),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Unknown error occurred"
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredRaags = filterRaags(it.raags, query)
            )
        }
    }

    fun clearSearch() {
        onSearchQueryChange("")
    }

    private fun filterRaags(raags: List<Raag>, query: String): List<Raag> {
        if (query.isBlank()) return raags
        
        return raags.filter { raag ->
            raag.name.contains(query, ignoreCase = true) ||
            raag.nameHindi?.contains(query, ignoreCase = true) == true ||
            raag.description.contains(query, ignoreCase = true) ||
            raag.tags.any { it.contains(query, ignoreCase = true) }
        }
    }

    fun toggleFavorite(raagId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(raagId, isFavorite)
            // Update local state
            _uiState.update { state ->
                val updatedRaags = state.raags.map { raag ->
                    if (raag.id == raagId) raag.copy(isFavorite = isFavorite) else raag
                }
                state.copy(
                    raags = updatedRaags,
                    filteredRaags = filterRaags(updatedRaags, state.searchQuery)
                )
            }
        }
    }
}

