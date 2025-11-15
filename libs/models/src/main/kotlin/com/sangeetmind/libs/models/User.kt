package com.sangeetmind.libs.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val preferences: UserPreferences? = null
)

@JsonClass(generateAdapter = true)
data class UserPreferences(
    val language: String = "en",
    val playbackQuality: PlaybackQuality = PlaybackQuality.HIGH,
    val downloadOnWifiOnly: Boolean = true,
    val notificationsEnabled: Boolean = true
)

enum class PlaybackQuality {
    LOW, MEDIUM, HIGH
}

