package com.sangeetmind.features.astrology.dashboard

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Days the user committed to the Action of the day, kept on the device (private, no
 * account needed; it does not sync across devices). Holds the last 90 days. */
@Singleton
class ActionStreakStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun days(): Set<String> = prefs.getStringSet(KEY, emptySet()).orEmpty()

    fun isDone(day: LocalDate): Boolean = day.toString() in days()

    fun markDone(day: LocalDate) {
        val cutoff = day.minusDays(KEEP_DAYS)
        val kept = days().filter { runCatching { LocalDate.parse(it) > cutoff }.getOrDefault(false) }
        prefs.edit().putStringSet(KEY, (kept + day.toString()).toSet()).apply()
    }

    /** Consecutive committed days ending today, or yesterday if today isn't done yet. */
    fun streak(today: LocalDate): Int {
        val done = days()
        var day = if (today.toString() in done) today else today.minusDays(1)
        var count = 0
        while (day.toString() in done) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    private companion object {
        const val PREFS = "daily_action"
        const val KEY = "done_days"
        const val KEEP_DAYS = 90L
    }
}
