package com.sangeetmind.libs.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Raag(
    val id: String,
    val name: String,
    val nameHindi: String? = null,
    val description: String,
    val artworkUrl: String? = null,
    val audioUrl: String,
    val durationSeconds: Int,
    val tags: List<String> = emptyList(),
    val timeOfDay: TimeOfDay? = null,
    val mood: Mood? = null,
    val intensity: Intensity? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false
)

enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING, NIGHT, ANYTIME
}

enum class Mood {
    PEACEFUL, ENERGETIC, DEVOTIONAL, ROMANTIC, MELANCHOLIC, JOYFUL
}

enum class Intensity {
    LOW, MEDIUM, HIGH
}

