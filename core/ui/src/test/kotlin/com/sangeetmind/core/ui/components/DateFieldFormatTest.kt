package com.sangeetmind.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class DateFieldFormatTest {
    @Test
    fun utcMillisRoundTripKeepsTheSameDay() {
        val d = LocalDate.of(2026, 10, 2)
        val millis = DateFieldFormat.toUtcMillis(d)
        assertEquals(1790899200000L, millis)
        assertEquals(d, DateFieldFormat.fromUtcMillis(millis))
        // A DatePicker selection is UTC midnight; late in that UTC day is still the same date.
        assertEquals(d, DateFieldFormat.fromUtcMillis(millis + 23 * 3600_000L))
    }

    @Test
    fun displayIsFriendly() {
        assertEquals("2 Oct 2026", DateFieldFormat.display("2026-10-02", Locale.ENGLISH))
        assertEquals("garbage", DateFieldFormat.display("garbage", Locale.ENGLISH))
    }

    @Test
    fun timestampShowsTheLocalCalendarDay() {
        val ist = ZoneId.of("Asia/Kolkata")
        // 20:00 UTC on 2 Oct is already 3 Oct in India.
        assertEquals("3 Oct 2026", DateFieldFormat.displayTimestamp("2026-10-02T20:00:00+00:00", Locale.ENGLISH, ist))
        assertEquals("3 Oct 2026", DateFieldFormat.displayTimestamp("2026-10-02T20:00:00Z", Locale.ENGLISH, ist))
        assertEquals("2 Oct 2026", DateFieldFormat.displayTimestamp("2026-10-02 20:00:00", Locale.ENGLISH, ist))
        assertEquals("2 Oct 2026", DateFieldFormat.displayTimestamp("2026-10-02", Locale.ENGLISH, ist))
        assertEquals("yesterday", DateFieldFormat.displayTimestamp("yesterday", Locale.ENGLISH, ist))
    }

    @Test
    fun parseIsoHandlesBlankAndBad() {
        assertNull(DateFieldFormat.parseIso(""))
        assertNull(DateFieldFormat.parseIso("02/10/2026"))
        assertEquals(LocalDate.of(2026, 10, 2), DateFieldFormat.parseIso(" 2026-10-02 "))
    }

    @Test
    fun boundsAreInclusive() {
        val min = LocalDate.of(2026, 10, 1)
        val max = LocalDate.of(2026, 10, 31)
        assertTrue(DateFieldFormat.isAllowed(min, min, max))
        assertTrue(DateFieldFormat.isAllowed(max, min, max))
        assertFalse(DateFieldFormat.isAllowed(min.minusDays(1), min, max))
        assertFalse(DateFieldFormat.isAllowed(max.plusDays(1), min, max))
        assertTrue(DateFieldFormat.isAllowed(min, null, null))
    }
}
