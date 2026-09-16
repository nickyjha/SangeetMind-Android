package com.sangeetmind.core.network

import com.sangeetmind.libs.models.CreateOrderRequest
import com.sangeetmind.libs.models.PremiumStatus
import com.sangeetmind.libs.models.RazorpayOrder
import com.sangeetmind.libs.models.VerifyPaymentRequest
import com.sangeetmind.libs.models.VerifyPaymentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RazorpayApi {
    @POST("v1/razorpay/create-order")
    suspend fun createOrder(@Body body: CreateOrderRequest): RazorpayOrder

    @POST("v1/razorpay/verify-payment")
    suspend fun verifyPayment(@Body body: VerifyPaymentRequest): VerifyPaymentResponse

    @GET("v1/razorpay/premium-status")
    suspend fun getPremiumStatus(): PremiumStatus
}
