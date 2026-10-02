package com.sangeetmind.libs.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** POST /v1/play/verify — a Google Play purchase the app wants the server to apply. */
@JsonClass(generateAdapter = true)
data class PlayVerifyRequest(
    @Json(name = "product_id") val productId: String,
    @Json(name = "purchase_token") val purchaseToken: String,
    @Json(name = "package_name") val packageName: String,
    /** "inapp" (wallet packs) or "subs" (Premium). */
    val type: String
)

/**
 * Server result. Exactly one of [creditedPaise] / [premiumUntil] is set depending on the
 * product; [duplicate] means the token had already been applied (safe to consume/ack again).
 */
@JsonClass(generateAdapter = true)
data class PlayVerifyResponse(
    val ok: Boolean,
    val duplicate: Boolean = false,
    @Json(name = "credited_paise") val creditedPaise: Long? = null,
    @Json(name = "premium_until") val premiumUntil: String? = null,
    val balance: Long? = null
)
