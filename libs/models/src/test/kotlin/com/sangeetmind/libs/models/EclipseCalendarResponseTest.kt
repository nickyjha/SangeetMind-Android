package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** GET /v1/eclipses (app/services/eclipse_service.py), real output for Delhi 2026 (first two). */
class EclipseCalendarResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(EclipseCalendarResponse::class.java)

    private fun fixture(): String =
        javaClass.classLoader!!.getResource("eclipses_delhi_2026.json")!!.readText()

    @Test
    fun parsesAnnularNotVisibleAndTotalLunarWithSutak() {
        val cal = adapter.fromJson(fixture())!!
        assertEquals(2026, cal.year)
        val (annular, lunar) = cal.eclipses
        assertTrue(annular.isSolar)
        assertEquals("annular", annular.type)
        assertFalse(annular.visible)
        assertNull(annular.local)
        assertNull(annular.sutak)

        assertEquals("पूर्ण चंद्र ग्रहण", lunar.name.forLanguage("hi"))
        assertTrue(lunar.visible)
        assertTrue(lunar.sutak!!.begin.startsWith("2026-03-03T06:2"))
        assertEquals(9, lunar.sutak!!.hoursBefore)
        assertEquals("Leo", lunar.sign)
        assertEquals("careful", lunar.rashiEffects.effectFor("Leo"))
        assertEquals("favourable", lunar.rashiEffects.effectFor("Gemini"))
        assertEquals("mixed", lunar.rashiEffects.effectFor("Aries"))
    }
}
