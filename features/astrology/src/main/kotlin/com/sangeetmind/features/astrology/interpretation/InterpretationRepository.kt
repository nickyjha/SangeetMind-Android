package com.sangeetmind.features.astrology.interpretation

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.InterpretationApi
import com.sangeetmind.libs.models.ChartAnalysis
import com.sangeetmind.libs.models.ChartAnalysisRequest
import com.sangeetmind.libs.models.ChartRequest
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.RulesEngineBirthDetails
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
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
                        )
                    ).analysis
                }.getOrNull()

                Result.Success(InterpretationData(chart, analysis))
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to load your reading")
            }
        }
}
