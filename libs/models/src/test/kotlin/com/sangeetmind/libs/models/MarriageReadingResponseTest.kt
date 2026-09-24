package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/marriage response shape (app/services/llm_marriage_service.py). */
class MarriageReadingResponseTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(MarriageReadingResponse::class.java)

    @Test
    fun parsesReadingWithMergedTiming() {
        val json = """
            {"ok": true, "marital_status": "single",
             "reading": {"summary": "s", "spouse_nature": "n",
               "relationship_strengths": ["a"], "challenges": ["b"], "manglik_note": "m",
               "timing": [{"id": "w1", "start": "2027-04-20", "end": "2029-09-16",
                 "kind": "marriage", "strength": "strong", "mahadasha": "Mercury",
                 "antardasha": "Mercury", "why": "Mercury is your 7th lord."}],
               "advice": ["c"], "remedies": [{"remedy": "r", "for_planet": "Venus"}]},
             "windows": [{"id": "w1", "start": "2027-04-20", "end": "2029-09-16",
               "mahadasha": "Mercury", "antardasha": "Mercury", "kind": "marriage",
               "strength": "strong", "double_transit_months": ["2027-06"], "reasons": ["x"]}],
             "facts": {"lagna": "Sagittarius"}, "error": null}
        """.trimIndent()
        val response = adapter.fromJson(json)!!
        val reading = response.reading!!
        assertTrue(response.ok)
        assertEquals("n", reading.spouseNature)
        assertEquals(listOf("a"), reading.relationshipStrengths)
        val w = reading.timing.single()
        assertEquals("2027-04-20", w.start)
        assertEquals("marriage", w.kind)
        assertEquals("Mercury", w.antardasha)
        assertEquals("Venus", reading.remedies.single().forPlanet)
        assertEquals(1, response.windows.size)
    }

    @Test
    fun parsesFailureWithoutReading() {
        val response = adapter.fromJson(
            """{"ok": false, "marital_status": "married", "reading": null, "windows": [], "facts": {}, "error": "Gemini did not return valid JSON."}"""
        )!!
        assertNull(response.reading)
        assertEquals("married", response.maritalStatus)
    }
}
