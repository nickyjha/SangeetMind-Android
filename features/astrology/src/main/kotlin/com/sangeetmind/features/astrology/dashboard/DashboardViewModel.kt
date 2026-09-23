package com.sangeetmind.features.astrology.dashboard

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.horoscope.HoroscopeRepository
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.features.astrology.panchang.PanchangRepository
import com.sangeetmind.features.astrology.profile.AstroProfileRepository
import com.sangeetmind.libs.models.AstroProfileSummary
import com.sangeetmind.libs.models.DailyHoroscope
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.PanchangResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val primaryKundli: Kundli? = null,
    val hasNoKundlis: Boolean = false,
    val profile: AstroProfileSummary? = null,
    val todayHoroscope: DailyHoroscope? = null,
    val todayPanchang: PanchangResponse? = null,
    val error: String? = null
)

private fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val kundliRepository: KundliRepository,
    private val astroProfileRepository: AstroProfileRepository,
    private val horoscopeRepository: HoroscopeRepository,
    private val panchangRepository: PanchangRepository,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    init {
        refresh()
        // The daily horoscope's Gemini-written fields come back in the requested language,
        // so re-fetch the Today card when the user switches language (skip the initial value).
        viewModelScope.launch {
            languageManager.language.drop(1).collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val kundliResult = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = kundliResult.data.firstOrNull { it.isPrimary }
                        ?: kundliResult.data.firstOrNull()
                    if (primary == null) {
                        _uiState.update {
                            it.copy(isLoading = false, hasNoKundlis = true, primaryKundli = null, profile = null)
                        }
                        return@launch
                    }
                    _uiState.update { it.copy(primaryKundli = primary, hasNoKundlis = false) }
                    when (val profileResult = astroProfileRepository.getProfile(primary)) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(isLoading = false, profile = profileResult.data)
                            }
                            loadTodayHub(profileResult.data.moonSign, primary.latitude, primary.longitude)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = profileResult.message)
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = kundliResult.message ?: str(R.string.dashboard_error_load))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Today's mood + lucky color/mantra/guidance + panchang snapshot — fetched
     * alongside the profile so the dashboard's "Today" card needs no extra tap. */
    private fun loadTodayHub(moonSign: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val horoscopeDeferred = async { horoscopeRepository.getDaily(moonSign) }
            val panchangDeferred = async { panchangRepository.getPanchang(todayIso(), latitude, longitude) }

            val horoscope = (horoscopeDeferred.await() as? Result.Success)?.data
            val panchang = (panchangDeferred.await() as? Result.Success)?.data

            _uiState.update { it.copy(todayHoroscope = horoscope, todayPanchang = panchang) }
        }
    }
}
