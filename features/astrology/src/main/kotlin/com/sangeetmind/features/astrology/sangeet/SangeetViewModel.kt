package com.sangeetmind.features.astrology.sangeet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.libs.models.JapaStats
import com.sangeetmind.libs.models.RaagPlaylist
import com.sangeetmind.libs.models.SoundHealingSession
import com.sangeetmind.libs.models.VoiceHoroscopeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SangeetTab { RAAG, JAPA, VOICE_HOROSCOPE, SOUND_HEALING }

data class SangeetUiState(
    val tab: SangeetTab = SangeetTab.RAAG,
    val isLoading: Boolean = false,
    val error: String? = null,
    val dailyRaag: RaagPlaylist? = null,
    val japaCount: Int = 0,
    val japaStats: JapaStats? = null,
    val japaLogged: Boolean = false,
    val voiceHoroscope: VoiceHoroscopeResponse? = null,
    val soundHealingSessions: List<SoundHealingSession> = emptyList(),
    val soundHealingPremiumRequired: Boolean = false
)

@HiltViewModel
class SangeetViewModel @Inject constructor(
    private val repository: SangeetRepository,
    languageManager: LanguageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SangeetUiState())
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
                is Result.Success -> _uiState.update { it.copy(isLoading = false, dailyRaag = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun incrementJapa() {
        _uiState.update { it.copy(japaCount = it.japaCount + 1, japaLogged = false) }
    }

    fun resetJapaCount() {
        _uiState.update { it.copy(japaCount = 0, japaLogged = false) }
    }

    fun logJapaSession() {
        val count = _uiState.value.japaCount
        if (count <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.logJapa("generic", count)) {
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
}
