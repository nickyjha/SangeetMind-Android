package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /v1/muhurat/vivah (vivah_muhurat_service.py), real output for Delhi 25 Jan-6 Feb 2026. */
class VivahResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(VivahResponse::class.java)

    @Test
    fun parsesDaysWindowsAndBlocks() {
        val json = javaClass.classLoader!!.getResource("vivah_delhi_2026_02.json")!!.readText()
        val r = adapter.fromJson(json)!!
        assertFalse(r.coupleChecked)
        // Shukra asta until 31 Jan 2026, so the first day is in February.
        val block = r.blocked.single { it.reason == "venus_combust" }
        assertEquals("2026-01-31", block.end)
        assertEquals("शुक्र अस्त", block.text.forLanguage("hi"))
        val first = r.days.first()
        assertTrue(first.date >= "2026-02-01")
        val window = first.windows.first()
        assertTrue(window.start < window.end && window.nakshatras.isNotEmpty())
        assertTrue(window.tithis.first().forLanguage("hi").isNotBlank())
    }
}
