package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApplyReferralRequest(
    @Json(name = "referrerUserId") val referrerUserId: String
)

@JsonClass(generateAdapter = true)
data class ApplyReferralResponse(
    val success: Boolean,
    val created: Boolean,
    @Json(name = "referrerCount") val referrerCount: Int
)

@JsonClass(generateAdapter = true)
data class ReferralStats(
    @Json(name = "referralCount") val referralCount: Int,
    @Json(name = "rewardEligible") val rewardEligible: Boolean,
    @Json(name = "rewardDescription") val rewardDescription: String
)
