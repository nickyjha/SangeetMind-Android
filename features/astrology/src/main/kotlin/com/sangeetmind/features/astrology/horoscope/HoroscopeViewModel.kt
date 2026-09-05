package com.sangeetmind.features.astrology.horoscope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.features.astrology.profile.AstroProfileRepository
import com.sangeetmind.libs.models.DailyHoroscope
import com.sangeetmind.libs.models.PeriodHoroscope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HoroscopeTab { DAILY, WEEKLY, MONTHLY, YEARLY }

data class HoroscopeUiState(
    val isLoading: Boolean = false,
    val selectedTab: HoroscopeTab = HoroscopeTab.DAILY,
    val moonSign: String? = null,
    val daily: DailyHoroscope? = null,
    val weekly: PeriodHoroscope? = null,
    val monthly: PeriodHoroscope? = null,
    val yearly: PeriodHoroscope? = null,
    val error: String? = null
)

/**
 * Uses the primary kundli's derived moon sign against the sign-based horoscope endpoints
 * rather than /v1/daily/user/{userId} — this module has no dependency on features:auth for
 * the Firebase uid, and the sign-based endpoints give the same personalized result.
 */
@HiltViewModel
class HoroscopeViewModel @Inject constructor(
    private val horoscopeRepository: HoroscopeRepository,
    private val kundliRepository: KundliRepository,
    private val astroProfileRepository: AstroProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HoroscopeUiState())
    val uiState: StateFlow<HoroscopeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun selectTab(tab: HoroscopeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val kundliResult = kundliRepository.listKundlis()
            val kundlis = (kundliResult as? Result.Success)?.data
            if (kundlis == null) {
                _uiState.update {
                    it.copy(isLoading = false, error = (kundliResult as? Result.Error)?.message ?: "Failed to load kundlis")
                }
                return@launch
            }
            val primary = kundlis.firstOrNull { it.isPrimary } ?: kundlis.firstOrNull()
            if (primary == null) {
                _uiState.update { it.copy(isLoading = false, error = "Add a kundli first to see your horoscope") }
                return@launch
            }

            val profileResult = astroProfileRepository.getProfile(primary)
            val moonSign = (profileResult as? Result.Success)?.data?.moonSign
            if (moonSign.isNullOrBlank()) {
                _uiState.update {
                    it.copy(isLoading = false, error = (profileResult as? Result.Error)?.message ?: "Could not determine your moon sign")
                }
                return@launch
            }
            _uiState.update { it.copy(moonSign = moonSign) }

            val daily = horoscopeRepository.getDaily(moonSign)
            val weekly = horoscopeRepository.getWeekly(moonSign)
            val monthly = horoscopeRepository.getMonthly(moonSign)
            val yearly = horoscopeRepository.getYearly(moonSign)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    daily = (daily as? Result.Success)?.data,
                    weekly = (weekly as? Result.Success)?.data,
                    monthly = (monthly as? Result.Success)?.data,
                    yearly = (yearly as? Result.Success)?.data,
                    error = (daily as? Result.Error)?.message
                )
            }
        }
    }
}
