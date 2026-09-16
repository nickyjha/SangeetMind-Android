package com.sangeetmind.features.astrology.payments

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class RazorpayResult {
    data class Success(val orderId: String, val paymentId: String, val signature: String) : RazorpayResult()
    data class Failure(val code: Int, val description: String?) : RazorpayResult()
}

/**
 * Razorpay's Checkout SDK reports results via an Activity callback interface, not a
 * suspend/coroutine API — MainActivity implements that interface and forwards results
 * here so a Compose ViewModel (which doesn't have an Activity callback) can collect them.
 */
@Singleton
class RazorpayResultBus @Inject constructor() {
    private val _results = MutableSharedFlow<RazorpayResult>(extraBufferCapacity = 1)
    val results: SharedFlow<RazorpayResult> = _results

    fun emit(result: RazorpayResult) {
        _results.tryEmit(result)
    }
}
