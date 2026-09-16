package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RaagTrack(
    val raag: String,
    val purpose: String
)

@JsonClass(generateAdapter = true)
data class RaagPlaylist(
    val tradition: String,
    @Json(name = "mahadashaLord") val mahadashaLord: String,
    @Json(name = "antardashaLord") val antardashaLord: String? = null,
    @Json(name = "timeOfDay") val timeOfDay: String,
    val tracks: List<RaagTrack> = emptyList(),
    @Json(name = "previewOnly") val previewOnly: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DailyRaagResponse(
    val success: Boolean,
    val playlist: RaagPlaylist
)

@JsonClass(generateAdapter = true)
data class JapaLogRequest(
    @Json(name = "mantraId") val mantraId: String = "generic",
    @Json(name = "japaCount") val japaCount: Int
)

@JsonClass(generateAdapter = true)
data class JapaLogResponse(val success: Boolean)

@JsonClass(generateAdapter = true)
data class MantraTally(
    @Json(name = "mantraId") val mantraId: String,
    val total: Int,
    val sessions: Int
)

@JsonClass(generateAdapter = true)
data class JapaStats(
    @Json(name = "totalJapa") val totalJapa: Int = 0,
    @Json(name = "byMantra") val byMantra: List<MantraTally> = emptyList(),
    @Json(name = "streakDays") val streakDays: Int = 0
)

@JsonClass(generateAdapter = true)
data class JapaStatsResponse(
    val success: Boolean,
    val stats: JapaStats
)

/**
 * The backend generates TTS in-memory and never persists/serves it (see
 * voice_horoscope_service.py) — there is no playable audio URL, only a text [script] and
 * whether audio generation succeeded server-side for this request. UI should present the
 * script as readable text, not attempt playback.
 */
@JsonClass(generateAdapter = true)
data class VoiceHoroscopeResponse(
    val success: Boolean,
    val sign: String,
    val lang: String,
    val script: String,
    @Json(name = "audioAvailable") val audioAvailable: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SoundHealingSession(
    val id: String,
    val title: String,
    @Json(name = "durationMin") val durationMin: Int,
    val theme: String
)

@JsonClass(generateAdapter = true)
data class SoundHealingResponse(
    val success: Boolean,
    val sessions: List<SoundHealingSession> = emptyList(),
    @Json(name = "premiumRequired") val premiumRequired: Boolean = false
)
