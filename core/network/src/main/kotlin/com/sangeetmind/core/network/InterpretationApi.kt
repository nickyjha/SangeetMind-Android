package com.sangeetmind.core.network

import com.sangeetmind.libs.models.ChartAnalysisRequest
import com.sangeetmind.libs.models.ChartAnalysisResponse
import com.sangeetmind.libs.models.ChartRequest
import com.sangeetmind.libs.models.PhalSummary
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.TransitRequest
import com.sangeetmind.libs.models.TransitResponse
import com.sangeetmind.libs.models.VarshaphalRequest
import com.sangeetmind.libs.models.VarshaphalResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface InterpretationApi {
    @POST("v1/chart")
    suspend fun getChart(@Body body: ChartRequest): ChartSummaryResponse

    /** Printable kundli PDF for the same body as [getChart] (app/api/v1/routes.py chart_pdf). */
    @Streaming
    @POST("v1/chart/pdf")
    suspend fun getChartPdf(@Body body: ChartRequest, @Query("name") name: String?): Response<ResponseBody>

    @POST("v1/varshaphal")
    suspend fun getVarshaphal(@Body body: VarshaphalRequest): VarshaphalResponse

    @POST("v1/transit")
    suspend fun getTransit(@Body body: TransitRequest): TransitResponse

    /** Compact phal-engine verdicts for the Full Reading screen (app/api/v1/routes.py phal_summary). */
    @POST("v1/phal/summary")
    suspend fun getPhalSummary(
        @Body body: ChartRequest,
        @Query("married") married: Boolean? = null // drops the Mangal-dosha matching penalty
    ): PhalSummary

    @POST("rules-engine/analyze-chart")
    suspend fun analyzeChart(
        @Body body: ChartAnalysisRequest,
        @Query("with_llm") withLlm: Boolean = true,
        @Query("lang") lang: String = "en" // en | hi | sn (app/routes/rules_engine.py)
    ): ChartAnalysisResponse
}
