package com.sangeetmind.features.astrology.eclipse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.features.astrology.profile.AstroProfileRepository
import com.sangeetmind.libs.models.EclipseCalendarResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Year
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EclipseUiState(
    val isLoading: Boolean = true,
    val hasNoKundli: Boolean = false,
    val kundli: Kundli? = null,
    // The primary kundli's Moon sign, to show which eclipses favour it; null if unavailable.
    val moonSign: String? = null,
    val year: Int = Year.now().value,
    val calendar: EclipseCalendarResponse? = null,
    val error: String? = null
)

@HiltViewModel
class EclipseViewModel @Inject constructor(
    private val eclipseRepository: EclipseRepository,
    private val kundliRepository: KundliRepository,
    private val astroProfileRepository: AstroProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EclipseUiState())
    val uiState: StateFlow<EclipseUiState> = _uiState.asStateFlow()

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
                        _uiState.update { it.copy(isLoading = false, hasNoKundli = true) }
                        return@launch
                    }
                    loadedKundli = primary
                    // The Moon sign is a nice-to-have: the calendar still loads without it.
                    val profile = astroProfileRepository.getProfile(primary)
                    if (profile is Result.Success) {
                        _uiState.update { it.copy(moonSign = profile.data.moonSign) }
                    }
                    primary
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = listResult.message) }
                    return@launch
                }
                is Result.Loading -> return@launch
            }
            _uiState.update { it.copy(kundli = kundli) }
            when (val result = eclipseRepository.getEclipses(kundli, _uiState.value.year)) {
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
