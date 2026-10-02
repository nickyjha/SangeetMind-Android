package com.sangeetmind.features.astrology.payments

/**
 * Google Play product catalog for everything sold inside the Android app. Mirrors
 * `app/services/play_billing_service.py` on the backend — keep both lists in sync and keep
 * the ids identical to the Play Console (in-app products + subscription base plans).
 *
 * Pure Kotlin (no Billing Library types) so it is unit-testable on the JVM.
 */
object PlayProducts {
    const val TYPE_INAPP = "inapp"
    const val TYPE_SUBS = "subs"

    /** Consumable wallet packs: product id -> paise credited by the backend. */
    val walletPacks: List<WalletPack> = listOf(
        WalletPack("wallet_99", 9_900),
        WalletPack("wallet_199", 19_900),
        WalletPack("wallet_499", 49_900),
        WalletPack("wallet_999", 99_900)
    )

    /** Premium subscriptions: product id + base plan id as set up in the Play Console. */
    val premiumPlans: List<PremiumPlan> = listOf(
        PremiumPlan("premium_monthly", basePlanId = "monthly", periodDays = 30, listPricePaise = 19_900),
        PremiumPlan("premium_yearly", basePlanId = "yearly", periodDays = 365, listPricePaise = 149_900)
    )

    val walletProductIds: List<String> = walletPacks.map { it.productId }
    val premiumProductIds: List<String> = premiumPlans.map { it.productId }

    fun paiseFor(productId: String): Long? = walletPacks.firstOrNull { it.productId == productId }?.paise

    fun premiumPlan(productId: String): PremiumPlan? = premiumPlans.firstOrNull { it.productId == productId }

    fun isWalletPack(productId: String): Boolean = productId in walletProductIds

    fun isPremium(productId: String): Boolean = productId in premiumProductIds

    /** "inapp" / "subs" for a known product id, null for anything we don't sell. */
    fun typeFor(productId: String): String? = when {
        isWalletPack(productId) -> TYPE_INAPP
        isPremium(productId) -> TYPE_SUBS
        else -> null
    }

    /** The first product id in a Play purchase that we actually sell (purchases can list several). */
    fun knownProductIn(productIds: List<String>): String? = productIds.firstOrNull { typeFor(it) != null }
}

/** A wallet pack costs exactly what it credits, so [paise] doubles as its list price. */
data class WalletPack(val productId: String, val paise: Long)

/**
 * [listPricePaise] is the catalogue price (backend pricing.py) shown only when Play has not
 * returned its localized price in time; the Play price always wins once it arrives.
 */
data class PremiumPlan(
    val productId: String,
    val basePlanId: String,
    val periodDays: Int,
    val listPricePaise: Long = 0
)

/** What to do with a purchase the Billing Library hands us. Mirrors Purchase.PurchaseState ints. */
enum class PurchaseAction {
    /** PURCHASED and not yet applied: send to the backend, then consume / acknowledge. */
    VERIFY,
    /** PENDING (e.g. cash at a store): tell the user, never credit. */
    PENDING,
    /** Already acknowledged subscription, unknown product, or an unspecified state: nothing to do. */
    IGNORE
}

object PlayPurchaseClassifier {
    // Purchase.PurchaseState values (com.android.billingclient.api.Purchase.PurchaseState).
    const val STATE_UNSPECIFIED = 0
    const val STATE_PURCHASED = 1
    const val STATE_PENDING = 2

    /**
     * @param productIds the purchase's product ids
     * @param purchaseState Purchase.getPurchaseState()
     * @param acknowledged Purchase.isAcknowledged()
     */
    fun classify(productIds: List<String>, purchaseState: Int, acknowledged: Boolean): PurchaseAction {
        val productId = PlayProducts.knownProductIn(productIds) ?: return PurchaseAction.IGNORE
        return when (purchaseState) {
            STATE_PENDING -> PurchaseAction.PENDING
            STATE_PURCHASED -> {
                // Consumables are consumed once credited, so any one still present is unapplied.
                // Subscriptions stay in the purchase list for their whole life; only an
                // unacknowledged one needs the backend (renewals arrive via RTDN server-side).
                if (PlayProducts.isPremium(productId) && acknowledged) PurchaseAction.IGNORE
                else PurchaseAction.VERIFY
            }
            else -> PurchaseAction.IGNORE
        }
    }
}
