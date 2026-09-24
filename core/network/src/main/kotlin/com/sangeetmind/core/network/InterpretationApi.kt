package com.sangeetmind.core.network

import com.sangeetmind.libs.models.ChartAnalysisRequest
import com.sangeetmind.libs.models.ChartAnalysisResponse
import com.sangeetmind.libs.models.ChartRequest
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.TransitRequest
import com.sangeetmind.libs.models.TransitResponse
import com.sangeetmind.libs.models.VarshaphalRequest
import com.sangeetmind.libs.models.VarshaphalResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface InterpretationApi {
    @POST("v1/chart")
    suspend fun getChart(@Body body: ChartRequest): ChartSummaryResponse

    @POST("v1/varshaphal")
    suspend fun getVarshaphal(@Body body: VarshaphalRequest): VarshaphalResponse

    @POST("v1/transit")
    suspend fun getTransit(@Body body: TransitRequest): TransitResponse

    @POST("rules-engine/analyze-chart")
    suspend fun analyzeChart(
        @Body body: ChartAnalysisRequest,
        @Query("with_llm") withLlm: Boolean = true,
        @Query("lang") lang: String = "en" // en | hi | sn (app/routes/rules_engine.py)
    ): ChartAnalysisResponse
}
