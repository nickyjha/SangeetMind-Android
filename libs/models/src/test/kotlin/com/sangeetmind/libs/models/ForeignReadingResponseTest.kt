package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/foreign response shape (app/services/llm_foreign_service.py). */
class ForeignReadingResponseTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ForeignReadingResponse::class.java)

    @Test
    fun parsesReadingWithMergedTiming() {
        val json = """
            {"ok": true, "status": "planning",
             "reading": {"summary": "s", "abroad_outlook": "o",
               "strengths": ["a"], "care_points": ["b"],
               "timing": [{"id": "w2", "start": "2033-07-14", "end": "2034-05-20",
                 "kind": "abroad", "strength": "strong", "mahadasha": "Mercury",
                 "antardasha": "Sun", "why": "Sun is your 9th lord."}],
               "advice": ["c"], "remedies": [{"remedy": "r", "for_planet": "Rahu"}]},
             "windows": [{"id": "w2", "start": "2033-07-14", "end": "2034-05-20",
               "mahadasha": "Mercury", "antardasha": "Sun", "kind": "abroad",
               "strength": "strong", "double_transit_months": ["2033-08"], "reasons": ["x"]}],
             "facts": {"indicators": ["Rahu aspects your 12th house."]}, "error": null}
        """.trimIndent()
        val response = adapter.fromJson(json)!!
        val reading = response.reading!!
        assertTrue(response.ok)
        assertEquals("o", reading.abroadOutlook)
        assertEquals(listOf("b"), reading.carePoints)
        assertEquals("abroad", reading.timing.single().kind)
        assertEquals("Rahu", reading.remedies.single().forPlanet)
    }

    @Test
    fun parsesFailureWithoutReading() {
        val response = adapter.fromJson(
            """{"ok": false, "status": "abroad", "reading": null, "windows": [], "facts": {}, "error": "Gemini did not return a usable reading."}"""
        )!!
        assertNull(response.reading)
        assertEquals("abroad", response.status)
    }
}
