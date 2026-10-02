package com.sangeetmind.features.astrology.referrals

import org.junit.Assert.assertEquals
import org.junit.Test

class ReferralCodesTest {

    @Test
    fun `short codes are trimmed and upper-cased`() {
        assertEquals("NICKY7K2", ReferralCodes.normalizeInput("  nicky7k2 \n"))
        assertEquals("NICKY7K2", ReferralCodes.normalizeInput("nicky 7k2"))
        assertEquals("ASTRO9XYZ", ReferralCodes.normalizeInput("Astro9xyz"))
    }

    @Test
    fun `legacy uid codes keep their case`() {
        val uid = "mROg7hISgBcS8kaXa1y21FZiRKR2"
        assertEquals(uid, ReferralCodes.normalizeInput("  $uid  "))
    }

    @Test
    fun `blank input stays blank`() {
        assertEquals("", ReferralCodes.normalizeInput("   "))
    }
}
