package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** POST /v1/daily/scores (app/services/daily_scores_service.py). */
@JsonClass(generateAdapter = true)
data class DailyScoresRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val tz: String? = null
)

/** One life area (self | wealth | love | career): level is good | mixed | low. */
@JsonClass(generateAdapter = true)
data class AreaScore(
    val score: Int,
    val level: String,
    val text: LocalizedText = LocalizedText(),
    val reason: LocalizedText = LocalizedText(),
    val question: LocalizedText = LocalizedText()
)

@JsonClass(generateAdapter = true)
data class LuckyTime(
    val name: String,
    val start: String? = null,
    val end: String? = null
)

/** label: yesterday | today | tomorrow. */
@JsonClass(generateAdapter = true)
data class DayScores(
    val date: String,
    val label: String,
    val overall: Int,
    val areas: Map<String, AreaScore> = emptyMap(),
    @Json(name = "lucky_number") val luckyNumber: Int? = null,
    @Json(name = "lucky_time") val luckyTime: LuckyTime? = null
)

@JsonClass(generateAdapter = true)
data class StripDay(
    val date: String,
    val overall: Int
)

@JsonClass(generateAdapter = true)
data class DailyScoresResponse(
    val today: String,
    val days: List<DayScores> = emptyList(),
    val strip: List<StripDay> = emptyList()
)
