package com.sangeetmind.features.astrology.payments

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.network.PricingApi
import com.sangeetmind.core.network.RazorpayApi
import com.sangeetmind.core.network.WalletApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.CreateOrderRequest
import com.sangeetmind.libs.models.PremiumStatus
import com.sangeetmind.libs.models.RazorpayOrder
import com.sangeetmind.libs.models.Sku
import com.sangeetmind.libs.models.VerifyPaymentRequest
import com.sangeetmind.libs.models.WalletBalance
import com.sangeetmind.libs.models.WalletDebitRequest
import com.sangeetmind.libs.models.WalletDebitResponse
import com.sangeetmind.libs.models.WalletReconcileResponse
import com.sangeetmind.libs.models.WalletTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentsRepository @Inject constructor(
    private val pricingApi: PricingApi,
    private val razorpayApi: RazorpayApi,
    private val walletApi: WalletApi,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getSkus(kind: String? = null): Result<List<Sku>> = withContext(ioDispatcher) {
        try {
            Result.Success(pricingApi.getSkus(kind, lang = languageManager.current.code).skus)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_load_plans))
        }
    }

    suspend fun createPremiumOrder(skuId: String): Result<RazorpayOrder> = withContext(ioDispatcher) {
        try {
            Result.Success(razorpayApi.createOrder(CreateOrderRequest(skuId)))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_start_checkout))
        }
    }

    /**
     * Server-side confirmation of a Checkout success (POST razorpay/verify-payment). The
     * backend looks up the order's purpose and either activates Premium or credits the
     * wallet, idempotently, so this is safe to call for both and alongside the webhook.
     */
    suspend fun verifyPayment(
        orderId: String,
        paymentId: String,
        signature: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            razorpayApi.verifyPayment(VerifyPaymentRequest(orderId, paymentId, signature))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_verify_payment))
        }
    }

    suspend fun getPremiumStatus(): Result<PremiumStatus> = withContext(ioDispatcher) {
        try {
            Result.Success(razorpayApi.getPremiumStatus())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_load_premium_status))
        }
    }

    suspend fun createWalletOrder(skuId: String): Result<RazorpayOrder> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.createOrder(CreateOrderRequest(skuId)))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_start_checkout))
        }
    }

    /** Asks the server to credit any paid recharge it missed (webhook lost or late). */
    suspend fun reconcileWallet(): Result<WalletReconcileResponse> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.reconcile())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_load_wallet_balance))
        }
    }

    suspend fun getWalletBalance(): Result<WalletBalance> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.getBalance())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_load_wallet_balance))
        }
    }

    suspend fun getWalletTransactions(): Result<List<WalletTransaction>> = withContext(ioDispatcher) {
        try {
            Result.Success(walletApi.getTransactions().transactions)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_load_transactions))
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
            Result.Error(e, e.message ?: context.getString(R.string.payments_error_spend_wallet))
        }
    }
}
