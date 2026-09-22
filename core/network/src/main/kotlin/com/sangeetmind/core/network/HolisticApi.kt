package com.sangeetmind.core.network

import com.sangeetmind.libs.models.HolisticCombinedRequest
import com.sangeetmind.libs.models.HolisticCombinedResponse
import retrofit2.http.Body
import retrofit2.http.POST

/** app/routes/holistic.py — mounted at the root (no /v1 prefix), like /numerology. */
interface HolisticApi {
    @POST("holistic/combined")
    suspend fun getCombined(@Body body: HolisticCombinedRequest): HolisticCombinedResponse
}
