package com.sangeetmind.features.astrology.profile

import com.sangeetmind.core.network.friendlyErrorMessage
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
import com.sangeetmind.libs.models.SupportMantra
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _supportMantras = MutableStateFlow<List<SupportMantra>>(emptyList())

    /**
     * Support mantras from the last profile loaded (Home's primary kundli), so Geet can tag
     * them without another request. Empty until Home loads, or on an older backend.
     */
    val supportMantras: StateFlow<List<SupportMantra>> = _supportMantras.asStateFlow()

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
            _supportMantras.value = summary.supportMantras
            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error(
                e,
                friendlyErrorMessage(
                    e,
                    appContext.withAppLanguage(languageManager.current),
                    appContext.withAppLanguage(languageManager.current).getString(R.string.numerology_profile_load_failed)
                )
            )
        }
    }
}
