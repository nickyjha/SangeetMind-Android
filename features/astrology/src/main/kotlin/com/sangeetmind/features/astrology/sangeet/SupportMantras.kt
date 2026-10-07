package com.sangeetmind.features.astrology.sangeet

import androidx.annotation.StringRes
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.SupportMantra

/**
 * Helpers for the backend's `support_mantras` (personal graha beej mantras): which one is
 * "Today's Geet" and the gentle line explaining why it was picked.
 */
object SupportMantras {

    /** Only mantras this app version can open in the japa picker, in server order. */
    fun usable(list: List<SupportMantra>): List<SupportMantra> =
        list.filter { JapaMantras.byId(it.mantraId)?.graha != null }
            .distinctBy { it.mantraId }

    /**
     * Today's pick: rotates daily through [list] and stays the same all day.
     * [dayOfYear] is 1-based (java.time.LocalDate.dayOfYear).
     */
    fun forDay(list: List<SupportMantra>, dayOfYear: Int): SupportMantra? =
        if (list.isEmpty()) null else list[Math.floorMod(dayOfYear, list.size)]

    /** Reason line string: weak/running take the planet; upcoming takes planet + date. */
    @StringRes
    fun reasonRes(reason: String): Int? = when (reason) {
        SupportMantra.REASON_WEAK -> R.string.dashboard_geet_reason_weak
        SupportMantra.REASON_RUNNING -> R.string.dashboard_geet_reason_running
        SupportMantra.REASON_UPCOMING -> R.string.dashboard_geet_reason_upcoming
        SupportMantra.REASON_PROTECTION -> R.string.dashboard_geet_reason_protection
        else -> null
    }
}
