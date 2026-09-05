package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- POST /v1/chart (scoped subset — full response has many more fields: divisional
// charts d2..d60, KP, jaimini, aspects, bhavabala. Out of scope for this MVP screen.) ----

@JsonClass(generateAdapter = true)
data class ChartRequest(
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val timezone: String? = null,
    val place: String = "",
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class LagnaInfo(
    val sign: String,
    val degree: Double,
    val absolute: Double
)

@JsonClass(generateAdapter = true)
data class PlanetInfo(
    val sign: String,
    val degree: Double,
    val absolute: String,
    val retrograde: Boolean = false,
    val combust: Boolean = false,
    val exalted: Boolean = false,
    val debilitated: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MoonNakshatraInfo(
    val index: Int,
    val pada: Int,
    val lord: String
)

@JsonClass(generateAdapter = true)
data class DashaPeriod(
    val lord: String,
    val start: String,
    val end: String
)

@JsonClass(generateAdapter = true)
data class VimshottariCurrent(
    val mahadasha: DashaPeriod?,
    val antardasha: DashaPeriod?,
    val pratyantardasha: DashaPeriod?
)

@JsonClass(generateAdapter = true)
data class VimshottariInfo(
    val current: VimshottariCurrent?
)

@JsonClass(generateAdapter = true)
data class ChartSummaryResponse(
    val lagna: LagnaInfo,
    val planets: Map<String, PlanetInfo>,
    @Json(name = "moon_nakshatra") val moonNakshatra: MoonNakshatraInfo,
    val vimshottari: VimshottariInfo
)

// ---- POST /rules-engine/analyze-chart ----

@JsonClass(generateAdapter = true)
data class RulesEngineBirthDetails(
    @Json(name = "birth_date") val birthDate: String,
    @Json(name = "birth_time") val birthTime: String,
    @Json(name = "birth_place") val birthPlace: String,
    val coordinates: Map<String, Double>? = null,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartAnalysisRequest(
    @Json(name = "birth_details") val birthDetails: RulesEngineBirthDetails,
    @Json(name = "narration_style") val narrationStyle: String = "product"
)

@JsonClass(generateAdapter = true)
data class RuleEffect(
    @Json(name = "rule_id") val ruleId: String,
    @Json(name = "rule_name") val ruleName: String,
    val weight: Int,
    val tags: List<String> = emptyList(),
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChartAnalysis(
    val narrative: String? = null,
    @Json(name = "positive_effects") val positiveEffects: List<RuleEffect> = emptyList(),
    val challenges: List<RuleEffect> = emptyList(),
    val remedies: List<RuleEffect> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChartAnalysisResponse(
    val success: Boolean,
    val analysis: ChartAnalysis?
)
