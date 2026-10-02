package com.sangeetmind.features.astrology.match

import com.sangeetmind.libs.models.Kundli
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultMatchPairTest {
    private fun k(id: String, primary: Boolean = false) = Kundli(
        id = id, userId = "u", fullName = id, birthDate = "1990-01-01", birthTime = "06:00",
        birthPlace = "Delhi", latitude = 28.6, longitude = 77.2, timezone = "Asia/Kolkata",
        isPrimary = primary, createdAt = "2026-01-01"
    )

    @Test
    fun fewerThanTwoKundlisSelectsNothing() {
        val (a, b) = defaultMatchPair(listOf(k("1", primary = true)))
        assertNull(a)
        assertNull(b)
    }

    @Test
    fun primaryIsPersonAAndFirstOtherIsPersonB() {
        val (a, b) = defaultMatchPair(listOf(k("1"), k("2", primary = true), k("3")))
        assertEquals("2", a?.id)
        assertEquals("1", b?.id)
    }

    @Test
    fun noPrimaryFallsBackToFirstTwo() {
        val (a, b) = defaultMatchPair(listOf(k("1"), k("2")))
        assertEquals("1", a?.id)
        assertEquals("2", b?.id)
    }

    @Test
    fun existingSelectionsAreKept() {
        val list = listOf(k("1", primary = true), k("2"), k("3"))
        val (a, b) = defaultMatchPair(list, currentA = k("3"), currentB = k("2"))
        assertEquals("3", a?.id)
        assertEquals("2", b?.id)
    }

    @Test
    fun personBAlreadyPrimaryPicksAnotherForA() {
        val list = listOf(k("1", primary = true), k("2"))
        val (a, b) = defaultMatchPair(list, currentB = k("1"))
        assertEquals("2", a?.id)
        assertEquals("1", b?.id)
    }
}
