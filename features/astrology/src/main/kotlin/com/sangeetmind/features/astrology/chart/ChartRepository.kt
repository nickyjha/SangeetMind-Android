package com.sangeetmind.features.astrology.chart

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.InterpretationApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.ChartRequest
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val interpretationApi: InterpretationApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getChart(kundli: Kundli): Result<ChartSummaryResponse> =
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
                Result.Success(chart)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: appContext.getString(R.string.chart_error_calculate))
            }
        }
}
