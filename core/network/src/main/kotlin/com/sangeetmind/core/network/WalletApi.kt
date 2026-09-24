package com.sangeetmind.core.network

import com.sangeetmind.libs.models.CreateOrderRequest
import com.sangeetmind.libs.models.RazorpayOrder
import com.sangeetmind.libs.models.WalletBalance
import com.sangeetmind.libs.models.WalletDebitRequest
import com.sangeetmind.libs.models.WalletDebitResponse
import com.sangeetmind.libs.models.WalletReconcileResponse
import com.sangeetmind.libs.models.WalletTransactionsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WalletApi {
    @POST("v1/wallet/create-order")
    suspend fun createOrder(@Body body: CreateOrderRequest): RazorpayOrder

    @POST("v1/wallet/debit")
    suspend fun debit(@Body body: WalletDebitRequest): WalletDebitResponse

    /** Credits paid recharges whose Razorpay webhook never arrived; safe to call repeatedly. */
    @POST("v1/wallet/reconcile")
    suspend fun reconcile(): WalletReconcileResponse

    @GET("v1/wallet/balance")
    suspend fun getBalance(): WalletBalance

    @GET("v1/wallet/transactions")
    suspend fun getTransactions(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): WalletTransactionsResponse
}
