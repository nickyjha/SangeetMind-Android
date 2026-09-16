package com.sangeetmind.features.astrology.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChartUiState(
    val isLoading: Boolean = true,
    val hasNoKundli: Boolean = false,
    val kundli: Kundli? = null,
    val chart: ChartSummaryResponse? = null,
    val showFullTimeline: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val chartRepository: ChartRepository,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null, hasNoKundli = false, chart = null)
            }
            when (val listResult = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = listResult.data.firstOrNull { it.isPrimary }
                        ?: listResult.data.firstOrNull()
                    if (primary == null) {
                        _uiState.update {
                            it.copy(isLoading = false, hasNoKundli = true, kundli = null)
                        }
                        return@launch
                    }
                    _uiState.update { it.copy(kundli = primary) }
                    when (val chartResult = chartRepository.getChart(primary)) {
                        is Result.Success -> _uiState.update {
                            it.copy(isLoading = false, chart = chartResult.data)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = chartResult.message)
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = listResult.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun toggleFullTimeline() {
        _uiState.update { it.copy(showFullTimeline = !it.showFullTimeline) }
    }
}
