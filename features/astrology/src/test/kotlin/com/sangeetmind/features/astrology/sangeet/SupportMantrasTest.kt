package com.sangeetmind.features.astrology.sangeet

import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.SupportMantra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SupportMantrasTest {
    private val list = listOf(
        SupportMantra("Jupiter", "guru_beej", SupportMantra.REASON_WEAK, null, 1),
        SupportMantra("Saturn", "shani_beej", SupportMantra.REASON_RUNNING, null, 2),
        SupportMantra("Mercury", "budh_beej", SupportMantra.REASON_UPCOMING, "2027-04-20", 3),
        SupportMantra("Sun", "surya_beej", SupportMantra.REASON_PROTECTION, null, 4)
    )

    @Test
    fun rotatesDailyThroughTheList() {
        assertEquals("shani_beej", SupportMantras.forDay(list, 1)?.mantraId)
        assertEquals("budh_beej", SupportMantras.forDay(list, 2)?.mantraId)
        assertEquals("surya_beej", SupportMantras.forDay(list, 3)?.mantraId)
        assertEquals("guru_beej", SupportMantras.forDay(list, 4)?.mantraId)
        assertEquals("shani_beej", SupportMantras.forDay(list, 5)?.mantraId)
        // Every day of a year lands on a valid item; same day → same pick.
        (1..366).forEach { day ->
            assertEquals(SupportMantras.forDay(list, day), SupportMantras.forDay(list, day))
        }
    }

    @Test
    fun emptyListHasNoPick() {
        assertNull(SupportMantras.forDay(emptyList(), 100))
    }

    @Test
    fun singleItemAlwaysPicked() {
        assertEquals(list[0], SupportMantras.forDay(list.take(1), 280))
    }

    @Test
    fun reasonTextSelection() {
        assertEquals(R.string.dashboard_geet_reason_weak, SupportMantras.reasonRes("strengthen_weak"))
        assertEquals(R.string.dashboard_geet_reason_running, SupportMantras.reasonRes("support_running_period"))
        assertEquals(R.string.dashboard_geet_reason_upcoming, SupportMantras.reasonRes("prepare_upcoming_period"))
        assertEquals(R.string.dashboard_geet_reason_protection, SupportMantras.reasonRes("ongoing_protection"))
        assertNull(SupportMantras.reasonRes("something_new"))
    }

    @Test
    fun usableDropsUnknownAndNonGrahaIds() {
        val withJunk = list + SupportMantra("X", "unknown_beej", "strengthen_weak") +
            SupportMantra("", "gayatri", "strengthen_weak") + list[0]
        assertEquals(list, SupportMantras.usable(withJunk))
    }
}
