package com.sangeetmind.features.astrology.profile

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.AstrologyApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.AstroProfileRequest
import com.sangeetmind.libs.models.AstroProfileSummary
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AstroProfileRepository @Inject constructor(
    private val astrologyApi: AstrologyApi,
    private val languageManager: LanguageManager,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getProfile(kundli: Kundli): Result<AstroProfileSummary> = withContext(ioDispatcher) {
        try {
            val summary = astrologyApi.getProfile(
                AstroProfileRequest(
                    date = kundli.birthDate,
                    time = kundli.birthTime,
                    place = kundli.birthPlace,
                    latitude = kundli.latitude,
                    longitude = kundli.longitude,
                    timezone = kundli.timezone
                )
            )
            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error(
                e,
                e.message ?: appContext.withAppLanguage(languageManager.current)
                    .getString(R.string.numerology_profile_load_failed)
            )
        }
    }
}
