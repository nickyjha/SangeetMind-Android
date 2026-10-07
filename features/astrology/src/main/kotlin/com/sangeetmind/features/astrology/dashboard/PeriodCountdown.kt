package com.sangeetmind.features.astrology.dashboard

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** How far away a dasha boundary is, coarsened for a one-line countdown. */
sealed interface RelativeSpan {
    data object Today : RelativeSpan
    data object Tomorrow : RelativeSpan
    data class Days(val count: Long) : RelativeSpan
    data class Weeks(val count: Long) : RelativeSpan
    data class Months(val count: Long) : RelativeSpan
    data class Years(val count: Long) : RelativeSpan
}

/** Pure helpers behind the Home "Your current period" card (unit-tested). */
object PeriodCountdown {
    /**
     * Days under 14 → days, under 9 weeks → weeks, otherwise months (years from 2 years on,
     * so a mahadasha 15 years out doesn't read "180 months"). Past dates read as today.
     */
    fun humanize(today: LocalDate, target: LocalDate): RelativeSpan {
        val days = ChronoUnit.DAYS.between(today, target)
        return when {
            days <= 0 -> RelativeSpan.Today
            days == 1L -> RelativeSpan.Tomorrow
            days < 14 -> RelativeSpan.Days(days)
            days < 63 -> RelativeSpan.Weeks(days / 7)
            else -> {
                val months = ChronoUnit.MONTHS.between(today, target).coerceAtLeast(2)
                if (months >= 24) RelativeSpan.Years(ChronoUnit.YEARS.between(today, target))
                else RelativeSpan.Months(months)
            }
        }
    }

    /** A backend timestamp ("2026-10-09T14:35:53Z") as the calendar day in [zone]. */
    fun localDate(ts: String?, zone: ZoneId = ZoneId.systemDefault()): LocalDate? {
        val trimmed = ts?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return runCatching { Instant.parse(trimmed).atZone(zone).toLocalDate() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(trimmed).atZoneSameInstant(zone).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDate.parse(trimmed.take(10)) }.getOrNull()
    }

    /** "9 Oct" in the app language (the year is implied for a near boundary). */
    fun shortDate(date: LocalDate, locale: Locale): String =
        date.format(DateTimeFormatter.ofPattern("d MMM", locale))
}
