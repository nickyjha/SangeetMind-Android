package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** "nicky jha" -> "Nicky Jha" — names are free-typed on the onboarding form with no
 * enforced capitalization, so every screen that displays one runs it through here. */
fun String.toTitleCase(): String = trim()
    .split(Regex("\\s+"))
    .filter { it.isNotEmpty() }
    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }

@JsonClass(generateAdapter = true)
data class Kundli(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "birth_date") val birthDate: String, // YYYY-MM-DD
    @Json(name = "birth_time") val birthTime: String, // HH:MM
    @Json(name = "birth_place") val birthPlace: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @Json(name = "is_primary") val isPrimary: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class KundliCreateRequest(
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "birth_date") val birthDate: String,
    @Json(name = "birth_time") val birthTime: String,
    @Json(name = "birth_place") val birthPlace: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class KundliUpdateRequest(
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "birth_date") val birthDate: String,
    @Json(name = "birth_time") val birthTime: String,
    @Json(name = "birth_place") val birthPlace: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String? = null
)
