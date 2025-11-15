package com.sangeetmind.features.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.audio.PlayerRepository
import com.sangeetmind.libs.models.Raag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentRaag: Raag? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Raag> = emptyList()
)

enum class RepeatMode {
    OFF, ONE, ALL
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            combine(
                playerRepository.currentRaag,
                playerRepository.isPlaying,
                playerRepository.playbackPosition
            ) { raag, isPlaying, position ->
                _uiState.update {
                    it.copy(
                        currentRaag = raag,
                        isPlaying = isPlaying,
                        currentPosition = position,
                        duration = raag?.durationSeconds?.times(1000L) ?: 0L
                    )
                }
            }.collect()
        }
    }

    fun playPause() {
        if (_uiState.value.isPlaying) {
            playerRepository.pause()
        } else {
            playerRepository.play()
        }
    }

    fun playRaag(raag: Raag) {
        playerRepository.playRaag(raag)
    }

    fun seekTo(positionMs: Long) {
        playerRepository.seekTo(positionMs)
    }

    fun skipToNext() {
        val currentQueue = _uiState.value.queue
        val currentRaag = _uiState.value.currentRaag
        
        if (currentQueue.isNotEmpty() && currentRaag != null) {
            val currentIndex = currentQueue.indexOf(currentRaag)
            val nextIndex = when {
                _uiState.value.isShuffleEnabled -> currentQueue.indices.random()
                currentIndex < currentQueue.size - 1 -> currentIndex + 1
                _uiState.value.repeatMode == RepeatMode.ALL -> 0
                else -> return
            }
            playerRepository.playRaag(currentQueue[nextIndex])
        } else {
            playerRepository.skipToNext()
        }
    }

    fun skipToPrevious() {
        val currentQueue = _uiState.value.queue
        val currentRaag = _uiState.value.currentRaag
        
        if (currentQueue.isNotEmpty() && currentRaag != null) {
            val currentIndex = currentQueue.indexOf(currentRaag)
            val previousIndex = when {
                currentIndex > 0 -> currentIndex - 1
                _uiState.value.repeatMode == RepeatMode.ALL -> currentQueue.size - 1
                else -> return
            }
            playerRepository.playRaag(currentQueue[previousIndex])
        } else {
            playerRepository.skipToPrevious()
        }
    }

    fun toggleShuffle() {
        _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

    fun toggleRepeatMode() {
        val newMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }
        _uiState.update { it.copy(repeatMode = newMode) }
    }

    fun setQueue(raags: List<Raag>) {
        _uiState.update { it.copy(queue = raags) }
    }

    fun removeFromQueue(raag: Raag) {
        _uiState.update { it.copy(queue = it.queue.filter { r -> r.id != raag.id }) }
    }

    fun clearQueue() {
        _uiState.update { it.copy(queue = emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        // Don't release player here - it should persist across ViewModels
    }
}

