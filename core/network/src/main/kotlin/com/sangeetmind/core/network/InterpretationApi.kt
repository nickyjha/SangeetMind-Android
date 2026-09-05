package com.sangeetmind.core.network

import com.sangeetmind.libs.models.ChartAnalysisRequest
import com.sangeetmind.libs.models.ChartAnalysisResponse
import com.sangeetmind.libs.models.ChartRequest
import com.sangeetmind.libs.models.ChartSummaryResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface InterpretationApi {
    @POST("v1/chart")
    suspend fun getChart(@Body body: ChartRequest): ChartSummaryResponse

    @POST("rules-engine/analyze-chart")
    suspend fun analyzeChart(
        @Body body: ChartAnalysisRequest,
        @Query("with_llm") withLlm: Boolean = true
    ): ChartAnalysisResponse
}
