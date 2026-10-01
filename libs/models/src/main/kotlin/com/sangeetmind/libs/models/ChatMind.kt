package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- Shared birth-details request shape used by /llm/chat, /llm/career, /llm/strengths ----

@JsonClass(generateAdapter = true)
data class LlmBirthDetails(
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val timezone: String? = null,
    val place: String? = "",
    val lat: Double,
    val lon: Double
)

// ---- ChatMind: POST /llm/chat ----

@JsonClass(generateAdapter = true)
data class ChatMindRequest(
    @Json(name = "birth_details") val birthDetails: LlmBirthDetails,
    val question: String,
    val lang: String = "en",
    @Json(name = "analysis_tier") val analysisTier: String? = null,
    // pandit (default) | counsellor | analyst | friend
    val persona: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatCostEstimate(
    val model: String,
    @Json(name = "input_tokens") val inputTokens: Int,
    @Json(name = "output_tokens") val outputTokens: Int,
    @Json(name = "total_tokens") val totalTokens: Int,
    @Json(name = "cost_usd") val costUsd: Double,
    @Json(name = "cost_inr") val costInr: Double,
    @Json(name = "cost_usd_display") val costUsdDisplay: String,
    @Json(name = "cost_inr_display") val costInrDisplay: String,
    val note: String
)

@JsonClass(generateAdapter = true)
data class ChatMindResponse(
    val answer: String? = null,
    val persona: String = "pandit",
    @Json(name = "context_focus") val contextFocus: String = "general",
    @Json(name = "context_blocks") val contextBlocks: List<String> = emptyList(),
    @Json(name = "cost_estimate") val costEstimate: ChatCostEstimate,
    val error: String? = null
)

// ---- Career reading: POST /llm/career ----
// `birth_details` here is a loose dict server-side (no field aliasing) — snake_case keys.

@JsonClass(generateAdapter = true)
data class CareerBirthDetails(
    val date: String,
    val time: String,
    val timezone: String? = null,
    val place: String? = "",
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class CareerReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class CareerReadingResponse(
    val ok: Boolean,
    // Gemini-generated free-form JSON — no fixed schema server-side, flattened for display.
    val analysis: Map<String, Any>? = null,
    @Json(name = "raw_model_text") val rawModelText: String? = null,
    @Json(name = "cost_estimate") val costEstimate: Map<String, Any>? = null,
    val error: String? = null
)

// ---- Strengths reading: POST /llm/strengths ----

@JsonClass(generateAdapter = true)
data class StrengthsBirthDetails(
    val date: String,
    val time: String,
    val place: String? = "",
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class StrengthsReadingRequest(
    @Json(name = "birth_details") val birthDetails: StrengthsBirthDetails,
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class RemedyResponse(
    val raag: String,
    @Json(name = "mantra_title") val mantraTitle: String,
    @Json(name = "mantra_text") val mantraText: List<String>,
    val why: String? = null
)

@JsonClass(generateAdapter = true)
data class StrengthsReadingResponse(
    @Json(name = "cache_status") val cacheStatus: String,
    @Json(name = "chart_summary") val chartSummary: String? = null,
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val remedies: List<RemedyResponse> = emptyList(),
    val error: String? = null
)

/** Best-effort flattening of Gemini's free-form career analysis JSON into readable text. */
fun Map<String, Any>.flattenToReadableText(indent: String = ""): String =
    entries.joinToString("\n") { (key, value) ->
        val label = "$indent${key.replace('_', ' ').replaceFirstChar { it.uppercase() }}:"
        when (value) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val nested = (value as Map<String, Any>).flattenToReadableText("$indent  ")
                "$label\n$nested"
            }
            is List<*> -> "$label ${value.joinToString(", ")}"
            else -> "$label $value"
        }
    }

// ---- Marriage reading: POST /llm/marriage ----
// Timing windows are computed server-side; Gemini only explains them (by window id), so
// every date here comes from the calculation (app/services/llm_marriage_service.py).

@JsonClass(generateAdapter = true)
data class MarriageReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    @Json(name = "marital_status") val maritalStatus: String, // "single" | "married"
    val lang: String = "en"
)

/** One timing window: `kind` is "marriage" (single), "supportive" or "sensitive" (married);
 * `strength` is "strong" or "moderate"; dates are "YYYY-MM-DD". */
@JsonClass(generateAdapter = true)
data class MarriageTiming(
    val id: String = "",
    val start: String = "",
    val end: String = "",
    val kind: String = "",
    val strength: String = "",
    val mahadasha: String = "",
    val antardasha: String = "",
    val why: String = ""
)

@JsonClass(generateAdapter = true)
data class MarriageRemedy(
    val remedy: String = "",
    @Json(name = "for_planet") val forPlanet: String = ""
)

@JsonClass(generateAdapter = true)
data class MarriageReading(
    val summary: String = "",
    @Json(name = "spouse_nature") val spouseNature: String = "",
    @Json(name = "relationship_strengths") val relationshipStrengths: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    @Json(name = "manglik_note") val manglikNote: String = "",
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MarriageReadingResponse(
    val ok: Boolean,
    @Json(name = "marital_status") val maritalStatus: String = "single",
    val reading: MarriageReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Children (santaan) reading: POST /llm/children ----
// Same shape as marriage: windows come from the calculation (5th lord, Jupiter, Putrakaraka,
// 9th lord, D7 5th lord antardashas + Jupiter/Saturn double transit), Gemini only explains.
// Timing `kind` is "children" (planning) or "supportive" (parent).

@JsonClass(generateAdapter = true)
data class ChildrenReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "planning" | "parent"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class ChildrenReading(
    val summary: String = "",
    @Json(name = "children_nature") val childrenNature: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChildrenReadingResponse(
    val ok: Boolean,
    val status: String = "planning",
    val reading: ChildrenReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Foreign travel / settlement reading: POST /llm/foreign ----
// Windows from 12th lord, 9th lord, Rahu, 9th/12th occupants antardashas + double transit.
// Timing `kind` is "abroad" (planning) or "supportive" (already abroad).

@JsonClass(generateAdapter = true)
data class ForeignReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "planning" | "abroad"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class ForeignReading(
    val summary: String = "",
    @Json(name = "abroad_outlook") val abroadOutlook: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ForeignReadingResponse(
    val ok: Boolean,
    val status: String = "planning",
    val reading: ForeignReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Wealth (dhana) reading: POST /llm/wealth ----
// Windows from 2nd lord, 11th lord, Jupiter, 9th lord, 2nd/11th occupants and dhana-yoga
// planets + double transit over the 2nd/11th. Timing `kind` is always "wealth".

@JsonClass(generateAdapter = true)
data class WealthReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "job" | "business"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class WealthReading(
    val summary: String = "",
    @Json(name = "money_nature") val moneyNature: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WealthReadingResponse(
    val ok: Boolean,
    val status: String = "job",
    val reading: WealthReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Career questions: POST /llm/career-question ----
// question: job_change | promotion | govt_private | job_business. facts.verdict is the
// answer counted from classical indicators; timing `kind` is always "career".

@JsonClass(generateAdapter = true)
data class CareerQuestionRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val question: String,
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class CareerQuestionReading(
    val summary: String = "",
    val outlook: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CareerQuestionFacts(
    val verdict: String = ""
)

@JsonClass(generateAdapter = true)
data class CareerQuestionResponse(
    val ok: Boolean,
    val question: String = "job_change",
    val reading: CareerQuestionReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val facts: CareerQuestionFacts = CareerQuestionFacts(),
    val error: String? = null
)

// ---- Property / vehicle reading: POST /llm/property ----
// status "property" (4th house, Mars, Moon, D4) or "vehicle" (Venus, 4th house, D16).
// Timing `kind` equals the status.

@JsonClass(generateAdapter = true)
data class PropertyReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "property" | "vehicle"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class PropertyReading(
    val summary: String = "",
    val outlook: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PropertyReadingResponse(
    val ok: Boolean,
    val status: String = "property",
    val reading: PropertyReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Education reading: POST /llm/education ----
// status "student" (4th/5th) or "higher" (5th/9th); Mercury, Jupiter, D24.
// Timing `kind` is always "education".

@JsonClass(generateAdapter = true)
data class EducationReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "student" | "higher"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class EducationReading(
    val summary: String = "",
    val outlook: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class EducationReadingResponse(
    val ok: Boolean,
    val status: String = "student",
    val reading: EducationReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Debt / disputes reading: POST /llm/debt ----
// status "debt" or "dispute"; the 6th house and lord, upachaya malefics, Jupiter.
// Timing `kind` equals the status.

@JsonClass(generateAdapter = true)
data class DebtReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "debt" | "dispute"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class DebtReading(
    val summary: String = "",
    val outlook: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DebtReadingResponse(
    val ok: Boolean,
    val status: String = "debt",
    val reading: DebtReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Relationship reading: POST /llm/relationship ----
// status "strain" (repair; never mentions an ending) or "remarriage" (8th/9th lords).
// Timing `kind` is "harmony" or "remarriage".

@JsonClass(generateAdapter = true)
data class RelationshipReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "strain" | "remarriage"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class RelationshipReading(
    val summary: String = "",
    val outlook: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RelationshipReadingResponse(
    val ok: Boolean,
    val status: String = "strain",
    val reading: RelationshipReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)

// ---- Health / wellbeing reading: POST /llm/health ----
// status "body" (lagna, Sun, Mars) or "mind" (Moon, Jupiter, Mercury, 4th).
// Timing `kind` is "vitality" or "calm"; supportive periods only.

@JsonClass(generateAdapter = true)
data class HealthReadingRequest(
    @Json(name = "birth_details") val birthDetails: CareerBirthDetails,
    val status: String, // "body" | "mind"
    val lang: String = "en"
)

@JsonClass(generateAdapter = true)
data class HealthReading(
    val summary: String = "",
    val outlook: String = "",
    val strengths: List<String> = emptyList(),
    @Json(name = "care_points") val carePoints: List<String> = emptyList(),
    val timing: List<MarriageTiming> = emptyList(),
    val advice: List<String> = emptyList(),
    val remedies: List<MarriageRemedy> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HealthReadingResponse(
    val ok: Boolean,
    val status: String = "body",
    val reading: HealthReading? = null,
    val windows: List<MarriageTiming> = emptyList(),
    val error: String? = null
)
