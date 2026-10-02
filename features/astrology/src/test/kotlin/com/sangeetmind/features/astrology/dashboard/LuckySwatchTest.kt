package com.sangeetmind.features.astrology.dashboard

import com.sangeetmind.features.astrology.dashboard.ui.luckySwatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LuckySwatchTest {
    @Test
    fun matchesContainedColourWord() {
        assertEquals(luckySwatch("blue"), luckySwatch("Light Blue"))
        assertEquals(luckySwatch("blue"), luckySwatch("sky-blue"))
        assertEquals(luckySwatch("green"), luckySwatch("Dark green"))
    }

    @Test
    fun aliasesMapToKnownSwatches() {
        assertEquals(luckySwatch("orange"), luckySwatch("Saffron"))
        assertNotNull(luckySwatch("Golden yellow"))
    }

    @Test
    fun unknownColourHasNoSwatch() {
        assertNull(luckySwatch("Turquoise"))
        assertNull(luckySwatch(""))
    }
}
