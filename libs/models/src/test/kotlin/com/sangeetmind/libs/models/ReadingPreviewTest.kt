package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/preview (app/services/reading_preview_service.py), real output shape. */
class ReadingPreviewTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ReadingPreview::class.java)

    @Test
    fun parsesKeyPlanetsWindowsAndTeaser() {
        val json = javaClass.classLoader!!.getResource("reading_preview_marriage.json")!!.readText()
        val p = adapter.fromJson(json)!!
        assertEquals("Saturn", p.keyPlanets.first().planet)
        assertEquals(7, p.keyPlanets.first().house)
        assertEquals(3, p.windows)
        assertEquals(2026, p.firstYear)
        assertTrue(p.runningNow)
        assertTrue(p.teaser.forLanguage("hi").contains("अभी चल रहा है"))
    }
}
