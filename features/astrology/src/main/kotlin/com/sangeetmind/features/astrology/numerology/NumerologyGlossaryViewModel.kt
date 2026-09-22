package com.sangeetmind.features.astrology.numerology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.NumerologyNumberInterpretation
import com.sangeetmind.libs.models.NumerologyNumberSummary
import com.sangeetmind.libs.models.NumerologySystemInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NumerologyGlossaryUiState(
    val isLoading: Boolean = true,
    val systems: List<NumerologySystemInfo> = emptyList(),
    val numbers: List<NumerologyNumberSummary> = emptyList(),
    val selectedNumber: Int? = null,
    val isLoadingDetail: Boolean = false,
    val detail: NumerologyNumberInterpretation? = null,
    val error: String? = null
)

/** Backs the read-only numerology reference/glossary screen — system overviews (GET
 * /numerology/systems) and per-number meanings (GET /numerology/numbers, GET
 * /numerology/number/{n}), all previously fully built on the backend but unused by
 * Android. No calculation/form state here, unlike [NumerologyViewModel] — this screen
 * takes no user input beyond "which number did they tap." */
@HiltViewModel
class NumerologyGlossaryViewModel @Inject constructor(
    private val repository: NumerologyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumerologyGlossaryUiState())
    val uiState: StateFlow<NumerologyGlossaryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val systemsResult = repository.getSystems()
            val numbersResult = repository.getNumbers()
            val systems = (systemsResult as? Result.Success)?.data?.systems.orEmpty()
            val numbers = (numbersResult as? Result.Success)?.data?.numbers.orEmpty()
            val error = (systemsResult as? Result.Error)?.message
                ?: (numbersResult as? Result.Error)?.message
            _uiState.update {
                it.copy(isLoading = false, systems = systems, numbers = numbers, error = error)
            }
        }
    }

    fun selectNumber(number: Int) {
        if (_uiState.value.selectedNumber == number) {
            _uiState.update { it.copy(selectedNumber = null, detail = null) }
            return
        }
        _uiState.update { it.copy(selectedNumber = number, isLoadingDetail = true, detail = null) }
        viewModelScope.launch {
            when (val result = repository.getNumberInterpretation(number)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingDetail = false, detail = result.data.interpretation)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingDetail = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }
}
