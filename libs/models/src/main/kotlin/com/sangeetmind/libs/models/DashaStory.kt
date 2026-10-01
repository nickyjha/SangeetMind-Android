package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- Lifetime dasha story: POST /llm/dasha-story (backend llm_dasha_story_service.py) ----
// One chapter per Vimshottari mahadasha, w1..w9 from birth. `kind` is the period's tone
// (growth | steady | sensitive), `when` is past | current | future; dates and ages come from
// the backend so the prose never carries them.

@JsonClass(generateAdapter = true)
data class DashaStoryRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String = "life",
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class DashaChapter(
    val id: String = "",
    val start: String = "",
    val end: String = "",
    val kind: String = "",
    val strength: String = "",
    val mahadasha: String = "",
    // `when` is a Kotlin keyword.
    @Json(name = "when") val phase: String = "",
    @Json(name = "age_from") val ageFrom: Int = 0,
    @Json(name = "age_to") val ageTo: Int = 0,
    val title: String = "",
    val why: String = ""
)

@JsonClass(generateAdapter = true)
data class DashaStoryReading(
    val summary: String = "",
    val timing: List<DashaChapter> = emptyList(),
    val now: String = "",
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DashaStoryResponse(
    val ok: Boolean,
    val status: String = "life",
    val reading: DashaStoryReading? = null,
    val windows: List<DashaChapter> = emptyList(),
    val error: String? = null
)
