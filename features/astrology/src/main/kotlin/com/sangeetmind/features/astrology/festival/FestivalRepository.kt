package com.sangeetmind.features.astrology.festival

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.PanchangApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.FestivalCalendarResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FestivalRepository @Inject constructor(
    private val panchangApi: PanchangApi,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /** Festivals for [year] at the kundli's place, or Delhi when there is no kundli. */
    suspend fun getFestivals(kundli: Kundli?, year: Int): Result<FestivalCalendarResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(
                    panchangApi.getFestivals(
                        year = year,
                        lat = kundli?.latitude ?: DELHI_LAT,
                        lon = kundli?.longitude ?: DELHI_LON,
                        tz = kundli?.timezone?.ifBlank { null } ?: "Asia/Kolkata"
                    )
                )
            } catch (e: Exception) {
                Result.Error(
                    e,
                    e.message ?: appContext.withAppLanguage(languageManager.current)
                        .getString(R.string.festival_error_load)
                )
            }
        }

    private companion object {
        const val DELHI_LAT = 28.6139
        const val DELHI_LON = 77.209
    }
}
