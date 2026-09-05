package com.sangeetmind.features.astrology.muhurat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.MuhuratSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val MUHURAT_INTENTS = listOf("general", "marriage", "business", "travel", "griha pravesh")

data class MuhuratUiState(
    val intent: String = "general",
    val windowStart: String = "",
    val windowEnd: String = "",
    val isSearching: Boolean = false,
    val results: List<MuhuratSlot> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MuhuratViewModel @Inject constructor(
    private val muhuratRepository: MuhuratRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MuhuratUiState())
    val uiState: StateFlow<MuhuratUiState> = _uiState.asStateFlow()

    fun onIntentChange(intent: String) {
        _uiState.update { it.copy(intent = intent, error = null) }
    }

    fun onWindowStartChange(value: String) {
        _uiState.update { it.copy(windowStart = value, error = null) }
    }

    fun onWindowEndChange(value: String) {
        _uiState.update { it.copy(windowEnd = value, error = null) }
    }

    fun findMuhurat() {
        val state = _uiState.value
        if (state.windowStart.isBlank() || state.windowEnd.isBlank()) {
            _uiState.update { it.copy(error = "Please enter both start and end dates") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            when (val result = muhuratRepository.findMuhurat(state.intent, state.windowStart, state.windowEnd)) {
                is Result.Success -> _uiState.update {
                    it.copy(isSearching = false, results = result.data.results)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSearching = false, error = result.message ?: "Failed to find muhurat")
                }
                is Result.Loading -> Unit
            }
        }
    }
}
