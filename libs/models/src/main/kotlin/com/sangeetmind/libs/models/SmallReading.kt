package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** POST /llm/small (app/services/llm_small_reading_service.py); topic is one of SMALL_TOPICS. */
@JsonClass(generateAdapter = true)
data class SmallReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val topic: String,
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class SmallReadingResponse(
    val ok: Boolean,
    val topic: String,
    val reading: SmallReading? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SmallReading(
    val summary: String = "",
    val points: List<SmallPoint> = emptyList(),
    val tip: String = "",
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SmallPoint(
    val title: String = "",
    val text: String = ""
)
