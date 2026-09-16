package com.sangeetmind.features.astrology.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.Astrologer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceListUiState(
    val isLoading: Boolean = false,
    val astrologers: List<Astrologer> = emptyList(),
    val onlineOnly: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MarketplaceListViewModel @Inject constructor(
    private val repository: MarketplaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceListUiState())
    val uiState: StateFlow<MarketplaceListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun setOnlineOnly(onlineOnly: Boolean) {
        _uiState.update { it.copy(onlineOnly = onlineOnly) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.listAstrologers(_uiState.value.onlineOnly)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, astrologers = result.data) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: "Failed to load astrologers")
                }
                is Result.Loading -> Unit
            }
        }
    }
}
