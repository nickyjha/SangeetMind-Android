package com.sangeetmind.features.astrology.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.KundliMatchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchUiState(
    val isLoadingKundlis: Boolean = false,
    val kundlis: List<Kundli> = emptyList(),
    val personA: Kundli? = null,
    val personB: Kundli? = null,
    val isMatching: Boolean = false,
    val result: KundliMatchResult? = null,
    val error: String? = null
)

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val kundliRepository: KundliRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    init {
        loadKundlis()
    }

    fun loadKundlis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingKundlis = true, error = null) }
            when (val result = kundliRepository.listKundlis()) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingKundlis = false, kundlis = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingKundlis = false, error = result.message ?: "Failed to load kundlis")
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectPersonA(kundli: Kundli) {
        _uiState.update { it.copy(personA = kundli, result = null, error = null) }
    }

    fun selectPersonB(kundli: Kundli) {
        _uiState.update { it.copy(personB = kundli, result = null, error = null) }
    }

    fun checkCompatibility() {
        val state = _uiState.value
        val a = state.personA
        val b = state.personB
        if (a == null || b == null) {
            _uiState.update { it.copy(error = "Please pick both people") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMatching = true, error = null) }
            when (val result = matchRepository.matchKundlis(a, b)) {
                is Result.Success -> _uiState.update {
                    it.copy(isMatching = false, result = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isMatching = false, error = result.message ?: "Failed to compute compatibility")
                }
                is Result.Loading -> Unit
            }
        }
    }
}
