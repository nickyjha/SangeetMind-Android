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
    fun datesInsideTextAreFormattedAndNothingElseChanges() {
        assertEquals(
            "**Window:** 20 Apr 2027 to 16 Sep 2029.\n- Saturn moves on 29 Mar 2027",
            DateFieldFormat.displayDatesInText(
                "**Window:** 2027-04-20 to 2029-09-16.\n- Saturn moves on 2027-03-29",
                Locale.ENGLISH
            )
        )
        // Hindi month names for the Hindi app language.
        assertEquals("20 अप्रैल 2027 से", DateFieldFormat.displayDatesInText("2027-04-20 से", Locale("hi")))
        // Timestamps, impossible dates, year-month and plain prose are left alone.
        assertEquals("2027-04-20T10:00", DateFieldFormat.displayDatesInText("2027-04-20T10:00", Locale.ENGLISH))
        assertEquals("2027-13-40", DateFieldFormat.displayDatesInText("2027-13-40", Locale.ENGLISH))
        assertEquals("in 2027-04 and 12345-04-20", DateFieldFormat.displayDatesInText("in 2027-04 and 12345-04-20", Locale.ENGLISH))
        assertEquals("", DateFieldFormat.displayDatesInText("", Locale.ENGLISH))
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
