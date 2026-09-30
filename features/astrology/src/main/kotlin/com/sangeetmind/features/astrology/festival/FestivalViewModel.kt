package com.sangeetmind.features.astrology.festival

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.FestivalCalendarResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Year
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FestivalUiState(
    val isLoading: Boolean = true,
    // The primary kundli, for its place; null means dates are for Delhi.
    val kundli: Kundli? = null,
    val year: Int = Year.now().value,
    val calendar: FestivalCalendarResponse? = null,
    val error: String? = null
)

@HiltViewModel
class FestivalViewModel @Inject constructor(
    private val festivalRepository: FestivalRepository,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FestivalUiState())
    val uiState: StateFlow<FestivalUiState> = _uiState.asStateFlow()

    private var kundliLoaded = false

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            if (!kundliLoaded) {
                // A kundli is optional here: without one the dates are for Delhi.
                val list = kundliRepository.listKundlis()
                if (list is Result.Success) {
                    val primary = list.data.firstOrNull { it.isPrimary } ?: list.data.firstOrNull()
                    _uiState.update { it.copy(kundli = primary) }
                }
                kundliLoaded = true
            }
            val state = _uiState.value
            when (val result = festivalRepository.getFestivals(state.kundli, state.year)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, calendar = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectYear(year: Int) {
        _uiState.update { it.copy(year = year) }
        refresh()
    }
}
