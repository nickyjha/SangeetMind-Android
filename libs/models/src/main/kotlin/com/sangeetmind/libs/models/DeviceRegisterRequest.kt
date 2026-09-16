package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceRegisterRequest(
    val token: String,
    val platform: String = "android",
    val locale: String = "en",
    @Json(name = "sign_topics") val signTopics: List<String> = emptyList(),
    @Json(name = "push_hour_utc") val pushHourUtc: Int = 3
)
