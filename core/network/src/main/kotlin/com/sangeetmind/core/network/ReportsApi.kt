package com.sangeetmind.core.network

import com.sangeetmind.libs.models.MyReportsResponse
import com.sangeetmind.libs.models.PurchaseReportRequest
import com.sangeetmind.libs.models.ReportJob
import com.sangeetmind.libs.models.ReportJobStatus
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ReportsApi {
    @POST("v1/reports/purchase")
    suspend fun purchaseReport(@Body body: PurchaseReportRequest): ReportJob

    @GET("v1/reports/{jobId}/status")
    suspend fun getReportStatus(@Path("jobId") jobId: String): ReportJobStatus

    @Streaming
    @GET("v1/reports/{jobId}/download")
    suspend fun downloadReport(@Path("jobId") jobId: String): Response<ResponseBody>

    @GET("v1/reports/mine/list")
    suspend fun listMyReports(): MyReportsResponse
}
