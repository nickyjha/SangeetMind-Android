package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Request body for POST /astro/get-profile. */
@JsonClass(generateAdapter = true)
data class AstroProfileRequest(
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val place: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null
)

/** Response of POST /astro/get-profile — used for the dashboard summary card. */
@JsonClass(generateAdapter = true)
data class AstroProfileSummary(
    @Json(name = "moon_sign") val moonSign: String,
    val lagna: String,
    val nakshatra: String = "",
    @Json(name = "nakshatra_ruler") val nakshatraRuler: String = "",
    @Json(name = "current_mahadasha") val currentMahadasha: String = "",
    @Json(name = "current_antardasha") val currentAntardasha: String = "",
    @Json(name = "planet_positions") val planetPositions: Map<String, String> = emptyMap(),
    @Json(name = "astro_mood") val astroMood: String = "",
    @Json(name = "suggested_raag") val suggestedRaag: String = "",
    /** Running dasha for "now" + next change (Home countdown card). Null on older backends. */
    @Json(name = "current_period") val currentPeriod: CurrentPeriod? = null,
    /** Ranked graha beej mantras for japa (Home "Today's Geet"). Empty on older backends. */
    @Json(name = "support_mantras") val supportMantras: List<SupportMantra> = emptyList()
)

/**
 * One item of `support_mantras` (backend app/services/support_mantras.py), in
 * /astro/get-profile and POST /v1/chart.
 */
@JsonClass(generateAdapter = true)
data class SupportMantra(
    /** English graha name: "Jupiter". */
    val planet: String = "",
    /** Japa mantra id: "guru_beej". */
    @Json(name = "mantra_id") val mantraId: String = "",
    /**
     * "strengthen_weak" | "support_running_period" | "prepare_upcoming_period" |
     * "ongoing_protection".
     */
    val reason: String = "",
    /** ISO date the graha's period starts (prepare_upcoming_period only). */
    @Json(name = "period_lord_from") val periodLordFrom: String? = null,
    val priority: Int = 0
) {
    companion object {
        const val REASON_WEAK = "strengthen_weak"
        const val REASON_RUNNING = "support_running_period"
        const val REASON_UPCOMING = "prepare_upcoming_period"
        const val REASON_PROTECTION = "ongoing_protection"
    }
}
