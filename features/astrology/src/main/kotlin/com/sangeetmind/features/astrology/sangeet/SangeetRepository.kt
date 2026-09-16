package com.sangeetmind.features.astrology.sangeet

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.SangeetApi
import com.sangeetmind.libs.models.JapaLogRequest
import com.sangeetmind.libs.models.JapaStats
import com.sangeetmind.libs.models.RaagPlaylist
import com.sangeetmind.libs.models.SoundHealingResponse
import com.sangeetmind.libs.models.VoiceHoroscopeResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SangeetRepository @Inject constructor(
    private val sangeetApi: SangeetApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getDailyRaag(locale: String = "hi"): Result<RaagPlaylist> = withContext(ioDispatcher) {
        try {
            Result.Success(sangeetApi.getDailyRaag(locale).playlist)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not load today's raag")
        }
    }

    suspend fun logJapa(mantraId: String, japaCount: Int): Result<Unit> = withContext(ioDispatcher) {
        try {
            sangeetApi.logJapa(JapaLogRequest(mantraId, japaCount))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not log japa")
        }
    }

    suspend fun getJapaStats(): Result<JapaStats> = withContext(ioDispatcher) {
        try {
            Result.Success(sangeetApi.getJapaStats().stats)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not load japa stats")
        }
    }

    suspend fun getVoiceHoroscope(sign: String, lang: String = "hi"): Result<VoiceHoroscopeResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(sangeetApi.getVoiceHoroscope(sign, lang))
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Voice horoscope needs Premium")
            }
        }

    suspend fun getSoundHealingSessions(): Result<SoundHealingResponse> = withContext(ioDispatcher) {
        try {
            Result.Success(sangeetApi.getSoundHealingSessions())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not load sound healing library")
        }
    }
}
