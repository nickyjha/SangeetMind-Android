package com.sangeetmind.features.astrology.eclipse

import com.sangeetmind.core.network.friendlyErrorMessage
import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.PanchangApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.EclipseCalendarResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EclipseRepository @Inject constructor(
    private val panchangApi: PanchangApi,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /** Eclipses for [year] as seen from the kundli's place, in its time zone. */
    suspend fun getEclipses(kundli: Kundli, year: Int): Result<EclipseCalendarResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(
                    panchangApi.getEclipses(
                        year = year,
                        lat = kundli.latitude,
                        lon = kundli.longitude,
                        tz = kundli.timezone.ifBlank { "Asia/Kolkata" }
                    )
                )
            } catch (e: Exception) {
                Result.Error(
                    e,
                    friendlyErrorMessage(
                        e,
                        appContext.withAppLanguage(languageManager.current),
                        appContext.withAppLanguage(languageManager.current).getString(R.string.eclipse_error_load)
                    )
                )
            }
        }
}
