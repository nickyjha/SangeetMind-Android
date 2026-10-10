package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- Phal engine summary: POST /v1/phal/summary ----
// Deterministic verdicts from the backend phal engine (app/services/phal_engine): every
// score is -3..+3 and every label is one of very_strong / strong / mixed / weak / very_weak.
// Nothing here comes from an LLM; the same engine grounds the AI readings.

@JsonClass(generateAdapter = true)
data class PhalDashaTone(
    val level: String = "",
    val lord: String = "",
    @Json(name = "lord_hi") val lordHi: String = "",
    val end: String? = null,
    val label: LocalizedText = LocalizedText(),
    val score: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class PhalGochar(
    val label: LocalizedText = LocalizedText(),
    val score: Double = 0.0,
    val cautions: List<LocalizedText> = emptyList(),
    val strengths: List<LocalizedText> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PhalTopic(
    val topic: String = "",
    val en: String = "",
    val hi: String = "",
    val label: LocalizedText = LocalizedText(),
    val score: Double = 0.0,
    val agreement: String = "",
    @Json(name = "main_house") val mainHouse: Int? = null,
    val lord: String? = null,
    val strengths: List<LocalizedText> = emptyList(),
    val cautions: List<LocalizedText> = emptyList(),
    val dasha: List<PhalDashaTone> = emptyList(),
    val gochar: PhalGochar? = null,
    /** Concept shares that add up to the score (phal_engine/breakdown.py), largest first. */
    val breakdown: List<PhalBreakdownPart> = emptyList(),
    /** Where the score falls among other charts, e.g. "Stronger than 68% of charts". */
    val compared: PhalCompared? = null,
    /** Career only: kind of work (Brihat Jataka 10.1-3, Phaladeepika 5.1-8). */
    val vocation: PhalVocation? = null
) {
    fun name(code: String): String = if (code == "hi") hi.ifBlank { en } else en
}

@JsonClass(generateAdapter = true)
data class PhalBreakdownPart(
    val key: String = "",
    val en: String = "",
    val hi: String = "",
    val weight: Double = 0.0
) {
    fun name(code: String): String = if (code == "hi") hi.ifBlank { en } else en
}

@JsonClass(generateAdapter = true)
data class PhalCompared(
    val percentile: Int = 0,
    val en: String = "",
    val hi: String = ""
) {
    fun text(code: String): String = if (code == "hi") hi.ifBlank { en } else en
}

@JsonClass(generateAdapter = true)
data class PhalVocation(
    val planet: String = "",
    val work: LocalizedText = LocalizedText(),
    val source: String? = null
)

@JsonClass(generateAdapter = true)
data class PhalPeriod(
    val level: String = "",
    val lord: String = "",
    @Json(name = "lord_hi") val lordHi: String = "",
    val start: String? = null,
    val end: String? = null,
    val label: LocalizedText = LocalizedText(),
    val score: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class PhalPlanet(
    val planet: String = "",
    val hi: String = "",
    val sign: String? = null,
    val house: Int? = null,
    val label: LocalizedText = LocalizedText(),
    val score: Double = 0.0,
    /** Shadbala bucket (very_strong / strong / adequate / weak / very_weak); null for Rahu/Ketu. */
    val strength: LocalizedText? = null,
    /** Placement favourability (very favourable ... very unfavourable), from the score band. */
    val placement: LocalizedText? = null,
    val gives: LocalizedText? = null,
    val tests: LocalizedText? = null,
    /** Both sides in one or two sentences: strength and what it gives, placement and its tests. */
    val reading: LocalizedText? = null,
    val strengths: List<LocalizedText> = emptyList(),
    val cautions: List<LocalizedText> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PhalHouse(
    val house: Int = 0,
    val en: String = "",
    val hi: String = "",
    val sign: String? = null,
    val lord: String? = null,
    val label: LocalizedText = LocalizedText(),
    val score: Double = 0.0
) {
    fun name(code: String): String = if (code == "hi") hi.ifBlank { en } else en
}

@JsonClass(generateAdapter = true)
data class PhalSadeSati(
    val kind: String = "",
    val phase: String = "",
    @Json(name = "saturn_sign") val saturnSign: String = "",
    @Json(name = "house_from_moon") val houseFromMoon: Int? = null,
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null
)

@JsonClass(generateAdapter = true)
data class PhalSummary(
    val version: String? = null,
    val lagna: String? = null,
    val now: String? = null,
    val topics: List<PhalTopic> = emptyList(),
    /** What the topic labels mean ("By the classical rules ..."). */
    @Json(name = "label_basis") val labelBasis: LocalizedText? = null,
    val dasha: List<PhalPeriod> = emptyList(),
    val planets: List<PhalPlanet> = emptyList(),
    val houses: List<PhalHouse> = emptyList(),
    val sadesati: PhalSadeSati? = null,
    val remedies: List<PhalRemedy> = emptyList()
)

/** Mantra / raag / dana for a debilitated, combust or weak planet (phal_engine/remedies.py). */
@JsonClass(generateAdapter = true)
data class PhalRemedy(
    val planet: String = "",
    val condition: String = "",
    val en: String = "",
    val hi: String = ""
)
