package com.sangeetmind.features.astrology.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.CareerReadingResponse
import com.sangeetmind.libs.models.StrengthsReadingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReadingTab { CAREER, STRENGTHS }

data class ReadingsUiState(
    val tab: ReadingTab = ReadingTab.CAREER,
    val isLoading: Boolean = false,
    val career: CareerReadingResponse? = null,
    val strengths: StrengthsReadingResponse? = null,
    val error: String? = null
)

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    private val repository: ReadingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingsUiState())
    val uiState: StateFlow<ReadingsUiState> = _uiState.asStateFlow()

    fun setTab(tab: ReadingTab) {
        _uiState.update { it.copy(tab = tab, error = null) }
    }

    fun generateCareerReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getCareerReading()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, career = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun generateStrengthsReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getStrengthsReading()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, strengths = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }
}
