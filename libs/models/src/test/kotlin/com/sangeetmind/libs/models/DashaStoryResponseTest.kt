package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/dasha-story (app/services/llm_dasha_story_service.py). */
class DashaStoryResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(DashaStoryResponse::class.java)

    @Test
    fun parsesChaptersWithPhaseAndAges() {
        val json = """
            {"ok": true, "status": "life",
             "reading": {"summary": "Overall support is moderate.",
                         "timing": [
                           {"id": "w1", "start": "1985-01-24", "end": "1992-04-18", "kind": "steady", "strength": "moderate",
                            "mahadasha": "Rahu", "antardasha": "", "when": "past", "age_from": 0, "age_to": 7,
                            "title": "Early years", "why": "Rahu in your 6th house → ..."},
                           {"id": "w4", "start": "2008-04-18", "end": "2027-04-18", "kind": "growth", "strength": "strong",
                            "mahadasha": "Saturn", "antardasha": "", "when": "current", "age_from": 23, "age_to": 42,
                            "title": "Building", "why": "Saturn ..."}],
                         "now": "Keep routines.", "advice": ["Walk daily."],
                         "remedies": [{"remedy": "Feed crows on Saturdays.", "for_planet": "Saturn"}]},
             "windows": [], "error": null}
        """.trimIndent()
        val r = adapter.fromJson(json)!!
        assertTrue(r.ok)
        val chapters = r.reading!!.timing
        assertEquals(2, chapters.size)
        assertEquals("past", chapters[0].phase)
        assertEquals(7, chapters[0].ageTo)
        assertEquals("current", chapters[1].phase)
        assertEquals("Saturn", chapters[1].mahadasha)
        assertEquals("Building", chapters[1].title)
        assertEquals("Saturn", r.reading!!.remedies.single().forPlanet)
    }
}
