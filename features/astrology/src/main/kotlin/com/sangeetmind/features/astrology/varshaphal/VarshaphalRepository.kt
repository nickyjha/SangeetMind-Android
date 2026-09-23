package com.sangeetmind.features.astrology.varshaphal

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.InterpretationApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.VarshaphalRequest
import com.sangeetmind.libs.models.VarshaphalResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VarshaphalRepository @Inject constructor(
    private val interpretationApi: InterpretationApi,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getVarshaphal(kundli: Kundli, year: Int): Result<VarshaphalResponse> =
        withContext(ioDispatcher) {
            try {
                val varshaphal = interpretationApi.getVarshaphal(
                    VarshaphalRequest(
                        date = kundli.birthDate,
                        time = kundli.birthTime,
                        timezone = kundli.timezone,
                        place = kundli.birthPlace,
                        lat = kundli.latitude,
                        lon = kundli.longitude,
                        year = year
                    )
                )
                Result.Success(varshaphal)
            } catch (e: Exception) {
                Result.Error(
                    e,
                    e.message ?: appContext.withAppLanguage(languageManager.current)
                        .getString(R.string.varshaphal_error_load)
                )
            }
        }
}
