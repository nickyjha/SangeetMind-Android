package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/career-question (app/services/llm_career_question_service.py). */
class CareerQuestionResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(CareerQuestionResponse::class.java)

    @Test
    fun parsesVerdictReadingAndWindows() {
        val json = """
            {"ok": true, "question": "job_business",
             "reading": {"summary": "Leans to business.", "outlook": "o", "strengths": ["a"],
               "care_points": ["b"], "advice": ["c"],
               "timing": [{"id": "w1", "start": "2027-04-20", "end": "2029-09-16", "kind": "career",
                 "strength": "strong", "mahadasha": "Mercury", "antardasha": "Mercury", "why": "Mercury period."}],
               "remedies": [{"remedy": "Vishnu Sahasranama on Wednesdays.", "for_planet": "Mercury"}]},
             "windows": [], "error": null,
             "facts": {"question": "job_business", "verdict": "business", "statements": ["s"],
               "indicators": {"business": ["x"], "job": []}, "notes": [], "significators": {}}}
        """.trimIndent()
        val r = adapter.fromJson(json)!!
        assertEquals("business", r.facts.verdict)
        assertEquals("career", r.reading!!.timing.single().kind)
        assertEquals(listOf("b"), r.reading!!.carePoints)
        assertTrue(r.reading!!.outlook.isNotBlank())
    }
}
