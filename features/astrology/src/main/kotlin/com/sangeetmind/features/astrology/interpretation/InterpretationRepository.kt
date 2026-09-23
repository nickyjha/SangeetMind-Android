package com.sangeetmind.features.astrology.interpretation

import android.content.Context
import androidx.annotation.StringRes
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.InterpretationApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.ChartAnalysis
import com.sangeetmind.libs.models.ChartAnalysisRequest
import com.sangeetmind.libs.models.ChartRequest
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.RulesEngineBirthDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class InterpretationData(
    val chart: ChartSummaryResponse,
    val analysis: ChartAnalysis?
)

@Singleton
class InterpretationRepository @Inject constructor(
    private val interpretationApi: InterpretationApi,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    /** The rules-engine narrative/effects come back in the app's current display language. */
    suspend fun getInterpretation(kundli: Kundli): Result<InterpretationData> =
        withContext(ioDispatcher) {
            try {
                val chart = interpretationApi.getChart(
                    ChartRequest(
                        date = kundli.birthDate,
                        time = kundli.birthTime,
                        timezone = kundli.timezone,
                        place = kundli.birthPlace,
                        lat = kundli.latitude,
                        lon = kundli.longitude
                    )
                )
                val analysis = runCatching {
                    interpretationApi.analyzeChart(
                        ChartAnalysisRequest(
                            birthDetails = RulesEngineBirthDetails(
                                birthDate = kundli.birthDate,
                                birthTime = kundli.birthTime,
                                birthPlace = kundli.birthPlace,
                                coordinates = mapOf("lat" to kundli.latitude, "lon" to kundli.longitude),
                                timezone = kundli.timezone
                            )
                        ),
                        lang = languageManager.current.code
                    ).analysis
                }.getOrNull()

                Result.Success(InterpretationData(chart, analysis))
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.interpretation_err_load_reading))
            }
        }
}
