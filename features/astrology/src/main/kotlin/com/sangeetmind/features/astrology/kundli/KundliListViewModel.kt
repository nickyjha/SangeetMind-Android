package com.sangeetmind.features.astrology.kundli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KundliListUiState(
    val isLoading: Boolean = false,
    val kundlis: List<Kundli> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class KundliListViewModel @Inject constructor(
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KundliListUiState())
    val uiState: StateFlow<KundliListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = kundliRepository.listKundlis()) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, kundlis = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: "Failed to load kundlis")
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun setPrimary(id: String) {
        viewModelScope.launch {
            when (val result = kundliRepository.setPrimary(id)) {
                is Result.Success -> refresh()
                is Result.Error -> _uiState.update {
                    it.copy(error = result.message ?: "Failed to switch kundli")
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            when (val result = kundliRepository.delete(id)) {
                is Result.Success -> refresh()
                is Result.Error -> _uiState.update {
                    it.copy(error = result.message ?: "Failed to delete kundli")
                }
                is Result.Loading -> Unit
            }
        }
    }
}
