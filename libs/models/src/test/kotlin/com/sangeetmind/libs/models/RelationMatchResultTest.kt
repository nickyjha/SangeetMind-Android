package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /v1/match/relation (app/services/relation_match_service.py). */
class RelationMatchResultTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(RelationMatchEnvelope::class.java)

    @Test
    fun parsesFactorsAndLocalizedText() {
        val json = """
            {"success": true, "match": {
              "relation": "business",
              "roles": {"personA": {"en": "Partner 1", "hi": "पार्टनर 1"}, "personB": {"en": "Partner 2", "hi": "पार्टनर 2"}},
              "personA": {"moonSign": "Cancer", "lagna": "Aries"}, "personB": {"moonSign": "Leo", "lagna": "Libra"},
              "factors": [
                {"key": "moon_harmony", "label": {"en": "Moon harmony", "hi": "चंद्र मेल"}, "points": 2.0, "max": 7.0,
                 "status": "caution", "reason": {"en": "Moons in Cancer and Leo: 2/12 apart.", "hi": "चंद्र कर्क और सिंह में।"}},
                {"key": "business", "label": {"en": "Business factors", "hi": "कारोबार के कारक"}, "points": 5.5, "max": 7.0,
                 "status": "good", "reason": {"en": "Mercury signs: friends.", "hi": "बुध की राशियाँ: मित्र।"}}],
              "total": 21.5, "max": 36.0, "level": "mixed",
              "summary": {"en": "A mixed picture.", "hi": "मिली-जुली तस्वीर।"},
              "tips": [{"en": "Keep money matters in writing.", "hi": "पैसे की बातें लिखित रखें।"}]}}
        """.trimIndent()
        val r = adapter.fromJson(json)!!
        assertTrue(r.success)
        val m = r.match
        assertEquals("business", m.relation)
        assertEquals("पार्टनर 1", m.roles.personA.forLanguage("hi"))
        assertEquals(2, m.factors.size)
        assertEquals("caution", m.factors[0].status)
        assertEquals(5.5, m.factors[1].points, 0.0)
        assertEquals("बुध की राशियाँ: मित्र।", m.factors[1].reason.forLanguage("hi"))
        assertEquals(21.5, m.total, 0.0)
        assertEquals("mixed", m.level)
        assertEquals("Keep money matters in writing.", m.tips.single().forLanguage("en"))
    }
}
