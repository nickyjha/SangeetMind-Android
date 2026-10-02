package com.sangeetmind.features.astrology.payments

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.firebase.auth.FirebaseAuth
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.features.astrology.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** A Play price for one of our products, as the store formats it for this user ("₹99.00"). */
data class PlayPrice(val productId: String, val formattedPrice: String)

sealed class PlayBillingEvent {
    /** The backend verified and applied the purchase (wallet credited or Premium active). */
    data class Applied(
        val productId: String,
        val creditedPaise: Long?,
        val premiumUntil: String?,
        val duplicate: Boolean
    ) : PlayBillingEvent()

    /** Purchase awaiting payment (e.g. cash at a store); nothing credited yet. */
    data class Pending(val productId: String) : PlayBillingEvent()

    /** Google Play billing cannot be used on this device/account. */
    data object Unavailable : PlayBillingEvent()

    /** [message] is a server/store message to show as-is; otherwise show [fallbackRes]. */
    data class Error(val message: String?, @StringRes val fallbackRes: Int) : PlayBillingEvent()
}

/**
 * Wraps the Play Billing Library (v7) for the whole app: one connection, product prices,
 * purchase flow, and the verify -> consume/acknowledge loop against our backend.
 *
 * Flow for every purchase (new, or found by [restorePurchases] on start):
 *  1. PURCHASED -> POST /v1/play/verify (backend checks with Google, credits idempotently)
 *  2. backend ok -> consume (wallet pack) / acknowledge (subscription)
 *  3. backend error -> leave it unconsumed/unacknowledged so the next start retries
 * PENDING purchases are reported but never sent for credit; user cancellation is silent.
 */
@Singleton
class PlayBillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PaymentsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val connectMutex = Mutex()
    private var connecting: CompletableDeferred<BillingResult>? = null

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val productDetails = mutableMapOf<String, ProductDetails>()
    /** Purchase tokens currently being verified, so a start()+onPurchasesUpdated overlap can't double-post. */
    private val inFlight = mutableSetOf<String>()

    private val _prices = MutableStateFlow<Map<String, PlayPrice>>(emptyMap())
    val prices: StateFlow<Map<String, PlayPrice>> = _prices.asStateFlow()

    /** null until the first connection attempt finishes. */
    private val _available = MutableStateFlow<Boolean?>(null)
    val available: StateFlow<Boolean?> = _available.asStateFlow()

    private val _events = MutableSharedFlow<PlayBillingEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<PlayBillingEvent> = _events.asSharedFlow()

    /** Connect, load prices and recover any unapplied purchases. Safe to call repeatedly. */
    fun start() {
        scope.launch {
            val result = ensureConnected()
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@launch
            queryProducts()
            restorePurchases()
        }
    }

    /**
     * Opens the Play purchase sheet for [productId]. The result arrives through
     * [onPurchasesUpdated] -> [events]; this only reports whether the sheet could be shown.
     */
    suspend fun launchPurchase(activity: Activity, productId: String): Boolean {
        val type = PlayProducts.typeFor(productId) ?: return false
        val connected = ensureConnected()
        if (connected.responseCode != BillingClient.BillingResponseCode.OK) {
            emitBillingFailure(connected)
            return false
        }
        if (productDetails[productId] == null) queryProducts()
        val details = productDetails[productId]
        if (details == null) {
            _events.tryEmit(PlayBillingEvent.Error(null, R.string.payments_error_product_unavailable))
            return false
        }

        val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details)
        if (type == PlayProducts.TYPE_SUBS) {
            val plan = PlayProducts.premiumPlan(productId)
            val offer = details.subscriptionOfferDetails
                ?.filter { plan == null || it.basePlanId == plan.basePlanId }
                // Prefer the plain base plan over any promotional offer.
                ?.sortedBy { if (it.offerId == null) 0 else 1 }
                ?.firstOrNull()
                ?: details.subscriptionOfferDetails?.firstOrNull()
            if (offer == null) {
                _events.tryEmit(PlayBillingEvent.Error(null, R.string.payments_error_product_unavailable))
                return false
            }
            paramsBuilder.setOfferToken(offer.offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(paramsBuilder.build()))
            .apply {
                // Lets the backend tie RTDN events to the user even for tokens it never saw.
                FirebaseAuth.getInstance().currentUser?.uid?.let { setObfuscatedAccountId(it) }
            }
            .build()

        val result = withContext(kotlinx.coroutines.Dispatchers.Main) {
            client.launchBillingFlow(activity, flowParams)
        }
        return when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> true
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // An earlier purchase wasn't consumed/acknowledged yet: apply it now.
                scope.launch { restorePurchases() }
                false
            }
            else -> {
                emitBillingFailure(result)
                false
            }
        }
    }

    /** Queries Play for purchases the app still owns and pushes each through verification. */
    suspend fun restorePurchases() {
        if (ensureConnected().responseCode != BillingClient.BillingResponseCode.OK) return
        val all = mutableListOf<Purchase>()
        for (type in listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS)) {
            val result = client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(type).build()
            )
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                all += result.purchasesList
            } else {
                Log.w(TAG, "queryPurchases($type) failed: ${result.billingResult.debugMessage}")
            }
        }
        processPurchases(all)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val list = purchases.orEmpty()
                scope.launch { processPurchases(list) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit // silent by design
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch { restorePurchases() }
            else -> emitBillingFailure(result)
        }
    }

    // ── internals ────────────────────────────────────────────────────────────────────────

    private suspend fun ensureConnected(): BillingResult {
        if (client.isReady) return ok()
        val deferred = connectMutex.withLock {
            connecting?.takeIf { !it.isCompleted } ?: CompletableDeferred<BillingResult>().also { d ->
                connecting = d
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        d.complete(billingResult)
                    }

                    override fun onBillingServiceDisconnected() {
                        // Next ensureConnected() reconnects; nothing to do here.
                        if (!d.isCompleted) {
                            d.complete(
                                BillingResult.newBuilder()
                                    .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                                    .build()
                            )
                        }
                    }
                })
            }
        }
        val result = deferred.await()
        val isOk = result.responseCode == BillingClient.BillingResponseCode.OK
        _available.value = isOk ||
            (result.responseCode != BillingClient.BillingResponseCode.BILLING_UNAVAILABLE &&
                result.responseCode != BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED)
        if (!isOk) Log.w(TAG, "Billing setup failed: ${result.responseCode} ${result.debugMessage}")
        return result
    }

    private suspend fun queryProducts() {
        val found = mutableMapOf<String, PlayPrice>()
        val queries = listOf(
            BillingClient.ProductType.INAPP to PlayProducts.walletProductIds,
            BillingClient.ProductType.SUBS to PlayProducts.premiumProductIds
        )
        for ((type, ids) in queries) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ids.map {
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(it)
                            .setProductType(type)
                            .build()
                    }
                )
                .build()
            val result = client.queryProductDetails(params)
            if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "queryProductDetails($type) failed: ${result.billingResult.debugMessage}")
                continue
            }
            for (details in result.productDetailsList.orEmpty()) {
                productDetails[details.productId] = details
                priceOf(details)?.let { found[details.productId] = PlayPrice(details.productId, it) }
            }
        }
        if (found.isNotEmpty()) _prices.value = _prices.value + found
    }

    private fun priceOf(details: ProductDetails): String? {
        details.oneTimePurchaseOfferDetails?.formattedPrice?.let { return it }
        val plan = PlayProducts.premiumPlan(details.productId)
        val offer = details.subscriptionOfferDetails
            ?.filter { plan == null || it.basePlanId == plan.basePlanId }
            ?.sortedBy { if (it.offerId == null) 0 else 1 }
            ?.firstOrNull()
            ?: details.subscriptionOfferDetails?.firstOrNull()
        // Last pricing phase is the recurring price (earlier ones are trials / intro offers).
        return offer?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
    }

    private suspend fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            val productId = PlayProducts.knownProductIn(purchase.products) ?: continue
            when (PlayPurchaseClassifier.classify(purchase.products, purchase.purchaseState, purchase.isAcknowledged)) {
                PurchaseAction.PENDING -> _events.tryEmit(PlayBillingEvent.Pending(productId))
                PurchaseAction.IGNORE -> Unit
                PurchaseAction.VERIFY -> verifyAndFinish(productId, purchase)
            }
        }
    }

    private suspend fun verifyAndFinish(productId: String, purchase: Purchase) {
        val token = purchase.purchaseToken
        val type = PlayProducts.typeFor(productId) ?: return
        synchronized(inFlight) { if (!inFlight.add(token)) return }
        try {
            when (val verified = repository.verifyPlayPurchase(productId, token, type)) {
                is Result.Success -> {
                    val response = verified.data
                    if (!response.ok) {
                        _events.tryEmit(PlayBillingEvent.Error(null, R.string.payments_error_verify_payment))
                        return
                    }
                    val finished = if (type == PlayProducts.TYPE_INAPP) {
                        client.consumePurchase(ConsumeParams.newBuilder().setPurchaseToken(token).build())
                            .billingResult
                    } else if (!purchase.isAcknowledged) {
                        client.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
                        )
                    } else {
                        ok()
                    }
                    if (finished.responseCode != BillingClient.BillingResponseCode.OK) {
                        // Backend has it; Play will hand the purchase back next start and the
                        // backend answers duplicate=true, so we just consume/ack then.
                        Log.w(TAG, "consume/ack failed for $productId: ${finished.debugMessage}")
                    }
                    _events.tryEmit(
                        PlayBillingEvent.Applied(
                            productId = productId,
                            creditedPaise = response.creditedPaise,
                            premiumUntil = response.premiumUntil,
                            duplicate = response.duplicate
                        )
                    )
                }
                is Result.Error -> {
                    // Not consumed/acknowledged on purpose: retried on next start().
                    _events.tryEmit(
                        PlayBillingEvent.Error(verified.message, R.string.payments_error_verify_payment)
                    )
                }
                is Result.Loading -> Unit
            }
        } finally {
            synchronized(inFlight) { inFlight.remove(token) }
        }
    }

    private fun emitBillingFailure(result: BillingResult) {
        val event = when (result.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> PlayBillingEvent.Unavailable
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.NETWORK_ERROR ->
                PlayBillingEvent.Error(null, R.string.payments_error_play_connection)
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                PlayBillingEvent.Error(null, R.string.payments_error_product_unavailable)
            else -> PlayBillingEvent.Error(
                result.debugMessage.takeIf { it.isNotBlank() },
                R.string.payments_error_payment_failed
            )
        }
        _events.tryEmit(event)
    }

    private fun ok(): BillingResult =
        BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()

    private companion object {
        const val TAG = "PlayBilling"
    }
}
