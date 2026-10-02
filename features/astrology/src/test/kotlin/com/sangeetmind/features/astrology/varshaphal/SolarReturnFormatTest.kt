package com.sangeetmind.features.astrology.varshaphal

import com.sangeetmind.features.astrology.varshaphal.ui.formatSolarReturn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.util.Locale

class SolarReturnFormatTest {
    @Test
    fun offsetTimeKeepsBirthPlaceWallClock() {
        val out = formatSolarReturn("2026-03-29T18:15:00+05:30", Locale.ENGLISH)
        assertEquals("29 Mar 2026, 06:15 PM", out)
    }

    @Test
    fun localTimeWithoutZone() {
        assertEquals("05 Jan 2027, 09:05 AM", formatSolarReturn("2027-01-05T09:05:00", Locale.ENGLISH))
    }

    @Test
    fun utcConvertsToGivenZone() {
        assertEquals(
            "29 Mar 2026, 06:15 PM",
            formatSolarReturn("2026-03-29T12:45:00Z", Locale.ENGLISH, ZoneId.of("Asia/Kolkata"))
        )
    }

    @Test
    fun hindiLocaleUsesHindiMonth() {
        val out = formatSolarReturn("2026-03-29T18:15:00+05:30", Locale("hi", "IN"))
        assertTrue(out, out.startsWith("29 ") && !out.contains("Mar") && out.contains("2026"))
    }

    @Test
    fun garbageIsReturnedUnchanged() {
        assertEquals("not a date", formatSolarReturn("not a date", Locale.ENGLISH))
    }
}
