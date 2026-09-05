package com.sangeetmind.core.network

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class NominatimPlace(
    val lat: String,
    val lon: String,
    @com.squareup.moshi.Json(name = "display_name") val displayName: String
)

/** OpenStreetMap Nominatim — public geocoding, no API key. Never send backend auth here. */
interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5
    ): List<NominatimPlace>
}
