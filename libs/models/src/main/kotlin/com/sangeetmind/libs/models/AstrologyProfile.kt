package com.sangeetmind.libs.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AstrologyProfile(
    val name: String,
    val dateOfBirth: String, // ISO 8601 format
    val timeOfBirth: String, // HH:mm format
    val placeOfBirth: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)

@JsonClass(generateAdapter = true)
data class AstrologyRecommendation(
    val raagIds: List<String>,
    val meditationSessionIds: List<String>,
    val message: String,
    val messageHindi: String? = null
)

