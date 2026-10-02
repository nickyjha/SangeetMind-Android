package com.sangeetmind.features.astrology.muhurat

import android.content.Context
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.friendlyErrorMessage
import com.sangeetmind.features.astrology.R
import dagger.hilt.android.qualifiers.ApplicationContext
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.MuhuratApi
import com.sangeetmind.libs.models.MuhuratRequest
import com.sangeetmind.libs.models.CareerBirthDetails
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.MuhuratResponse
import com.sangeetmind.libs.models.VivahRequest
import com.sangeetmind.libs.models.VivahResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MuhuratRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    private val muhuratApi: MuhuratApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun localized(): Context = appContext.withAppLanguage(languageManager.current)

    suspend fun findMuhurat(
        intent: String,
        windowStart: String,
        windowEnd: String,
        lat: Double = 28.6139,
        lon: Double = 77.209
    ): Result<MuhuratResponse> = withContext(ioDispatcher) {
        try {
            val response = muhuratApi.findMuhurat(
                MuhuratRequest(intent = intent, windowStart = windowStart, windowEnd = windowEnd, lat = lat, lon = lon)
            )
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e, friendlyErrorMessage(e, localized(), localized().getString(R.string.muhurat_error_find_failed)))
        }
    }

    /** Marriage windows at [place]'s location and time zone; [couple] (0-2 kundlis) adds
     * Chandra bala and Tara bala for them. */
    suspend fun findVivah(
        windowStart: String,
        windowEnd: String,
        place: Kundli?,
        couple: List<Kundli>
    ): Result<VivahResponse> = withContext(ioDispatcher) {
        try {
            val response = muhuratApi.findVivah(
                VivahRequest(
                    windowStart = windowStart,
                    windowEnd = windowEnd,
                    lat = place?.latitude ?: 28.6139,
                    lon = place?.longitude ?: 77.209,
                    tz = place?.timezone?.ifBlank { null } ?: "Asia/Kolkata",
                    people = couple.take(2).map {
                        CareerBirthDetails(
                            date = it.birthDate,
                            time = it.birthTime,
                            timezone = it.timezone,
                            place = it.birthPlace,
                            lat = it.latitude,
                            lon = it.longitude
                        )
                    }.ifEmpty { null }
                )
            )
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e, friendlyErrorMessage(e, localized(), localized().getString(R.string.muhurat_error_find_failed)))
        }
    }
}
