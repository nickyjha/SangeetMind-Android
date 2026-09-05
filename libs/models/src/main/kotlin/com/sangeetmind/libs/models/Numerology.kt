package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Request for POST /numerology/calculate/pythagorean (app/numerology/models.py::NumerologyRequest). */
@JsonClass(generateAdapter = true)
data class NumerologyRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "date_of_birth") val dateOfBirth: String, // YYYY-MM-DD
    val gender: String = "male",
    @Json(name = "current_name") val currentName: String? = null
)

@JsonClass(generateAdapter = true)
data class NumberDetail(
    val number: Int,
    @Json(name = "is_master_number") val isMasterNumber: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CoreNumbers(
    @Json(name = "life_path") val lifePath: NumberDetail,
    val destiny: NumberDetail,
    @Json(name = "soul_urge") val soulUrge: NumberDetail,
    val personality: NumberDetail,
    val maturity: NumberDetail,
    @Json(name = "birth_day") val birthDay: NumberDetail,
    val attitude: NumberDetail
)

@JsonClass(generateAdapter = true)
data class NumberInterpretation(
    val number: Int,
    val name: String,
    @Json(name = "display_name") val displayName: String,
    val archetype: String,
    val element: String,
    val vibration: String,
    @Json(name = "core_traits") val coreTraits: List<String> = emptyList(),
    val strengths: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    @Json(name = "spiritual_theme") val spiritualTheme: String = "",
    @Json(name = "growth_advice") val growthAdvice: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RaagMoodsResponse(
    val recommended: List<String> = emptyList(),
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class PracticeTimeResponse(
    val preferred: String = "",
    val reasoning: String = ""
)

@JsonClass(generateAdapter = true)
data class HabitStyleResponse(
    @Json(name = "primary_style") val primaryStyle: String = "",
    val approach: String = "",
    @Json(name = "practice_tips") val practiceTips: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SangeetMindPersonalization(
    @Json(name = "raag_moods") val raagMoods: RaagMoodsResponse,
    @Json(name = "practice_time") val practiceTime: PracticeTimeResponse,
    @Json(name = "habit_style") val habitStyle: HabitStyleResponse,
    @Json(name = "personalization_summary") val personalizationSummary: String = ""
)

/** Response of POST /numerology/calculate/pythagorean (app/numerology/models.py::NumerologyResponse). */
@JsonClass(generateAdapter = true)
data class NumerologyResponse(
    @Json(name = "core_numbers") val coreNumbers: CoreNumbers,
    val interpretations: Map<String, NumberInterpretation> = emptyMap(),
    @Json(name = "sangeetmind_personalization") val sangeetmindPersonalization: SangeetMindPersonalization
)
