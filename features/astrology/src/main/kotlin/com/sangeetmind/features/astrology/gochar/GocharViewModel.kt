package com.sangeetmind.features.astrology.gochar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.TransitResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GocharUiState(
    val isLoading: Boolean = true,
    val hasNoKundli: Boolean = false,
    val kundli: Kundli? = null,
    val date: LocalDate = LocalDate.now(),
    val transit: TransitResponse? = null,
    val error: String? = null
) {
    val isToday: Boolean get() = date == LocalDate.now()
}

@HiltViewModel
class GocharViewModel @Inject constructor(
    private val gocharRepository: GocharRepository,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GocharUiState())
    val uiState: StateFlow<GocharUiState> = _uiState.asStateFlow()

    private var loadedKundli: Kundli? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasNoKundli = false) }
            val kundli = loadedKundli ?: when (val listResult = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = listResult.data.firstOrNull { it.isPrimary }
                        ?: listResult.data.firstOrNull()
                    if (primary == null) {
                        _uiState.update { it.copy(isLoading = false, hasNoKundli = true, kundli = null) }
                        return@launch
                    }
                    loadedKundli = primary
                    primary
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = listResult.message) }
                    return@launch
                }
                is Result.Loading -> return@launch
            }
            _uiState.update { it.copy(kundli = kundli) }
            val state = _uiState.value
            // Today shows the sky right now (the Moon moves ~13° a day); other dates use noon.
            val time = if (state.isToday) {
                val zone = runCatching { ZoneId.of(kundli.timezone) }.getOrDefault(ZoneId.systemDefault())
                LocalTime.now(zone)
            } else {
                LocalTime.NOON
            }
            when (val result = gocharRepository.getTransit(kundli, state.date, time)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, transit = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectDate(date: LocalDate) {
        if (date == _uiState.value.date) return
        _uiState.update { it.copy(date = date) }
        refresh()
    }
}
