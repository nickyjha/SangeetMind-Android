package com.sangeetmind.features.astrology.gochar

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.InterpretationApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.TransitRequest
import com.sangeetmind.libs.models.TransitResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GocharRepository @Inject constructor(
    private val interpretationApi: InterpretationApi,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /** `time` is wall-clock time in the kundli's timezone (the backend reads it that way). */
    suspend fun getTransit(kundli: Kundli, date: LocalDate, time: LocalTime): Result<TransitResponse> =
        withContext(ioDispatcher) {
            try {
                val transit = interpretationApi.getTransit(
                    TransitRequest(
                        date = kundli.birthDate,
                        time = kundli.birthTime,
                        timezone = kundli.timezone,
                        place = kundli.birthPlace,
                        lat = kundli.latitude,
                        lon = kundli.longitude,
                        transitDate = date.toString(),
                        transitTime = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                    )
                )
                Result.Success(transit)
            } catch (e: Exception) {
                Result.Error(
                    e,
                    e.message ?: appContext.withAppLanguage(languageManager.current)
                        .getString(R.string.gochar_error_load)
                )
            }
        }
}
