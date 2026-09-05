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
