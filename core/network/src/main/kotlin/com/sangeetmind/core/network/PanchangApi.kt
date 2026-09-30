package com.sangeetmind.core.network

import com.sangeetmind.libs.models.DailyScoresRequest
import com.sangeetmind.libs.models.DailyScoresResponse
import com.sangeetmind.libs.models.EclipseCalendarResponse
import com.sangeetmind.libs.models.PanchangResponse
import com.sangeetmind.libs.models.PrashnaRequest
import com.sangeetmind.libs.models.PrashnaResponse
import com.sangeetmind.libs.models.RulingPlanetsResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface PanchangApi {
    @GET("v1/panchang")
    suspend fun getPanchang(
        @Query("date") date: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): PanchangResponse

    @GET("v1/eclipses")
    suspend fun getEclipses(
        @Query("year") year: Int,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("tz") tz: String
    ): EclipseCalendarResponse

    @GET("v1/kp/ruling-planets")
    suspend fun getRulingPlanets(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("tz") tz: String
    ): RulingPlanetsResponse

    @POST("v1/daily/scores")
    suspend fun getDailyScores(@Body body: DailyScoresRequest): DailyScoresResponse

    @POST("v1/kp/prashna")
    suspend fun askPrashna(@Body body: PrashnaRequest): PrashnaResponse
}
