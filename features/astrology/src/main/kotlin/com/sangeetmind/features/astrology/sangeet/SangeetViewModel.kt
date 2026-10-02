package com.sangeetmind.features.astrology.sangeet

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.libs.models.JapaStats
import com.sangeetmind.libs.models.RaagPlaylist
import com.sangeetmind.libs.models.SoundHealingSession
import com.sangeetmind.libs.models.VoiceHoroscopeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SangeetTab { RAAG, JAPA, VOICE_HOROSCOPE, SOUND_HEALING }

enum class ChantState { IDLE, PLAYING, PAUSED }

data class SangeetUiState(
    val tab: SangeetTab = SangeetTab.RAAG,
    val isLoading: Boolean = false,
    val error: String? = null,
    val dailyRaag: RaagPlaylist? = null,
    val japaCount: Int = 0,
    val japaStats: JapaStats? = null,
    val japaLogged: Boolean = false,
    /** Mantra the counter logs against (a [JapaMantras] id). */
    val selectedMantraId: String = JapaMantras.GENERIC_ID,
    /** Beej mantra for the user's current mahadasha lord (from the daily raag playlist). */
    val recommendedMantraId: String? = null,
    /** The mahadasha lord behind [recommendedMantraId], e.g. "Saturn". */
    val recommendedForLord: String? = null,
    val voiceHoroscope: VoiceHoroscopeResponse? = null,
    val soundHealingSessions: List<SoundHealingSession> = emptyList(),
    val soundHealingPremiumRequired: Boolean = false,
    /** Audio chant-along for the selected mantra (only when it has a recorded track). */
    val chantState: ChantState = ChantState.IDLE,
    val malaCompleted: Int? = null
)

@HiltViewModel
class SangeetViewModel @Inject constructor(
    private val repository: SangeetRepository,
    private val mantraStore: JapaMantraStore,
    languageManager: LanguageManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private var player: MediaPlayer? = null

    private val _uiState = MutableStateFlow(
        SangeetUiState(
            selectedMantraId = mantraStore.lastMantraId
                ?.takeIf { JapaMantras.byId(it) != null } ?: JapaMantras.GENERIC_ID
        )
    )
    val uiState: StateFlow<SangeetUiState> = _uiState.asStateFlow()

    /** Sign of the last generated voice horoscope, so a language switch can re-fetch it. */
    private var voiceHoroscopeSign: String? = null

    init {
        loadDailyRaag()
        viewModelScope.launch {
            // The voice horoscope script is the only Sangeet content fetched in a language;
            // regenerate it when the app language changes (skip the initial emission).
            languageManager.language.drop(1).collect {
                loadDailyRaag()
                voiceHoroscopeSign?.let { sign -> getVoiceHoroscope(sign) }
            }
        }
    }

    fun setTab(tab: SangeetTab) {
        if (tab != SangeetTab.JAPA) stopChant()
        _uiState.update { it.copy(tab = tab, error = null) }
        when (tab) {
            SangeetTab.RAAG -> if (_uiState.value.dailyRaag == null) loadDailyRaag()
            SangeetTab.JAPA -> loadJapaStats()
            SangeetTab.SOUND_HEALING -> loadSoundHealing()
            SangeetTab.VOICE_HOROSCOPE -> Unit
        }
    }

    private fun loadDailyRaag() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getDailyRaag()) {
                is Result.Success -> {
                    val lord = result.data.mahadashaLord
                    val recommended = JapaMantras.forDashaLord(lord)
                    // Pre-select the dasha mantra only until the user has made a choice.
                    val keepChoice = mantraStore.lastMantraId != null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dailyRaag = result.data,
                            recommendedMantraId = recommended?.id,
                            recommendedForLord = lord.takeIf { recommended != null },
                            selectedMantraId = if (!keepChoice && recommended != null) recommended.id else it.selectedMantraId
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectMantra(mantraId: String) {
        if (mantraId != _uiState.value.selectedMantraId) stopChant()
        mantraStore.lastMantraId = mantraId
        _uiState.update { it.copy(selectedMantraId = mantraId, japaLogged = false, malaCompleted = null) }
    }

    fun incrementJapa() {
        _uiState.update { it.copy(japaCount = it.japaCount + 1, japaLogged = false) }
    }

    fun resetJapaCount() {
        stopChant()
        _uiState.update { it.copy(japaCount = 0, japaLogged = false, malaCompleted = null) }
    }

    fun logJapaSession() {
        val count = _uiState.value.japaCount
        val mantraId = _uiState.value.selectedMantraId
        if (count <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.logJapa(mantraId, count)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, japaCount = 0, japaLogged = true) }
                    loadJapaStats()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadJapaStats() {
        viewModelScope.launch {
            when (val result = repository.getJapaStats()) {
                is Result.Success -> _uiState.update { it.copy(japaStats = result.data) }
                is Result.Error -> _uiState.update { it.copy(error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun getVoiceHoroscope(sign: String) {
        voiceHoroscopeSign = sign
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getVoiceHoroscope(sign)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, voiceHoroscope = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadSoundHealing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getSoundHealingSessions()) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        soundHealingSessions = result.data.sessions,
                        soundHealingPremiumRequired = result.data.premiumRequired
                    )
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    /** Starts (or resumes) the recorded chant; each full play adds its chants to the mala. */
    fun playChant() {
        val track = MantraAudio.forMantra(_uiState.value.selectedMantraId) ?: return
        val existing = player
        if (existing != null) {
            existing.start()
            _uiState.update { it.copy(chantState = ChantState.PLAYING) }
            return
        }
        if (_uiState.value.japaCount >= MantraAudio.MALA) {
            _uiState.update { it.copy(japaCount = 0) }
        }
        val mp = MediaPlayer.create(appContext, track.rawRes) ?: return
        mp.setOnCompletionListener { finished ->
            val count = MantraAudio.afterPlay(_uiState.value.japaCount, track)
            _uiState.update { it.copy(japaCount = count, japaLogged = false) }
            if (count >= MantraAudio.MALA) {
                stopChant()
                _uiState.update { it.copy(malaCompleted = count) }
                logJapaSession()
            } else {
                finished.seekTo(0)
                finished.start()
            }
        }
        player = mp
        mp.start()
        _uiState.update { it.copy(chantState = ChantState.PLAYING, malaCompleted = null, japaLogged = false) }
    }

    fun pauseChant() {
        player?.takeIf { it.isPlaying }?.pause()
        if (player != null) _uiState.update { it.copy(chantState = ChantState.PAUSED) }
    }

    fun stopChant() {
        player?.run {
            setOnCompletionListener(null)
            runCatching { stop() }
            release()
        }
        player = null
        if (_uiState.value.chantState != ChantState.IDLE) {
            _uiState.update { it.copy(chantState = ChantState.IDLE) }
        }
    }

    override fun onCleared() {
        stopChant()
        super.onCleared()
    }
}
