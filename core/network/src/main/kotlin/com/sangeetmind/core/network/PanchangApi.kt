package com.sangeetmind.core.network

import com.sangeetmind.libs.models.EclipseCalendarResponse
import com.sangeetmind.libs.models.PanchangResponse
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
}
