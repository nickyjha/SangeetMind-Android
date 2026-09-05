package com.sangeetmind.core.network

import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.KundliCreateRequest
import com.sangeetmind.libs.models.KundliUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface KundliApi {
    @GET("v1/kundli")
    suspend fun listKundlis(): List<Kundli>

    @POST("v1/kundli")
    suspend fun createKundli(@Body body: KundliCreateRequest): Kundli

    @PUT("v1/kundli/{id}")
    suspend fun updateKundli(@Path("id") id: String, @Body body: KundliUpdateRequest): Kundli

    @POST("v1/kundli/{id}/primary")
    suspend fun setPrimaryKundli(@Path("id") id: String): Kundli

    @DELETE("v1/kundli/{id}")
    suspend fun deleteKundli(@Path("id") id: String)
}
