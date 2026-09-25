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
    @Json(name = "analysis_tier") val analysisTier: String? = null
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
