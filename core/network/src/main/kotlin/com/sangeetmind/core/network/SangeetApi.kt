package com.sangeetmind.core.network

import com.sangeetmind.libs.models.DailyRaagResponse
import com.sangeetmind.libs.models.JapaLogRequest
import com.sangeetmind.libs.models.JapaLogResponse
import com.sangeetmind.libs.models.JapaStatsResponse
import com.sangeetmind.libs.models.SoundHealingResponse
import com.sangeetmind.libs.models.VoiceHoroscopeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Methods added by the Sangeet sound features (daily raag, japa, voice horoscope,
// sound healing) — /v1/sangeet/*.
interface SangeetApi {
    @GET("v1/sangeet/raag/daily")
    suspend fun getDailyRaag(@Query("locale") locale: String = "hi"): DailyRaagResponse

    @POST("v1/sangeet/japa/log")
    suspend fun logJapa(@Body body: JapaLogRequest): JapaLogResponse

    @GET("v1/sangeet/japa/stats")
    suspend fun getJapaStats(): JapaStatsResponse

    @GET("v1/sangeet/voice-horoscope/daily")
    suspend fun getVoiceHoroscope(
        @Query("sign") sign: String,
        @Query("lang") lang: String = "hi"
    ): VoiceHoroscopeResponse

    @GET("v1/sangeet/sound-healing")
    suspend fun getSoundHealingSessions(): SoundHealingResponse
}
