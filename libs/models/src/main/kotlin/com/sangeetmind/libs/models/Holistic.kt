package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- POST /holistic/combined (app/routes/holistic.py, app/services/holistic_analysis_service.py) ----
//
// Shapes below were pulled from a live prod response (1985-01-24 06:35 Nagda) and matched
// against the service source. The response is a hand-built dict, not a Pydantic model, so
// several branches vary by outcome: `analyses.astrology` is `{vedic_profile}` on success but
// `{note}` without birth time or `{error}` on failure; `llm_enhanced_narrative` is
// `{narrative, source}` normally but `{error, fallback}` if Gemini raised. Everything that
// can vary is nullable here rather than guessed.

/** Request for POST /holistic/combined (app/routes/holistic.py::CombinedAnalysisRequest). */
@JsonClass(generateAdapter = true)
data class HolisticCombinedRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "date_of_birth") val dateOfBirth: String, // YYYY-MM-DD
    @Json(name = "time_of_birth") val timeOfBirth: String? = null, // HH:MM
    val place: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    @Json(name = "enhance_with_llm") val enhanceWithLlm: Boolean = true,
    val language: String = "en" // en | hi — only affects the Gemini narrative
)

@JsonClass(generateAdapter = true)
data class HolisticInput(
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "date_of_birth") val dateOfBirth: String = "",
    @Json(name = "has_birth_time") val hasBirthTime: Boolean = false
)

// -- analyses.numerology.enhanced_analysis (app/numerology/interpretations/*) --
// This is the part of numerology Android has never shown: the standalone
// /numerology/calculate/* endpoints stop at core numbers + personalization.

@JsonClass(generateAdapter = true)
data class PlanetaryRuler(
    val planet: String = "",
    @Json(name = "planet_sanskrit") val planetSanskrit: String = "",
    val symbol: String = "",
    val nature: String = "",
    val day: String = "",
    val color: String = "",
    val gemstone: String = "",
    val metal: String = "",
    val deity: String = "",
    val mantra: String = "",
    @Json(name = "body_parts") val bodyParts: List<String> = emptyList(),
    @Json(name = "positive_influence") val positiveInfluence: List<String> = emptyList(),
    @Json(name = "negative_influence") val negativeInfluence: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HealthPrediction(
    val general: String = "",
    val strengths: List<String> = emptyList(),
    val vulnerabilities: List<String> = emptyList(),
    val advice: String = "",
    @Json(name = "best_practices") val bestPractices: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WealthPrediction(
    val general: String = "",
    val pattern: String = "",
    @Json(name = "peak_years") val peakYears: String = "",
    val strengths: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    val advice: String = ""
)

@JsonClass(generateAdapter = true)
data class RelationshipsPrediction(
    val general: String = "",
    @Json(name = "love_pattern") val lovePattern: String = "",
    @Json(name = "compatible_numbers") val compatibleNumbers: List<Int> = emptyList(),
    @Json(name = "challenging_numbers") val challengingNumbers: List<Int> = emptyList(),
    val strengths: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    val advice: String = ""
)

@JsonClass(generateAdapter = true)
data class SuccessPrediction(
    val general: String = "",
    @Json(name = "career_paths") val careerPaths: List<String> = emptyList(),
    @Json(name = "success_pattern") val successPattern: String = "",
    @Json(name = "peak_success_age") val peakSuccessAge: String = "",
    val advice: String = ""
)

@JsonClass(generateAdapter = true)
data class LifePredictions(
    val health: HealthPrediction? = null,
    val wealth: WealthPrediction? = null,
    val relationships: RelationshipsPrediction? = null,
    val success: SuccessPrediction? = null
)

@JsonClass(generateAdapter = true)
data class NumerologyYoga(
    val name: String = "",
    val sanskrit: String = "",
    @Json(name = "numbers_involved") val numbersInvolved: List<Int> = emptyList(),
    val rarity: String = "",
    val meaning: String = "",
    val effect: String = "",
    val advice: String = ""
)

@JsonClass(generateAdapter = true)
data class NumerologyYogas(
    val detected: List<NumerologyYoga> = emptyList(),
    val summary: String = ""
)

@JsonClass(generateAdapter = true)
data class FamousPersonality(
    val name: String = "",
    val field: String = "",
    val note: String = ""
)

@JsonClass(generateAdapter = true)
data class FamousPersonalities(
    val indian: List<FamousPersonality> = emptyList(),
    val global: List<FamousPersonality> = emptyList(),
    @Json(name = "common_traits") val commonTraits: String = ""
)

@JsonClass(generateAdapter = true)
data class NumerologyCautions(
    @Json(name = "primary_warning") val primaryWarning: String = "",
    @Json(name = "watch_out_for") val watchOutFor: List<String> = emptyList(),
    @Json(name = "karmic_lesson") val karmicLesson: String = "",
    @Json(name = "health_caution") val healthCaution: String = "",
    @Json(name = "relationship_caution") val relationshipCaution: String = "",
    @Json(name = "financial_caution") val financialCaution: String = ""
)

@JsonClass(generateAdapter = true)
data class NumerologyExcellence(
    @Json(name = "how_to_excel") val howToExcel: List<String> = emptyList(),
    @Json(name = "leverage_strengths") val leverageStrengths: List<String> = emptyList(),
    @Json(name = "success_formula") val successFormula: String = "",
    val affirmation: String = "",
    @Json(name = "daily_practice") val dailyPractice: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CautionsAndExcellence(
    val cautions: NumerologyCautions? = null,
    val excellence: NumerologyExcellence? = null
)

@JsonClass(generateAdapter = true)
data class NumerologyEnhancedAnalysis(
    @Json(name = "planetary_ruler") val planetaryRuler: PlanetaryRuler? = null,
    @Json(name = "life_predictions") val lifePredictions: LifePredictions? = null,
    val yogas: NumerologyYogas? = null,
    @Json(name = "famous_personalities") val famousPersonalities: FamousPersonalities? = null,
    @Json(name = "cautions_and_excellence") val cautionsAndExcellence: CautionsAndExcellence? = null
)

/** `analyses.numerology` — `{error}` only when the numerology calculation failed. Reuses
 * the Pythagorean models since `core_numbers`/`interpretations`/`sangeetmind_personalization`
 * are the same `model_dump()` output POST /numerology/calculate/pythagorean returns. */
@JsonClass(generateAdapter = true)
data class HolisticNumerology(
    val success: Boolean = false,
    val error: String? = null,
    @Json(name = "core_numbers") val coreNumbers: CoreNumbers? = null,
    val interpretations: Map<String, NumberInterpretation> = emptyMap(),
    @Json(name = "sangeetmind_personalization") val sangeetmindPersonalization: SangeetMindPersonalization? = null,
    @Json(name = "enhanced_analysis") val enhancedAnalysis: NumerologyEnhancedAnalysis? = null
)

/** `analyses.astrology` — `vedic_profile` is the same object POST /astro/get-profile returns,
 * so [AstroProfileSummary] is reused; `note` appears instead when no birth time was sent. */
@JsonClass(generateAdapter = true)
data class HolisticAstrology(
    val success: Boolean = false,
    val error: String? = null,
    val note: String? = null,
    @Json(name = "vedic_profile") val vedicProfile: AstroProfileSummary? = null
)

@JsonClass(generateAdapter = true)
data class HolisticAnalyses(
    val numerology: HolisticNumerology? = null,
    val astrology: HolisticAstrology? = null
)

// -- combined_insights (_combine_insights) --

/** One cross-system insight. `personality_synthesis`/`timing_insights`/`life_path_alignment`
 * entries carry `aspect` + `insight`; `spiritual_guidance` entries carry `type` + `guidance`. */
@JsonClass(generateAdapter = true)
data class HolisticInsight(
    val source: String = "",
    val aspect: String? = null,
    val insight: String? = null,
    val type: String? = null,
    val guidance: String? = null
) {
    val text: String get() = insight ?: guidance ?: ""
    val label: String get() = aspect ?: type?.replace('_', ' ')?.replaceFirstChar { it.uppercase() } ?: ""
}

@JsonClass(generateAdapter = true)
data class HolisticHarmony(
    val type: String = "",
    val description: String = "",
    val significance: String? = null
)

@JsonClass(generateAdapter = true)
data class CombinedInsights(
    @Json(name = "personality_synthesis") val personalitySynthesis: List<HolisticInsight> = emptyList(),
    @Json(name = "life_path_alignment") val lifePathAlignment: List<HolisticInsight> = emptyList(),
    @Json(name = "spiritual_guidance") val spiritualGuidance: List<HolisticInsight> = emptyList(),
    @Json(name = "timing_insights") val timingInsights: List<HolisticInsight> = emptyList(),
    @Json(name = "potential_conflicts") val potentialConflicts: List<HolisticHarmony> = emptyList(),
    val harmonies: List<HolisticHarmony> = emptyList()
)

// -- sangeetmind_recommendations (_generate_sangeetmind_recommendations) --

/** Heterogeneous on purpose: numerology entries are `{mood, source, priority}` only; the
 * astrology entry adds `raag` + `reason`. */
@JsonClass(generateAdapter = true)
data class RaagRecommendation(
    val raag: String? = null,
    val mood: String? = null,
    val source: String = "",
    val priority: String = "",
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class AstroPracticeTime(
    val preferred: String = "",
    @Json(name = "based_on") val basedOn: String = ""
)

@JsonClass(generateAdapter = true)
data class HolisticPracticeTime(
    val numerology: PracticeTimeResponse? = null,
    val astrology: AstroPracticeTime? = null
)

@JsonClass(generateAdapter = true)
data class HolisticHabitStyle(
    val numerology: HabitStyleResponse? = null
)

@JsonClass(generateAdapter = true)
data class EmotionalTendency(
    val primary: String = "",
    val secondary: String = "",
    val description: String = "",
    @Json(name = "emotional_strengths") val emotionalStrengths: List<String> = emptyList(),
    @Json(name = "emotional_challenges") val emotionalChallenges: List<String> = emptyList(),
    @Json(name = "balance_practice") val balancePractice: String = ""
)

@JsonClass(generateAdapter = true)
data class HolisticEmotionalGuidance(
    val numerology: EmotionalTendency? = null
)

@JsonClass(generateAdapter = true)
data class CombinedPracticeTime(
    val recommended: String = "flexible",
    val alternative: String? = null,
    val confidence: String = ""
)

@JsonClass(generateAdapter = true)
data class CombinedRecommendation(
    @Json(name = "practice_time") val practiceTime: CombinedPracticeTime? = null,
    @Json(name = "raag_moods") val raagMoods: List<String> = emptyList(),
    @Json(name = "specific_raag") val specificRaag: String? = null
)

@JsonClass(generateAdapter = true)
data class SangeetMindRecommendations(
    @Json(name = "raag_recommendations") val raagRecommendations: List<RaagRecommendation> = emptyList(),
    @Json(name = "practice_time") val practiceTime: HolisticPracticeTime? = null,
    @Json(name = "habit_style") val habitStyle: HolisticHabitStyle? = null,
    @Json(name = "emotional_guidance") val emotionalGuidance: HolisticEmotionalGuidance? = null,
    @Json(name = "daily_practices") val dailyPractices: List<String> = emptyList(),
    @Json(name = "combined_recommendation") val combinedRecommendation: CombinedRecommendation? = null
)

// -- llm_enhanced_narrative --

/** `source` is "gemini" when the LLM actually wrote it, "template" for the deterministic
 * fallback. If Gemini raised, the backend sends `{error, fallback}` with no `narrative`. */
@JsonClass(generateAdapter = true)
data class LlmNarrative(
    val narrative: String? = null,
    val source: String? = null,
    val language: String? = null,
    val reason: String? = null,
    val error: String? = null,
    val fallback: String? = null
) {
    /** Gemini tends to emit markdown emphasis (`**…**`) even when not asked; the app renders
     * plain text, so strip the markers rather than show them literally. */
    val text: String get() = (narrative ?: fallback ?: "").replace("**", "")
    val isLlm: Boolean get() = source == "gemini"
}

@JsonClass(generateAdapter = true)
data class HolisticCombinedResponse(
    val success: Boolean = false,
    val input: HolisticInput? = null,
    val analyses: HolisticAnalyses = HolisticAnalyses(),
    @Json(name = "combined_insights") val combinedInsights: CombinedInsights = CombinedInsights(),
    @Json(name = "sangeetmind_recommendations") val sangeetmindRecommendations: SangeetMindRecommendations = SangeetMindRecommendations(),
    @Json(name = "llm_enhanced_narrative") val llmEnhancedNarrative: LlmNarrative? = null
)
