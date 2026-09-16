package com.sangeetmind.features.astrology.payments

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.PricingApi
import com.sangeetmind.core.network.RazorpayApi
import com.sangeetmind.core.network.WalletApi
import com.sangeetmind.libs.models.CreateOrderRequest
import com.sangeetmind.libs.models.PremiumStatus
import com.sangeetmind.libs.models.RazorpayOrder
import com.sangeetmind.libs.models.Sku
import com.sangeetmind.libs.models.VerifyPaymentRequest
import com.sangeetmind.libs.models.WalletBalance
import com.sangeetmind.libs.models.WalletDebitRequest
import com.sangeetmind.libs.models.WalletDebitResponse
import com.sangeetmind.libs.models.WalletTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentsRepository @Inject constructor(
    private val pricingApi: PricingApi,
    private val razorpayApi: RazorpayApi,
    private val walletApi: WalletApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getSkus(kind: String? = null): Result<List<Sku>> = withContext(ioDispatcher) {
        try {
            Result.Success(pricingApi.getSkus(kind).skus)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load plans")
        }
    }

    suspend fun createPremiumOrder(skuId: String): Result<RazorpayOrder> = withContext(ioDispatcher) {
        try {
            Result.Success(razorpayApi.createOrder(CreateOrderRequest(skuId)))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not start checkout")
        }
    }

    suspend fun verifyPremiumPayment(
        orderId: String,
        paymentId: String,
        signature: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            razorpayApi.verifyPayment(VerifyPaymentRequest(orderId, paymentId, signature))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not verify payment")
        }
    }

    suspend fun getPremiumStatus(): Result<PremiumStatus> = withContext(ioDispatcher) {
        try {
            Result.Success(razorpayApi.getPremiumStatus())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load premium status")
        }
    }

    suspend fun createWalletOrder(skuId: String): Result<RazorpayOrder> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.createOrder(CreateOrderRequest(skuId)))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not start checkout")
        }
    }

    /**
     * Wallet recharges (unlike premium purchases) settle via the Razorpay webhook alone
     * (see wallet_routes.py) — there is no wallet-specific verify-payment endpoint, so this
     * just re-reads the balance after Checkout succeeds. The caller should poll briefly if
     * the webhook hasn't landed yet.
     */
    suspend fun getWalletBalance(): Result<WalletBalance> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.getBalance())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load wallet balance")
        }
    }

    suspend fun getWalletTransactions(): Result<List<WalletTransaction>> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.getTransactions().transactions)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load transactions")
        }
    }

    suspend fun debitWallet(
        skuId: String,
        idempotencyKey: String,
        amountPaise: Long? = null
    ): Result<WalletDebitResponse> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.debit(WalletDebitRequest(skuId, idempotencyKey, amountPaise)))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not spend wallet balance")
        }
    }
}
