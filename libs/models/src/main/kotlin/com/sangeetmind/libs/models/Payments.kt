package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Sku(
    @Json(name = "skuId") val skuId: String,
    val label: String,
    @Json(name = "pricePaise") val pricePaise: Long,
    val currency: String,
    val kind: String, // "one_time" | "subscription" | "recharge"
    @Json(name = "periodDays") val periodDays: Int? = null
)

@JsonClass(generateAdapter = true)
data class SkuListResponse(val skus: List<Sku>)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "skuId") val skuId: String
)

@JsonClass(generateAdapter = true)
data class RazorpayOrder(
    @Json(name = "orderId") val orderId: String,
    val amount: Long,
    val currency: String,
    @Json(name = "keyId") val keyId: String,
    @Json(name = "skuId") val skuId: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentRequest(
    @Json(name = "razorpayOrderId") val razorpayOrderId: String,
    @Json(name = "razorpayPaymentId") val razorpayPaymentId: String,
    @Json(name = "razorpaySignature") val razorpaySignature: String
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentResponse(
    val purpose: String? = null,
    val credited: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class PremiumStatus(
    val premium: Boolean,
    @Json(name = "endsAt") val endsAt: String? = null
)

@JsonClass(generateAdapter = true)
data class WalletBalance(
    @Json(name = "balancePaise") val balancePaise: Long
)

@JsonClass(generateAdapter = true)
data class WalletTransaction(
    @Json(name = "amountPaise") val amountPaise: Long,
    @Json(name = "razorpayOrderId") val razorpayOrderId: String?,
    val currency: String,
    @Json(name = "createdAt") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class WalletTransactionsResponse(val transactions: List<WalletTransaction>)

@JsonClass(generateAdapter = true)
data class WalletDebitRequest(
    @Json(name = "skuId") val skuId: String,
    @Json(name = "idempotencyKey") val idempotencyKey: String,
    @Json(name = "amountPaise") val amountPaise: Long? = null
)

@JsonClass(generateAdapter = true)
data class WalletDebitResponse(
    val success: Boolean,
    val applied: Boolean,
    val premium: Boolean? = null,
    @Json(name = "balancePaise") val balancePaise: Long
)

/** POST /v1/wallet/reconcile: how many missed recharges were just credited, and the balance. */
@JsonClass(generateAdapter = true)
data class WalletReconcileResponse(
    val credited: Int = 0,
    @Json(name = "balancePaise") val balancePaise: Long = 0
)
