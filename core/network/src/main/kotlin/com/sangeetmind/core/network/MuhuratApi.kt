package com.sangeetmind.core.network

import com.sangeetmind.libs.models.MuhuratRequest
import com.sangeetmind.libs.models.MuhuratResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MuhuratApi {
    @POST("v1/muhurat/find")
    suspend fun findMuhurat(@Body body: MuhuratRequest): MuhuratResponse
}
