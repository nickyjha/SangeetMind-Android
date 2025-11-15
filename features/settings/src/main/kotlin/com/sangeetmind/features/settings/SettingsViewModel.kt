package com.sangeetmind.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.libs.models.PlaybackQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val language: String = "en",
    val playbackQuality: PlaybackQuality = PlaybackQuality.HIGH,
    val downloadOnWifiOnly: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val darkMode: DarkModePreference = DarkModePreference.SYSTEM,
    val autoPlayNext: Boolean = true,
    val showLyrics: Boolean = true
)

enum class DarkModePreference {
    LIGHT, DARK, SYSTEM
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Inject DataStore or PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // TODO: Load from DataStore
            // For now, using default values
        }
    }

    fun setLanguage(language: String) {
        _uiState.update { it.copy(language = language) }
        saveSettings()
    }

    fun setPlaybackQuality(quality: PlaybackQuality) {
        _uiState.update { it.copy(playbackQuality = quality) }
        saveSettings()
    }

    fun setDownloadOnWifiOnly(enabled: Boolean) {
        _uiState.update { it.copy(downloadOnWifiOnly = enabled) }
        saveSettings()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        saveSettings()
    }

    fun setDarkMode(preference: DarkModePreference) {
        _uiState.update { it.copy(darkMode = preference) }
        saveSettings()
    }

    fun setAutoPlayNext(enabled: Boolean) {
        _uiState.update { it.copy(autoPlayNext = enabled) }
        saveSettings()
    }

    fun setShowLyrics(enabled: Boolean) {
        _uiState.update { it.copy(showLyrics = enabled) }
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            // TODO: Save to DataStore
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // TODO: Clear audio cache
        }
    }

    fun clearDownloads() {
        viewModelScope.launch {
            // TODO: Clear downloaded files
        }
    }
}

