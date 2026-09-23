package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Request for POST /numerology/calculate/pythagorean (app/numerology/models.py::NumerologyRequest). */
@JsonClass(generateAdapter = true)
data class NumerologyRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "date_of_birth") val dateOfBirth: String, // YYYY-MM-DD
    val gender: String = "male",
    @Json(name = "current_name") val currentName: String? = null,
    /** en | hi — only affects the optional LLM summary (app/numerology/models.py). */
    @Json(name = "preferred_language") val preferredLanguage: String = "en"
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

// ---- Chaldean (app/numerology/models.py's CHALDEAN SYSTEM MODELS section) ----

@JsonClass(generateAdapter = true)
data class CompoundNumberMeaning(
    val number: Int,
    val name: String = "",
    val nature: String = "",
    val meaning: String = ""
)

@JsonClass(generateAdapter = true)
data class ChaldeanNumberDetail(
    val number: Int,
    @Json(name = "compound_number") val compoundNumber: Int? = null,
    @Json(name = "compound_meaning") val compoundMeaning: CompoundNumberMeaning? = null
)

@JsonClass(generateAdapter = true)
data class ChaldeanNumbers(
    @Json(name = "name_number") val nameNumber: ChaldeanNumberDetail,
    @Json(name = "birth_number") val birthNumber: ChaldeanNumberDetail,
    @Json(name = "destiny_number") val destinyNumber: ChaldeanNumberDetail,
    @Json(name = "soul_number") val soulNumber: ChaldeanNumberDetail,
    @Json(name = "personality_number") val personalityNumber: ChaldeanNumberDetail
)

/** Response of POST /numerology/calculate/chaldean (app/numerology/models.py::ChaldeanResponse). */
@JsonClass(generateAdapter = true)
data class ChaldeanResponse(
    @Json(name = "name_used") val nameUsed: String = "",
    @Json(name = "is_current_name") val isCurrentName: Boolean = false,
    val numbers: ChaldeanNumbers,
    @Json(name = "sangeetmind_personalization") val sangeetmindPersonalization: SangeetMindPersonalization? = null
)

// ---- Vedic (app/numerology/models.py's VEDIC SYSTEM MODELS section) ----

@JsonClass(generateAdapter = true)
data class KarmicDebtDetail(
    val code: Int,
    val meaning: String = "",
    val name: String = "",
    val remedy: String = "",
    val mantra: String = ""
)

@JsonClass(generateAdapter = true)
data class LoShuPlane(
    val numbers: List<Int> = emptyList(),
    val present: Int = 0,
    val meaning: String = "",
    val strong: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LoShuGrid(
    @Json(name = "grid_display") val gridDisplay: List<List<String>> = emptyList(),
    @Json(name = "missing_numbers") val missingNumbers: List<Int> = emptyList(),
    @Json(name = "repeated_numbers") val repeatedNumbers: List<Int> = emptyList(),
    @Json(name = "interpretation_summary") val interpretationSummary: String = "",
    val planes: Map<String, LoShuPlane> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class MoolankBhagyankCompatibility(
    val moolank: Int = 0,
    val bhagyank: Int = 0,
    val relationship: String = "",
    val description: String = "",
    @Json(name = "compatibility_score") val compatibilityScore: Int = 0,
    @Json(name = "moolank_planet") val moolankPlanet: String? = null,
    @Json(name = "bhagyank_planet") val bhagyankPlanet: String? = null
)

@JsonClass(generateAdapter = true)
data class VedicNumberDetail(
    val number: Int,
    val name: String = "",
    @Json(name = "ruling_planet") val rulingPlanet: String? = null,
    @Json(name = "sanskrit_name") val sanskritName: String? = null,
    val deity: String? = null,
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class VedicNumbers(
    val moolank: VedicNumberDetail,
    val bhagyank: VedicNumberDetail,
    val namank: VedicNumberDetail,
    @Json(name = "soul_number") val soulNumber: VedicNumberDetail,
    @Json(name = "personality_number") val personalityNumber: VedicNumberDetail,
    @Json(name = "kua_number") val kuaNumber: VedicNumberDetail
)

/** Response of POST /numerology/calculate/vedic (app/numerology/models.py::VedicResponse). */
@JsonClass(generateAdapter = true)
data class VedicResponse(
    val numbers: VedicNumbers,
    @Json(name = "lo_shu_grid") val loShuGrid: LoShuGrid,
    @Json(name = "karmic_debts") val karmicDebts: List<Int> = emptyList(),
    @Json(name = "karmic_debt_details") val karmicDebtDetails: List<KarmicDebtDetail> = emptyList(),
    @Json(name = "moolank_bhagyank_compatibility") val moolankBhagyankCompatibility: MoolankBhagyankCompatibility,
    @Json(name = "sangeetmind_personalization") val sangeetmindPersonalization: SangeetMindPersonalization? = null
)

// ---- GET /numerology/systems, /numbers, /number/{n} — reference/glossary, previously
// fully built on the backend but 100% unused by Android (app/numerology/api.py) ----

@JsonClass(generateAdapter = true)
data class NumerologySystemInfo(
    val id: String,
    val name: String = "",
    val origin: String = "",
    val description: String = "",
    @Json(name = "uses_name") val usesName: String = "",
    @Json(name = "letter_chart") val letterChart: String = "",
    @Json(name = "master_numbers") val masterNumbers: List<Int> = emptyList(),
    @Json(name = "compound_numbers") val compoundNumbers: String? = null,
    val features: List<String> = emptyList(),
    @Json(name = "key_numbers") val keyNumbers: List<String> = emptyList(),
    @Json(name = "best_for") val bestFor: String = ""
)

@JsonClass(generateAdapter = true)
data class NumerologySystemsResponse(
    val success: Boolean = true,
    val systems: List<NumerologySystemInfo> = emptyList(),
    val default: String = "",
    @Json(name = "recommendation_for_sangeetmind") val recommendationForSangeetmind: String = ""
)

@JsonClass(generateAdapter = true)
data class NumerologyNumberSummary(
    val number: Int,
    val name: String = "",
    val archetype: String = "",
    val element: String = "",
    val brief: String = "",
    @Json(name = "is_master_number") val isMasterNumber: Boolean = false
)

@JsonClass(generateAdapter = true)
data class NumerologyNumbersListResponse(
    val success: Boolean = true,
    val numbers: List<NumerologyNumberSummary> = emptyList(),
    @Json(name = "total_count") val totalCount: Int = 0
)

/** Full interpretation for one number — `context_notes` is always present in practice
 * (GET /number/{n} calls get_interpretation without a number_type override, which
 * defaults to "life_path" and always adds a note), but modeled nullable since the
 * backend function signature allows it to be absent
 * (app/numerology/interpretations/interpreter.py: get_interpretation). */
@JsonClass(generateAdapter = true)
data class NumerologyNumberInterpretation(
    val number: Int,
    val name: String = "",
    val archetype: String = "",
    val element: String = "",
    val vibration: String = "",
    @Json(name = "is_master_number") val isMasterNumber: Boolean = false,
    @Json(name = "core_traits") val coreTraits: List<String> = emptyList(),
    val strengths: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    @Json(name = "spiritual_theme") val spiritualTheme: String = "",
    @Json(name = "growth_advice") val growthAdvice: List<String> = emptyList(),
    @Json(name = "context_notes") val contextNotes: String? = null
)

@JsonClass(generateAdapter = true)
data class NumerologyNumberInterpretationResponse(
    val success: Boolean = true,
    val interpretation: NumerologyNumberInterpretation
)
