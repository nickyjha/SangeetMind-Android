package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchBirthDetails(
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val place: String? = null,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class KundliMatchRequest(
    val personA: MatchBirthDetails,
    val personB: MatchBirthDetails
)

@JsonClass(generateAdapter = true)
data class KundliMatchEnvelope(
    val success: Boolean,
    val match: KundliMatchResult
)

@JsonClass(generateAdapter = true)
data class Koota(
    val points: Double,
    val max: Double
)

@JsonClass(generateAdapter = true)
data class Ashtakoot(
    val varna: Koota,
    val vashya: Koota,
    val tara: Koota,
    val yoni: Koota,
    @Json(name = "grahaMaitri") val grahaMaitri: Koota,
    val gana: Koota,
    val bhakoot: Koota,
    val nadi: Koota
)

@JsonClass(generateAdapter = true)
data class ManglikStatus(
    val present: Boolean,
    @Json(name = "effective_present") val effectivePresent: Boolean,
    val cancelled: Boolean,
    val summary: String
)

@JsonClass(generateAdapter = true)
data class ManglikPair(
    val personA: ManglikStatus,
    val personB: ManglikStatus
)

@JsonClass(generateAdapter = true)
data class KundliMatchResult(
    val ashtakoot: Ashtakoot,
    @Json(name = "totalGunas") val totalGunas: Double,
    @Json(name = "maxGunas") val maxGunas: Double,
    val manglik: ManglikPair,
    val verdict: String,
    @Json(name = "remedyHint") val remedyHint: String? = null
)
