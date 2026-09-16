package com.sangeetmind.core.network

import com.sangeetmind.libs.models.SkuListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PricingApi {
    @GET("v1/pricing/skus")
    suspend fun getSkus(@Query("kind") kind: String? = null): SkuListResponse
}
