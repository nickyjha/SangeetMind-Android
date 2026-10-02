package com.sangeetmind.features.astrology.payments

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayProductsTest {

    @Test
    fun `wallet packs map to the paise the backend credits`() {
        assertEquals(9_900L, PlayProducts.paiseFor("wallet_99"))
        assertEquals(19_900L, PlayProducts.paiseFor("wallet_199"))
        assertEquals(49_900L, PlayProducts.paiseFor("wallet_499"))
        assertEquals(99_900L, PlayProducts.paiseFor("wallet_999"))
        assertNull(PlayProducts.paiseFor("premium_monthly"))
        assertNull(PlayProducts.paiseFor("wallet_500"))
    }

    @Test
    fun `product ids and base plans match the Play Console setup`() {
        assertEquals(listOf("wallet_99", "wallet_199", "wallet_499", "wallet_999"), PlayProducts.walletProductIds)
        assertEquals(listOf("premium_monthly", "premium_yearly"), PlayProducts.premiumProductIds)
        assertEquals("monthly", PlayProducts.premiumPlan("premium_monthly")?.basePlanId)
        assertEquals("yearly", PlayProducts.premiumPlan("premium_yearly")?.basePlanId)
    }

    @Test
    fun `type is inapp for packs, subs for premium, null otherwise`() {
        assertEquals("inapp", PlayProducts.typeFor("wallet_99"))
        assertEquals("subs", PlayProducts.typeFor("premium_yearly"))
        assertNull(PlayProducts.typeFor("sangeet_plus_monthly"))
        assertTrue(PlayProducts.isWalletPack("wallet_999"))
        assertFalse(PlayProducts.isPremium("wallet_999"))
    }

    @Test
    fun `knownProductIn skips ids we do not sell`() {
        assertEquals("wallet_199", PlayProducts.knownProductIn(listOf("legacy_thing", "wallet_199")))
        assertNull(PlayProducts.knownProductIn(listOf("legacy_thing")))
        assertNull(PlayProducts.knownProductIn(emptyList()))
    }
}

class PlayPurchaseClassifierTest {

    private fun classify(id: String, state: Int, acknowledged: Boolean = false) =
        PlayPurchaseClassifier.classify(listOf(id), state, acknowledged)

    @Test
    fun `purchased wallet pack is verified even if acknowledged`() {
        assertEquals(PurchaseAction.VERIFY, classify("wallet_99", PlayPurchaseClassifier.STATE_PURCHASED))
        // Consumables stay owned until consumed, so an acknowledged one is still unapplied.
        assertEquals(
            PurchaseAction.VERIFY,
            classify("wallet_99", PlayPurchaseClassifier.STATE_PURCHASED, acknowledged = true)
        )
    }

    @Test
    fun `purchased subscription is verified only until acknowledged`() {
        assertEquals(PurchaseAction.VERIFY, classify("premium_monthly", PlayPurchaseClassifier.STATE_PURCHASED))
        assertEquals(
            PurchaseAction.IGNORE,
            classify("premium_monthly", PlayPurchaseClassifier.STATE_PURCHASED, acknowledged = true)
        )
    }

    @Test
    fun `pending purchases are reported, never verified`() {
        assertEquals(PurchaseAction.PENDING, classify("wallet_499", PlayPurchaseClassifier.STATE_PENDING))
        assertEquals(PurchaseAction.PENDING, classify("premium_yearly", PlayPurchaseClassifier.STATE_PENDING))
    }

    @Test
    fun `unknown products and unspecified state are ignored`() {
        assertEquals(PurchaseAction.IGNORE, classify("something_else", PlayPurchaseClassifier.STATE_PURCHASED))
        assertEquals(PurchaseAction.IGNORE, classify("wallet_99", PlayPurchaseClassifier.STATE_UNSPECIFIED))
        assertEquals(PurchaseAction.IGNORE, classify("wallet_99", 99))
    }
}
