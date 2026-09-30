package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** `doshas.pitra/grahan/gandanta` in POST /v1/chart (app/services/extra_doshas.py). */
class ExtraDoshasTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ChartSummaryResponse::class.java)

    @Test
    fun parsesPresentAndClearDoshas() {
        // Backend output for a chart with the Sun and Rahu together in the 7th house.
        val json = """
            {"lagna": {"sign": "Aries"}, "doshas": {
              "pitra": {"present": true, "name": {"en": "Pitra Dosha", "hi": "पितृ दोष"},
                "factors": [{"planets": ["Sun", "Rahu"], "en": "Sun sits with Rahu in the 7th house.",
                  "hi": "सूर्य राहु के साथ सातवें भाव में है।"}],
                "meaning": {"en": "The Sun and the 9th house stand for father.", "hi": "सूर्य और नौवां भाव"},
                "remedy": {"en": "Offer water and sesame to ancestors.", "hi": "पितरों को तर्पण करें।"}},
              "gandanta": {"present": false, "name": {"en": "Gandanta Dosha", "hi": "गंडांत दोष"},
                "factors": [], "meaning": null, "remedy": null}}}
        """.trimIndent()
        val doshas = adapter.fromJson(json)!!.doshas
        val pitra = doshas.pitra!!
        assertTrue(pitra.present)
        assertEquals(listOf("Sun", "Rahu"), pitra.factors.single().planets)
        assertEquals("सूर्य राहु के साथ सातवें भाव में है।", pitra.factors.single().text.forLanguage("hi"))
        assertEquals("पितृ दोष", pitra.name.forLanguage("hi"))
        assertTrue(pitra.remedy!!.forLanguage("en").contains("ancestors"))
        val gandanta = doshas.gandanta!!
        assertFalse(gandanta.present)
        assertNull(gandanta.meaning)
        assertNull(doshas.grahan)
    }

    @Test
    fun oldCachedChartWithoutExtraDoshasStillParses() {
        val json = """{"lagna": {"sign": "Aries"}, "doshas": {"manglik": {}, "kalsarpa": {}, "sadesati": {}}}"""
        val doshas = adapter.fromJson(json)!!.doshas
        assertNull(doshas.pitra)
        assertNull(doshas.gandanta)
    }
}
