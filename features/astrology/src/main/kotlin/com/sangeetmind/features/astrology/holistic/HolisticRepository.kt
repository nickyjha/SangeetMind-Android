package com.sangeetmind.features.astrology.holistic

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.HolisticApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.HolisticCombinedRequest
import com.sangeetmind.libs.models.HolisticCombinedResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolisticRepository @Inject constructor(
    private val holisticApi: HolisticApi,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getCombined(
        kundli: Kundli,
        fullName: String,
        language: String,
        enhanceWithLlm: Boolean = true
    ): Result<HolisticCombinedResponse> = withContext(ioDispatcher) {
        try {
            val response = holisticApi.getCombined(
                HolisticCombinedRequest(
                    fullName = fullName,
                    dateOfBirth = kundli.birthDate,
                    timeOfBirth = kundli.birthTime,
                    place = kundli.birthPlace,
                    latitude = kundli.latitude,
                    longitude = kundli.longitude,
                    timezone = kundli.timezone,
                    enhanceWithLlm = enhanceWithLlm,
                    language = language
                )
            )
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(
                e,
                e.message ?: appContext.withAppLanguage(languageManager.current)
                    .getString(R.string.holistic_error_load)
            )
        }
    }
}
