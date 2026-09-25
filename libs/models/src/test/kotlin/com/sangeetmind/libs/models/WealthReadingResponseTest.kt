package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/wealth response shape (app/services/llm_wealth_service.py). */
class WealthReadingResponseTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(WealthReadingResponse::class.java)

    @Test
    fun parsesReadingWithMergedTiming() {
        val json = """
            {"ok": true, "status": "business",
             "reading": {"summary": "s", "money_nature": "m",
               "strengths": ["a"], "care_points": ["b"],
               "timing": [{"id": "w1", "start": "2026-09-24", "end": "2027-04-20",
                 "kind": "wealth", "strength": "moderate", "mahadasha": "Saturn",
                 "antardasha": "Jupiter", "why": "Jupiter is the karaka of wealth."}],
               "advice": ["c"], "remedies": [{"remedy": "r", "for_planet": "Jupiter"}]},
             "windows": [{"id": "w1", "start": "2026-09-24", "end": "2027-04-20",
               "mahadasha": "Saturn", "antardasha": "Jupiter", "kind": "wealth",
               "strength": "moderate", "double_transit_months": [], "reasons": ["x"]}],
             "facts": {"yogas": ["Jupiter sits in your 2nd house."], "strength": "moderate"},
             "error": null}
        """.trimIndent()
        val response = adapter.fromJson(json)!!
        val reading = response.reading!!
        assertTrue(response.ok)
        assertEquals("business", response.status)
        assertEquals("m", reading.moneyNature)
        assertEquals(listOf("b"), reading.carePoints)
        assertEquals("wealth", reading.timing.single().kind)
    }

    @Test
    fun parsesFailureWithoutReading() {
        val response = adapter.fromJson(
            """{"ok": false, "status": "job", "reading": null, "windows": [], "facts": {}, "error": "Gemini did not return a usable reading."}"""
        )!!
        assertNull(response.reading)
        assertEquals("job", response.status)
    }
}
