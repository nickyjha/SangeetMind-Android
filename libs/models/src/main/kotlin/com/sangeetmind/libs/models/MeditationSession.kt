package com.sangeetmind.libs.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MeditationSession(
    val id: String,
    val title: String,
    val titleHindi: String? = null,
    val description: String,
    val durationMinutes: Int,
    val audioUrl: String,
    val artworkUrl: String? = null,
    val category: MeditationCategory,
    val isGuided: Boolean = true
)

enum class MeditationCategory {
    BREATHING, MINDFULNESS, SLEEP, STRESS_RELIEF, FOCUS, CHAKRA
}

