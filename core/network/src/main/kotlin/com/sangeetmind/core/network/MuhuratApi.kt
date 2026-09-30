package com.sangeetmind.core.network

import com.sangeetmind.libs.models.MuhuratRequest
import com.sangeetmind.libs.models.MuhuratResponse
import com.sangeetmind.libs.models.VivahRequest
import com.sangeetmind.libs.models.VivahResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MuhuratApi {
    @POST("v1/muhurat/find")
    suspend fun findMuhurat(@Body body: MuhuratRequest): MuhuratResponse

    @POST("v1/muhurat/vivah")
    suspend fun findVivah(@Body body: VivahRequest): VivahResponse
}
