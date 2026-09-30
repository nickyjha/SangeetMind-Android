package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** GET /v1/eclipses (app/services/eclipse_service.py): a year's solar and lunar eclipses. */
@JsonClass(generateAdapter = true)
data class EclipseCalendarResponse(
    val year: Int,
    val tz: String = "Asia/Kolkata",
    val eclipses: List<Eclipse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Eclipse(
    val kind: String, // "solar" | "lunar"
    val type: String, // "total" | "annular" | "hybrid" | "partial" | "penumbral"
    val name: LocalizedText = LocalizedText(),
    val peak: String, // ISO-8601 with the requested zone's offset
    val sign: String = "",
    val nakshatra: String = "",
    val visible: Boolean = false,
    val local: EclipseLocal? = null,
    val sutak: EclipseSutak? = null,
    @Json(name = "rashi_effects") val rashiEffects: RashiEffects = RashiEffects()
) {
    val isSolar: Boolean get() = kind == "solar"
}

/** The part of the eclipse seen from the place (bounded by sunrise/sunset or moonrise/moonset). */
@JsonClass(generateAdapter = true)
data class EclipseLocal(
    val begin: String,
    val max: String,
    val end: String,
    val magnitude: Double = 0.0
)

/** Only where the eclipse is visible and not penumbral; counted from the eclipse's own start. */
@JsonClass(generateAdapter = true)
data class EclipseSutak(
    val begin: String,
    val end: String,
    @Json(name = "hours_before") val hoursBefore: Int = 0
)

/** Moon signs by where the eclipse falls from them: 3/6/10/11 favourable, 2/5/7/9 mixed,
 * 1/4/8/12 careful. */
@JsonClass(generateAdapter = true)
data class RashiEffects(
    val favourable: List<String> = emptyList(),
    val mixed: List<String> = emptyList(),
    val careful: List<String> = emptyList()
) {
    fun effectFor(moonSign: String): String? = when (moonSign) {
        in favourable -> "favourable"
        in mixed -> "mixed"
        in careful -> "careful"
        else -> null
    }
}
