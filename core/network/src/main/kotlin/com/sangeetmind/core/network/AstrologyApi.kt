package com.sangeetmind.core.network

import com.sangeetmind.libs.models.AstroProfileRequest
import com.sangeetmind.libs.models.AstroProfileSummary
import retrofit2.http.Body
import retrofit2.http.POST

interface AstrologyApi {
    @POST("astro/get-profile")
    suspend fun getProfile(@Body body: AstroProfileRequest): AstroProfileSummary
}
