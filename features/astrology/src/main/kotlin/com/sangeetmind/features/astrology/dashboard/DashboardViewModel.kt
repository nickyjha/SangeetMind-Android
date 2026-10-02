package com.sangeetmind.features.astrology.dashboard

import java.time.LocalDate
import com.sangeetmind.core.network.PanchangApi
import com.sangeetmind.core.network.PushTokenRegistrar
import com.sangeetmind.core.network.SangeetMindMessagingService
import com.sangeetmind.libs.models.CareerBirthDetails
import com.sangeetmind.libs.models.DailyScoresRequest
import com.sangeetmind.libs.models.DailyScoresResponse
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

/** What the dashboard shows about notifications after the first successful load. */
enum class NotificationNudge {
    /** Nothing to show: granted, dismissed, or pre-Android-13. */
    NONE,

    /** Denied once; the card's button re-opens the system dialog. */
    ASK_AGAIN,

    /** Denied for good ("don't ask again" / twice); the card's button opens app settings. */
    OPEN_SETTINGS
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val primaryKundli: Kundli? = null,
    val hasNoKundlis: Boolean = false,
    val profile: AstroProfileSummary? = null,
    val todayHoroscope: DailyHoroscope? = null,
    val todayPanchang: PanchangResponse? = null,
    val dailyScores: DailyScoresResponse? = null,
    val actionDoneToday: Boolean = false,
    val actionStreak: Int = 0,
    val notificationNudge: NotificationNudge = NotificationNudge.NONE,
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
    private val panchangApi: PanchangApi,
    private val actionStreakStore: ActionStreakStore,
    private val languageManager: LanguageManager,
    private val pushTokenRegistrar: PushTokenRegistrar,
    private val notificationPromptStore: NotificationPromptStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /** The device is registered once per app session (the token rarely changes). */
    private var pushDeviceRegistered = false

    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    init {
        refresh()
        // The daily horoscope's Gemini-written fields come back in the requested language,
        // so re-fetch the Today card when the user switches language (skip the initial value).
        viewModelScope.launch {
            languageManager.language.drop(1).collect {
                refresh()
                // The server picks en/hi for the push from the registered locale.
                pushDeviceRegistered = false
            }
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
                            loadDailyScores(primary)
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

    /** Life-area scores for yesterday/today/tomorrow and the week (rule-based, free). */
    private fun loadDailyScores(kundli: Kundli) {
        viewModelScope.launch {
            val scores = runCatching {
                panchangApi.getDailyScores(
                    DailyScoresRequest(
                        birthDetails = CareerBirthDetails(
                            date = kundli.birthDate,
                            time = kundli.birthTime,
                            timezone = kundli.timezone,
                            place = kundli.birthPlace,
                            lat = kundli.latitude,
                            lon = kundli.longitude
                        ),
                        tz = kundli.timezone.ifBlank { null }
                    )
                )
            }.getOrNull()
            _uiState.update { it.copy(dailyScores = scores) }
            refreshActionState()
        }
    }

    private fun refreshActionState() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                actionDoneToday = actionStreakStore.isDone(today),
                actionStreak = actionStreakStore.streak(today)
            )
        }
    }

    /** "I'll do this today" on the Action of the day. */
    fun markActionDone() {
        actionStreakStore.markDone(LocalDate.now())
        refreshActionState()
    }

    // --- Daily push: permission nudge + device registration ---------------------------

    /** True exactly once per device: the system dialog has not been shown from here yet. */
    fun shouldShowSystemNotificationDialog(): Boolean = !notificationPromptStore.systemDialogShown

    fun onSystemNotificationDialogShown() {
        notificationPromptStore.systemDialogShown = true
    }

    /**
     * Notifications are allowed (granted now, or pre-Android-13): make sure the channels
     * exist and the token is registered with the timezone/hour the daily push needs.
     */
    fun onNotificationsAllowed() {
        _uiState.update { it.copy(notificationNudge = NotificationNudge.NONE) }
        if (pushDeviceRegistered) return
        pushDeviceRegistered = true
        SangeetMindMessagingService.ensureChannels(context)
        viewModelScope.launch {
            if (pushTokenRegistrar.registerCurrentToken().isFailure) pushDeviceRegistered = false
        }
    }

    /**
     * Permission is not granted. [canAskAgain] is the system's
     * shouldShowRequestPermissionRationale: true after a single denial, false once the
     * user has denied for good (then only Settings can turn it on).
     */
    fun onNotificationsDenied(canAskAgain: Boolean) {
        if (notificationPromptStore.rationaleDismissed) {
            _uiState.update { it.copy(notificationNudge = NotificationNudge.NONE) }
            return
        }
        val nudge = if (canAskAgain) NotificationNudge.ASK_AGAIN else NotificationNudge.OPEN_SETTINGS
        _uiState.update { it.copy(notificationNudge = nudge) }
    }

    /** "Not now" on the rationale card: never show it again on this device. */
    fun dismissNotificationNudge() {
        notificationPromptStore.rationaleDismissed = true
        _uiState.update { it.copy(notificationNudge = NotificationNudge.NONE) }
    }
}
