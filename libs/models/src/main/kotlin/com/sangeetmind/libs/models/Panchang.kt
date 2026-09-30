package com.sangeetmind.libs.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PanchangIndexName(
    val index: Int,
    val name: String,
    // Tithi only: "Shukla" | "Krishna" (absent on other elements and older responses).
    val paksha: String? = null
)

/** A local-time span, "HH:MM" in the place's time zone. */
@JsonClass(generateAdapter = true)
data class PanchangSpan(
    val start: String? = null,
    val end: String? = null
)

@JsonClass(generateAdapter = true)
data class Choghadiya(
    val name: String,
    val quality: String, // "auspicious" | "neutral" | "inauspicious"
    val start: String? = null,
    val end: String? = null
)

/** GET /v1/panchang response (app/services/panchang_service.py::compute_panchang). */
@JsonClass(generateAdapter = true)
data class PanchangResponse(
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val tithi: PanchangIndexName,
    val nakshatra: PanchangIndexName,
    val yoga: PanchangIndexName,
    val karana: PanchangIndexName,
    val vara: PanchangIndexName,
    val moonSign: String,
    val sunSign: String,
    // Local HH:MM times (null on older backends or where the Sun doesn't rise).
    val sunrise: String? = null,
    val sunset: String? = null,
    val rahuKalam: PanchangSpan? = null,
    val abhijitMuhurat: PanchangSpan? = null,
    val choghadiya: List<Choghadiya> = emptyList()
)
