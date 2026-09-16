package com.sangeetmind.core.network

import com.sangeetmind.libs.models.DeviceRegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceApi {
    @POST("v1/users/me/devices")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest): Map<String, Boolean>
}
