package com.sangeetmind.features.astrology.dashboard

import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PeriodCountdownTest {
    private val today = LocalDate.of(2026, 10, 7)

    private fun span(daysAhead: Long) = PeriodCountdown.humanize(today, today.plusDays(daysAhead))

    @Test
    fun todayTomorrowAndPast() {
        assertEquals(RelativeSpan.Today, span(0))
        assertEquals(RelativeSpan.Today, span(-3))
        assertEquals(RelativeSpan.Tomorrow, span(1))
    }

    @Test
    fun daysUnderTwoWeeks() {
        // Reference chart: Moon pratyantar ends 9 Oct 2026.
        assertEquals(RelativeSpan.Days(2), PeriodCountdown.humanize(today, LocalDate.of(2026, 10, 9)))
        assertEquals(RelativeSpan.Days(13), span(13))
    }

    @Test
    fun weeksUnderNineWeeks() {
        assertEquals(RelativeSpan.Weeks(2), span(14))
        assertEquals(RelativeSpan.Weeks(3), span(21))
        assertEquals(RelativeSpan.Weeks(8), span(62))
    }

    @Test
    fun monthsThenYears() {
        assertEquals(RelativeSpan.Months(2), span(63))
        // Mercury mahadasha begins 20 Apr 2027.
        assertEquals(RelativeSpan.Months(6), PeriodCountdown.humanize(today, LocalDate.of(2027, 4, 20)))
        assertEquals(RelativeSpan.Months(23), PeriodCountdown.humanize(today, LocalDate.of(2028, 9, 30)))
        assertEquals(RelativeSpan.Years(2), PeriodCountdown.humanize(today, LocalDate.of(2028, 10, 7)))
        assertEquals(RelativeSpan.Years(17), PeriodCountdown.humanize(today, LocalDate.of(2044, 4, 19)))
    }

    @Test
    fun parsesBackendTimestampsInZone() {
        val ist = ZoneId.of("Asia/Kolkata")
        assertEquals(LocalDate.of(2026, 10, 9), PeriodCountdown.localDate("2026-10-09T14:35:53Z", ist))
        // 20:00Z is already the next day in India.
        assertEquals(LocalDate.of(2026, 10, 10), PeriodCountdown.localDate("2026-10-09T20:00:00Z", ist))
        assertEquals(LocalDate.of(2027, 4, 20), PeriodCountdown.localDate("2027-04-20T09:05:53+00:00", ist))
        assertEquals(LocalDate.of(2027, 4, 20), PeriodCountdown.localDate("2027-04-20", ist))
        assertNull(PeriodCountdown.localDate("", ist))
        assertNull(PeriodCountdown.localDate(null, ist))
        assertNull(PeriodCountdown.localDate("soon", ist))
    }

    @Test
    fun shortDateFollowsLocale() {
        assertEquals("9 Oct", PeriodCountdown.shortDate(LocalDate.of(2026, 10, 9), Locale.ENGLISH))
        val hi = PeriodCountdown.shortDate(LocalDate.of(2026, 10, 9), Locale("hi", "IN"))
        assert(hi.startsWith("9 ") && hi != "9 Oct") { hi }
    }
}
