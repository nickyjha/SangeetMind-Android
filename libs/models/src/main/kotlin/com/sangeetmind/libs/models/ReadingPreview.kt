package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** POST /llm/preview (app/services/reading_preview_service.py): free, no Gemini, no charge. */
@JsonClass(generateAdapter = true)
data class ReadingPreviewRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val topic: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class PreviewPlanet(
    val planet: String,
    val sign: String? = null,
    val house: Int? = null
)

/** Key planets and how many favourable windows lie ahead; firstYear is null when none. */
@JsonClass(generateAdapter = true)
data class ReadingPreview(
    val topic: String,
    val status: String,
    @Json(name = "key_planets") val keyPlanets: List<PreviewPlanet> = emptyList(),
    val windows: Int = 0,
    @Json(name = "strong_windows") val strongWindows: Int = 0,
    @Json(name = "first_year") val firstYear: Int? = null,
    @Json(name = "running_now") val runningNow: Boolean = false,
    val teaser: LocalizedText = LocalizedText()
)
