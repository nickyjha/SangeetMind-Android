package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/** POST /llm/property (app/services/llm_property_service.py). */
class PropertyReadingResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(PropertyReadingResponse::class.java)

    @Test
    fun parsesVehicleReading() {
        val json = """
            {"ok": true, "status": "vehicle",
             "reading": {"summary": "s", "outlook": "o", "strengths": ["a"], "care_points": ["b"],
               "advice": ["c"],
               "timing": [{"id": "w1", "start": "2030-09-13", "end": "2033-07-14", "kind": "vehicle",
                 "strength": "strong", "mahadasha": "Mercury", "antardasha": "Venus", "why": "Venus period."}],
               "remedies": [{"remedy": "Shri Suktam on Fridays.", "for_planet": "Venus"}]},
             "windows": [], "facts": {"status": "vehicle", "strength": "mild"}, "error": null}
        """.trimIndent()
        val r = adapter.fromJson(json)!!
        assertEquals("vehicle", r.status)
        assertEquals("vehicle", r.reading!!.timing.single().kind)
        assertEquals("Venus", r.reading!!.remedies.single().forPlanet)
        assertEquals("o", r.reading!!.outlook)
    }
}
