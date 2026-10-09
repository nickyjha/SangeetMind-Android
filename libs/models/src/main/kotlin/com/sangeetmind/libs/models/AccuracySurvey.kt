package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- Opt-in accuracy survey: /v1/accuracy-survey (app/routes/accuracy_survey_routes.py) ----
// Users may volunteer a few life facts tied to their primary kundli so the engine's verdicts can
// be tested against real outcomes. Every answer is optional (null = prefer not to say).

@JsonClass(generateAdapter = true)
data class SurveyAnswers(
    @Json(name = "marital_status") val maritalStatus: String? = null,
    @Json(name = "marriage_year") val marriageYear: Int? = null,
    val children: Int? = null,
    val career: String? = null,
    val wealth: String? = null,
    val education: String? = null,
    val property: String? = null,
    @Json(name = "settled_abroad") val settledAbroad: Boolean? = null,
    @Json(name = "serious_illness") val seriousIllness: String? = null,
    @Json(name = "birth_time_source") val birthTimeSource: String? = null
)

@JsonClass(generateAdapter = true)
data class SurveyBirth(
    val date: String,
    val time: String,
    val timezone: String,
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class SurveyRequest(
    val consent: Boolean,
    @Json(name = "consent_version") val consentVersion: String,
    val birth: SurveyBirth,
    val answers: SurveyAnswers
)

@JsonClass(generateAdapter = true)
data class SurveyStatus(
    val submitted: Boolean = false,
    @Json(name = "consent_version") val consentVersion: String = "",
    val answers: SurveyAnswers? = null
)

@JsonClass(generateAdapter = true)
data class SurveySaved(val saved: Boolean = false)

@JsonClass(generateAdapter = true)
data class SurveyDeleted(val deleted: Boolean = false)
