package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/children response shape (app/services/llm_children_service.py). */
class ChildrenReadingResponseTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ChildrenReadingResponse::class.java)

    @Test
    fun parsesReadingWithMergedTiming() {
        val json = """
            {"ok": true, "status": "planning",
             "reading": {"summary": "s", "children_nature": "n",
               "strengths": ["a"], "care_points": ["b"],
               "timing": [{"id": "w2", "start": "2030-09-13", "end": "2033-07-14",
                 "kind": "children", "strength": "strong", "mahadasha": "Mercury",
                 "antardasha": "Venus", "why": "Venus rules your D7 5th house."}],
               "advice": ["c"], "remedies": [{"remedy": "r", "for_planet": "Jupiter"}]},
             "windows": [{"id": "w2", "start": "2030-09-13", "end": "2033-07-14",
               "mahadasha": "Mercury", "antardasha": "Venus", "kind": "children",
               "strength": "strong", "double_transit_months": ["2031-06"], "reasons": ["x"]}],
             "facts": {"significators": {"Jupiter": ["karaka"]}}, "error": null}
        """.trimIndent()
        val response = adapter.fromJson(json)!!
        val reading = response.reading!!
        assertTrue(response.ok)
        assertEquals("planning", response.status)
        assertEquals("n", reading.childrenNature)
        assertEquals(listOf("b"), reading.carePoints)
        val w = reading.timing.single()
        assertEquals("children", w.kind)
        assertEquals("Venus", w.antardasha)
        assertEquals("Jupiter", reading.remedies.single().forPlanet)
        assertEquals(1, response.windows.size)
    }

    @Test
    fun parsesFailureWithoutReading() {
        val response = adapter.fromJson(
            """{"ok": false, "status": "parent", "reading": null, "windows": [], "facts": {}, "error": "Gemini did not return a usable reading."}"""
        )!!
        assertNull(response.reading)
        assertEquals("parent", response.status)
    }
}
