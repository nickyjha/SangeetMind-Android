package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HoroscopeTheme(
    @Json(name = "short_label") val shortLabel: String = "",
    @Json(name = "long_text") val longText: String = "",
    val emotion: String = "",
    val keywords: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TransitMoon(
    val sign: String? = null,
    val nakshatra: String? = null,
    val longitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class RecommendedMantra(
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class HoroscopeEnhancement(
    val interpretation: String? = null,
    @Json(name = "what_to_do_today") val whatToDoToday: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class DailyHoroscope(
    val sign: String,
    @Json(name = "date_iso") val dateIso: String = "",
    @Json(name = "transit_moon") val transitMoon: TransitMoon? = null,
    @Json(name = "house_from_natal") val houseFromNatal: Int = 0,
    val theme: HoroscopeTheme? = null,
    @Json(name = "recommended_mantras") val recommendedMantras: List<RecommendedMantra> = emptyList(),
    val enhanced: HoroscopeEnhancement? = null
)

@JsonClass(generateAdapter = true)
data class DailyHoroscopeResponse(
    val success: Boolean = true,
    val horoscope: DailyHoroscope
)

@JsonClass(generateAdapter = true)
data class PeriodHoroscope(
    val sign: String,
    val period: String,
    val start: String,
    val end: String,
    val summary: DailyHoroscope
)

@JsonClass(generateAdapter = true)
data class PeriodHoroscopeResponse(
    val success: Boolean = true,
    val horoscope: PeriodHoroscope
)
