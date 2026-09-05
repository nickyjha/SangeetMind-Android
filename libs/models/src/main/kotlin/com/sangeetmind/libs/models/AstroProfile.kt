package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Request body for POST /astro/get-profile. */
@JsonClass(generateAdapter = true)
data class AstroProfileRequest(
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val place: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null
)

/** Response of POST /astro/get-profile — used for the dashboard summary card. */
@JsonClass(generateAdapter = true)
data class AstroProfileSummary(
    @Json(name = "moon_sign") val moonSign: String,
    val lagna: String,
    val nakshatra: String = "",
    @Json(name = "nakshatra_ruler") val nakshatraRuler: String = "",
    @Json(name = "current_mahadasha") val currentMahadasha: String = "",
    @Json(name = "current_antardasha") val currentAntardasha: String = "",
    @Json(name = "planet_positions") val planetPositions: Map<String, String> = emptyMap(),
    @Json(name = "astro_mood") val astroMood: String = "",
    @Json(name = "suggested_raag") val suggestedRaag: String = ""
)
