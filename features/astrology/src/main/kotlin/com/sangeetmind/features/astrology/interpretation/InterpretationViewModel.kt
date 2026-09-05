package com.sangeetmind.features.astrology.interpretation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterpretationUiState(
    val isLoading: Boolean = false,
    val data: InterpretationData? = null,
    val hasNoKundli: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InterpretationViewModel @Inject constructor(
    private val kundliRepository: KundliRepository,
    private val interpretationRepository: InterpretationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterpretationUiState())
    val uiState: StateFlow<InterpretationUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val kundliResult = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = kundliResult.data.firstOrNull { it.isPrimary }
                        ?: kundliResult.data.firstOrNull()
                    if (primary == null) {
                        _uiState.update { it.copy(isLoading = false, hasNoKundli = true) }
                        return@launch
                    }
                    when (val result = interpretationRepository.getInterpretation(primary)) {
                        is Result.Success -> _uiState.update {
                            it.copy(isLoading = false, data = result.data, hasNoKundli = false)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = kundliResult.message ?: "Failed to load")
                }
                is Result.Loading -> Unit
            }
        }
    }
}
