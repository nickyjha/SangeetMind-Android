package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BirthDetailsPayload(
    val date: String,
    val time: String,
    val place: String,
    val lat: Double,
    val lon: Double,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class PurchaseReportRequest(
    @Json(name = "skuId") val skuId: String,
    @Json(name = "birthDetails") val birthDetails: BirthDetailsPayload,
    val lang: String = "en",
    @Json(name = "idempotencyKey") val idempotencyKey: String? = null
)

@JsonClass(generateAdapter = true)
data class ReportJob(
    val success: Boolean = true,
    @Json(name = "jobId") val jobId: String,
    val status: String,
    @Json(name = "storageKey") val storageKey: String? = null
)

@JsonClass(generateAdapter = true)
data class ReportJobStatus(
    @Json(name = "jobId") val jobId: String,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "skuId") val skuId: String,
    val status: String,
    val lang: String? = null,
    @Json(name = "storageKey") val storageKey: String? = null,
    val error: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class MyReportItem(
    @Json(name = "jobId") val jobId: String,
    @Json(name = "skuId") val skuId: String,
    val status: String,
    @Json(name = "storageKey") val storageKey: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class MyReportsResponse(val reports: List<MyReportItem>)
