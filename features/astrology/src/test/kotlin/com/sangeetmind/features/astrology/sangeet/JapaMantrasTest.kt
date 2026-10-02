package com.sangeetmind.features.astrology.sangeet

import com.sangeetmind.features.astrology.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JapaMantrasTest {
    @Test
    fun everyIdRoundTripsToItsMantra() {
        JapaMantras.all.forEach { mantra -> assertEquals(mantra, JapaMantras.byId(mantra.id)) }
    }

    @Test
    fun idsAndNamesAreUnique() {
        assertEquals(JapaMantras.all.size, JapaMantras.all.map { it.id }.toSet().size)
        assertEquals(JapaMantras.all.size, JapaMantras.all.map { it.nameRes }.toSet().size)
    }

    @Test
    fun genericIsKeptForOldSessions() {
        assertEquals("generic", JapaMantras.GENERIC_ID)
        assertEquals(R.string.sangeet_mantra_generic, JapaMantras.byId("generic")?.nameRes)
        assertEquals(JapaMantras.generic, JapaMantras.byId(""))
        assertEquals(JapaMantras.generic, JapaMantras.byId(null))
        assertTrue(JapaMantras.all.contains(JapaMantras.generic))
    }

    @Test
    fun lookupIgnoresCaseAndWhitespace() {
        assertEquals(R.string.sangeet_mantra_shani, JapaMantras.byId(" Shani_Beej ")?.nameRes)
        assertEquals(R.string.sangeet_mantra_gayatri, JapaMantras.byId("GAYATRI")?.nameRes)
    }

    @Test
    fun unknownIdsFallBackToTitleCase() {
        assertNull(JapaMantras.byId("gayatri_mantra"))
        assertEquals("Gayatri Mantra", JapaMantras.titleCase("gayatri_mantra"))
        assertEquals("My Custom Mantra", JapaMantras.titleCase(" my_custom-MANTRA "))
        assertEquals("", JapaMantras.titleCase("__"))
    }

    @Test
    fun everyNavagrahaHasABeejMantra() {
        val expected = mapOf(
            "Sun" to "surya_beej", "Moon" to "chandra_beej", "Mars" to "mangal_beej",
            "Mercury" to "budh_beej", "Jupiter" to "guru_beej", "Venus" to "shukra_beej",
            "Saturn" to "shani_beej", "Rahu" to "rahu_beej", "Ketu" to "ketu_beej"
        )
        expected.forEach { (lord, id) -> assertEquals(lord, id, JapaMantras.forDashaLord(lord)?.id) }
    }

    @Test
    fun dashaLordAcceptsHindiNamesAndRejectsUnknown() {
        assertEquals("shani_beej", JapaMantras.forDashaLord(" shani ")?.id)
        assertEquals("guru_beej", JapaMantras.forDashaLord("Guru")?.id)
        assertEquals("mangal_beej", JapaMantras.forDashaLord("Mangal")?.id)
        assertNull(JapaMantras.forDashaLord("Pluto"))
        assertNull(JapaMantras.forDashaLord(null))
        assertNull(JapaMantras.forDashaLord(""))
    }
}
