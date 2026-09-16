package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Astrologer(
    @Json(name = "astrologerId") val astrologerId: String,
    @Json(name = "displayName") val displayName: String,
    val languages: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    @Json(name = "ratePaisePerMin") val ratePaisePerMin: Long,
    val online: Boolean = false,
    @Json(name = "ratingAvg") val ratingAvg: Double = 0.0,
    val bio: String? = null
)

@JsonClass(generateAdapter = true)
data class AstrologerListResponse(val astrologers: List<Astrologer>)

@JsonClass(generateAdapter = true)
data class SubmitReviewRequest(
    val rating: Int,
    val comment: String
)

@JsonClass(generateAdapter = true)
data class SubmitReviewResponse(val success: Boolean)

@JsonClass(generateAdapter = true)
data class DebitChatMinuteRequest(
    @Json(name = "astrologerId") val astrologerId: String,
    @Json(name = "idempotencyKey") val idempotencyKey: String
)

@JsonClass(generateAdapter = true)
data class DebitChatMinuteResponse(
    val success: Boolean,
    val applied: Boolean,
    @Json(name = "amountPaise") val amountPaise: Long
)
