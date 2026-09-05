package com.sangeetmind.features.astrology.numerology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.NumerologyResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NumerologyUiState(
    val fullName: String = "",
    val dateOfBirth: String = "",
    val isLoading: Boolean = false,
    val result: NumerologyResponse? = null,
    val error: String? = null
)

@HiltViewModel
class NumerologyViewModel @Inject constructor(
    private val numerologyRepository: NumerologyRepository,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumerologyUiState())
    val uiState: StateFlow<NumerologyUiState> = _uiState.asStateFlow()

    init {
        prefillFromPrimaryKundli()
    }

    private fun prefillFromPrimaryKundli() {
        viewModelScope.launch {
            val result = kundliRepository.listKundlis()
            if (result is Result.Success) {
                val primary = result.data.firstOrNull { it.isPrimary } ?: result.data.firstOrNull()
                if (primary != null) {
                    _uiState.update {
                        it.copy(
                            fullName = primary.fullName ?: it.fullName,
                            dateOfBirth = primary.birthDate
                        )
                    }
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, error = null) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value, error = null) }
    }

    fun analyze() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your name") }
            return
        }
        if (state.dateOfBirth.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your date of birth") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = numerologyRepository.analyze(state.fullName, state.dateOfBirth)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, result = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: "Failed to calculate numerology")
                }
                is Result.Loading -> Unit
            }
        }
    }
}
