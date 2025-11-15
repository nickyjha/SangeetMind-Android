package com.sangeetmind.features.meditation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.MeditationSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MeditationUiState(
    val sessions: List<MeditationSession> = emptyList(),
    val currentSession: MeditationSession? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSessionActive: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Int = 0,
    val totalSeconds: Int = 0
)

@HiltViewModel
class MeditationViewModel @Inject constructor(
    private val repository: MeditationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeditationUiState())
    val uiState: StateFlow<MeditationUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            repository.getMeditationSessions().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                sessions = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Unknown error"
                            )
                        }
                    }
                }
            }
        }
    }

    fun startSession(session: MeditationSession) {
        _uiState.update {
            it.copy(
                currentSession = session,
                isSessionActive = true,
                isPaused = false,
                elapsedSeconds = 0,
                totalSeconds = session.durationMinutes * 60
            )
        }
        startTimer()
    }

    fun pauseSession() {
        _uiState.update { it.copy(isPaused = true) }
        timerJob?.cancel()
    }

    fun resumeSession() {
        _uiState.update { it.copy(isPaused = false) }
        startTimer()
    }

    fun stopSession() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isSessionActive = false,
                isPaused = false,
                elapsedSeconds = 0,
                currentSession = null
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.elapsedSeconds < _uiState.value.totalSeconds) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
            // Session completed
            _uiState.update { it.copy(isSessionActive = false, isPaused = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

