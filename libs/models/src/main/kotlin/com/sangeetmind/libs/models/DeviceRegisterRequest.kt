package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceRegisterRequest(
    val token: String,
    val platform: String = "android",
    val locale: String = "en",
    @Json(name = "sign_topics") val signTopics: List<String> = emptyList(),
    @Json(name = "push_hour_utc") val pushHourUtc: Int = 3,
    /** IANA zone (e.g. Asia/Kolkata) so the server sends "Your day" at local morning. */
    val timezone: String? = null,
    /** Preferred local hour (0-23) for the daily push; the server defaults to 7. */
    @Json(name = "push_hour_local") val pushHourLocal: Int? = null,
    @Json(name = "app_version") val appVersion: String? = null
)
