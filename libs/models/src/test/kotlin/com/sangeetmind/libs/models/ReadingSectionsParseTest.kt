package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every topic reading (app/services/llm_*_service.py) now carries an optional `sections`
 * list of six {heading, body} pairs next to the legacy keys. Readings cached before that
 * change have no `sections` at all, so the field must default to an empty list.
 */
class ReadingSectionsParseTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val sectionsJson = """
        "sections": [
          {"heading": "In short", "body": "Money comes through **steady work**."},
          {"heading": "The house and its lord", "body": "Your 2nd lord Mercury sits in the 10th."},
          {"heading": "What is driving it now", "body": "Mercury–Venus runs till 2027-04-20."},
          {"heading": "Transits", "body": "- Jupiter in the 11th from 2027-05-01\n- Saturn aspects the 2nd"},
          {"heading": "Next turning point", "body": "2027-04-20: the Venus antardasha begins."},
          {"heading": "What to do", "body": "1. Save first\n2. Avoid loans"}
        ]
    """.trimIndent()

    @Test
    fun parsesSectionsInOrderNextToLegacyKeys() {
        val json = """
            {"ok": true, "status": "job",
             "reading": {"summary": "s", "money_nature": "n", "strengths": ["a"], "care_points": ["b"],
               "timing": [], "advice": ["c"], "remedies": [{"remedy": "r", "for_planet": "Venus"}],
               $sectionsJson},
             "windows": [], "error": null}
        """.trimIndent()
        val reading = moshi.adapter(WealthReadingResponse::class.java).fromJson(json)!!.reading!!
        assertEquals("s", reading.summary)
        assertEquals("n", reading.moneyNature)
        assertEquals(6, reading.sections.size)
        assertEquals(
            listOf("In short", "The house and its lord", "What is driving it now", "Transits", "Next turning point", "What to do"),
            reading.sections.map { it.heading }
        )
        assertEquals("Money comes through **steady work**.", reading.sections[0].body)
        assertEquals("1. Save first\n2. Avoid loans", reading.sections[5].body)
    }

    @Test
    fun hindiSectionsAndMarriageShape() {
        val json = """
            {"ok": true, "marital_status": "single",
             "reading": {"summary": "s", "spouse_nature": "n", "relationship_strengths": [], "challenges": [],
               "manglik_note": "", "timing": [], "advice": [], "remedies": [],
               "sections": [{"heading": "संक्षेप में", "body": "शादी का योग 2027-04-20 के बाद।"}]},
             "windows": [], "facts": {}, "error": null}
        """.trimIndent()
        val reading = moshi.adapter(MarriageReadingResponse::class.java).fromJson(json)!!.reading!!
        assertEquals("संक्षेप में", reading.sections.single().heading)
        assertEquals("शादी का योग 2027-04-20 के बाद।", reading.sections.single().body)
    }

    @Test
    fun missingSectionsParsesToEmptyListEverywhere() {
        val legacy = """{"summary": "s", "outlook": "o", "strengths": [], "care_points": [], "timing": [], "advice": [], "remedies": []}"""
        assertTrue(moshi.adapter(HealthReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(EducationReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(PropertyReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(RelationshipReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(DebtReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(CareerQuestionReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(ChildrenReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(ForeignReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(WealthReading::class.java).fromJson(legacy)!!.sections.isEmpty())
        assertTrue(moshi.adapter(MarriageReading::class.java).fromJson("""{"summary": "s"}""")!!.sections.isEmpty())
    }
}
