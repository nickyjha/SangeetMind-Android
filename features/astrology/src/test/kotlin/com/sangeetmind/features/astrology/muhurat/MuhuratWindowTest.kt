package com.sangeetmind.features.astrology.muhurat

import org.junit.Assert.assertEquals
import org.junit.Test

class MuhuratWindowTest {
    @Test
    fun endAfterStartIsKept() {
        assertEquals("2026-10-05" to "2026-11-01", adjustWindowForStart("2026-10-05", "2026-11-01"))
    }

    @Test
    fun endBeforeNewStartMovesThirtyDaysOut() {
        assertEquals("2026-12-01" to "2026-12-31", adjustWindowForStart("2026-12-01", "2026-11-01"))
    }

    @Test
    fun blankEndGetsDefaultWindow() {
        assertEquals("2026-10-02" to "2026-11-01", adjustWindowForStart("2026-10-02", ""))
    }
}
