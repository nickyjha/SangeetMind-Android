package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- POST /v1/chart (MVP + Vimshottari timeline; full response also has d2..d60,
// doshas, friendship, KP, jaimini — deferred to later chart sprints.) ----

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
    val degree: Double = 0.0,
    val absolute: Double = 0.0,
    @Json(name = "absolute_dms") val absoluteDms: String? = null
)

@JsonClass(generateAdapter = true)
data class PlanetInfo(
    val sign: String,
    val degree: Double = 0.0,
    val absolute: String = "",
    @Json(name = "absolute_dms") val absoluteDms: String? = null,
    val house: Int? = null,
    val retrograde: Boolean = false,
    val combust: Boolean = false,
    val exalted: Boolean = false,
    val debilitated: Boolean = false,
    val vargottama: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MoonNakshatraInfo(
    val index: Int = 0,
    val pada: Int = 0,
    val lord: String = "",
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class DashaPeriod(
    val lord: String,
    val start: String,
    val end: String,
    val partial: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VimshottariCurrent(
    val mahadasha: DashaPeriod? = null,
    val antardasha: DashaPeriod? = null,
    val pratyantardasha: DashaPeriod? = null,
    @Json(name = "pratyantar_dasha") val pratyantarDashaAlt: DashaPeriod? = null,
    val pratyantar: DashaPeriod? = null,
    val now: String? = null
) {
    val resolvedPratyantar: DashaPeriod?
        get() = pratyantardasha ?: pratyantarDashaAlt ?: pratyantar
}

@JsonClass(generateAdapter = true)
data class BhuktiPeriod(
    val lord: String,
    val start: String,
    val end: String,
    val partial: Boolean = false,
    val pratyantars: List<DashaPeriod> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MahadashaPeriod(
    val lord: String,
    val start: String,
    val end: String,
    val partial: Boolean = false,
    val bhuktis: List<BhuktiPeriod> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VimshottariInfo(
    val current: VimshottariCurrent? = null,
    val mahadashas: List<MahadashaPeriod> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChartSummaryResponse(
    val lagna: LagnaInfo,
    val planets: Map<String, PlanetInfo> = emptyMap(),
    @Json(name = "moon_nakshatra") val moonNakshatra: MoonNakshatraInfo = MoonNakshatraInfo(),
    val vimshottari: VimshottariInfo = VimshottariInfo()
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

/** Classical 27 nakshatras; API `moon_nakshatra.index` is 0-based when present. */
val NAKSHATRA_NAMES: List<String> = listOf(
    "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu",
    "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
    "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
    "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
    "Uttara Bhadrapada", "Revati"
)

fun MoonNakshatraInfo.displayName(): String {
    name?.takeIf { it.isNotBlank() }?.let { return it }
    return NAKSHATRA_NAMES.getOrNull(index) ?: "Nakshatra $index"
}
