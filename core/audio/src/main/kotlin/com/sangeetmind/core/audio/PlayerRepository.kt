package com.sangeetmind.core.audio

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sangeetmind.libs.models.Raag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing audio playback
 */
@Singleton
class PlayerRepository @Inject constructor(
    private val player: ExoPlayer
) {
    private val _currentRaag = MutableStateFlow<Raag?>(null)
    val currentRaag: StateFlow<Raag?> = _currentRaag.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun playRaag(raag: Raag) {
        _currentRaag.value = raag
        val mediaItem = MediaItem.fromUri(raag.audioUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        _currentRaag.value = null
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun skipToNext() {
        player.seekToNext()
    }

    fun skipToPrevious() {
        player.seekToPrevious()
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun getCurrentPosition(): Long = player.currentPosition

    fun getDuration(): Long = player.duration

    fun release() {
        player.release()
    }
}

