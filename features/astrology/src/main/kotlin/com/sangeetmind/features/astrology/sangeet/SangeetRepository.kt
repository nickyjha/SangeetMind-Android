package com.sangeetmind.features.astrology.sangeet

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.network.SangeetApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.JapaLogRequest
import com.sangeetmind.libs.models.JapaStats
import com.sangeetmind.libs.models.RaagPlaylist
import com.sangeetmind.libs.models.SoundHealingResponse
import com.sangeetmind.libs.models.VoiceHoroscopeResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SangeetRepository @Inject constructor(
    private val sangeetApi: SangeetApi,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /** The playlist's descriptive text follows the app language (`locale` on the backend). */
    suspend fun getDailyRaag(): Result<RaagPlaylist> = withContext(ioDispatcher) {
        try {
            Result.Success(sangeetApi.getDailyRaag(languageManager.current.code).playlist)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.sangeet_err_raag))
        }
    }

    suspend fun logJapa(mantraId: String, japaCount: Int): Result<Unit> = withContext(ioDispatcher) {
        try {
            sangeetApi.logJapa(JapaLogRequest(mantraId, japaCount))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.sangeet_err_japa_log))
        }
    }

    suspend fun getJapaStats(): Result<JapaStats> = withContext(ioDispatcher) {
        try {
            Result.Success(sangeetApi.getJapaStats().stats)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.sangeet_err_japa_stats))
        }
    }

    /** The script comes back in the app's current display language. */
    suspend fun getVoiceHoroscope(sign: String): Result<VoiceHoroscopeResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(sangeetApi.getVoiceHoroscope(sign, lang = languageManager.current.code))
            } catch (e: Exception) {
                Result.Error(e, e.message ?: context.getString(R.string.sangeet_err_voice))
            }
        }

    suspend fun getSoundHealingSessions(): Result<SoundHealingResponse> = withContext(ioDispatcher) {
        try {
            Result.Success(sangeetApi.getSoundHealingSessions())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.sangeet_err_sound))
        }
    }
}
