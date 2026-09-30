package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/** POST /llm/health (app/services/llm_health_service.py). */
class HealthReadingResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(HealthReadingResponse::class.java)

    @Test
    fun parsesMindReading() {
        val json = """
            {"ok": true, "status": "mind",
             "reading": {"summary": "s", "outlook": "o", "strengths": ["a"], "care_points": ["b"],
               "advice": ["c"],
               "timing": [{"id": "w1", "start": "2030-09-13", "end": "2033-07-14", "kind": "calm",
                 "strength": "strong", "mahadasha": "Mercury", "antardasha": "Venus", "why": "Venus period."}],
               "remedies": [{"remedy": "Shri Suktam on Fridays.", "for_planet": "Venus"}]},
             "windows": [], "facts": {"status": "mind", "strength": "mild"}, "error": null}
        """.trimIndent()
        val r = adapter.fromJson(json)!!
        assertEquals("mind", r.status)
        assertEquals("calm", r.reading!!.timing.single().kind)
        assertEquals("Venus", r.reading!!.remedies.single().forPlanet)
        assertEquals("o", r.reading!!.outlook)
    }
}
