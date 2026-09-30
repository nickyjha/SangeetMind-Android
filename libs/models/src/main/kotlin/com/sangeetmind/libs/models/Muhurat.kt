package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MuhuratRequest(
    val intent: String,
    @Json(name = "window_start") val windowStart: String, // YYYY-MM-DD
    @Json(name = "window_end") val windowEnd: String, // YYYY-MM-DD
    val lat: Double = 28.6139,
    val lon: Double = 77.209,
    val tradition: String = "drik"
)

@JsonClass(generateAdapter = true)
data class MuhuratSlot(
    val date: String,
    val score: Int,
    val tithi: String,
    val nakshatra: String,
    val vara: String,
    val verdict: String // "auspicious" | "neutral" | "avoid"
)

@JsonClass(generateAdapter = true)
data class MuhuratResponse(
    val intent: String,
    val windowStart: String,
    val windowEnd: String,
    val results: List<MuhuratSlot> = emptyList()
)

// ---- Vivah (marriage) muhurat: POST /v1/muhurat/vivah (vivah_muhurat_service.py) ----

@JsonClass(generateAdapter = true)
data class VivahRequest(
    @Json(name = "window_start") val windowStart: String,
    @Json(name = "window_end") val windowEnd: String,
    val lat: Double,
    val lon: Double,
    val tz: String = "Asia/Kolkata",
    // Up to two birth details; adds Chandra bala and Tara bala for the couple.
    val people: List<CareerBirthDetails>? = null
)

@JsonClass(generateAdapter = true)
data class VivahWindow(
    val start: String, // ISO-8601 with offset
    val end: String,
    val nakshatras: List<String> = emptyList(),
    val tithis: List<LocalizedText> = emptyList(),
    val shukla: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VivahDay(
    val date: String,
    val weekday: String = "",
    val month: String = "",
    val score: Int = 0,
    val windows: List<VivahWindow> = emptyList()
)

/** A stretch with no vivah muhurat: kharmas, chaturmas, adhik_maas, jupiter_combust,
 * venus_combust or eclipse. */
@JsonClass(generateAdapter = true)
data class VivahBlock(
    val reason: String,
    val start: String,
    val end: String,
    val text: LocalizedText = LocalizedText()
)

@JsonClass(generateAdapter = true)
data class VivahResponse(
    @Json(name = "couple_checked") val coupleChecked: Boolean = false,
    val days: List<VivahDay> = emptyList(),
    val blocked: List<VivahBlock> = emptyList()
)
