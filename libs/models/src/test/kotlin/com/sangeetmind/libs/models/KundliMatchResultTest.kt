package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /v1/kundli/match (app/services/kundli_match_service.py): doshaExceptions + summary_hi. */
class KundliMatchResultTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(KundliMatchEnvelope::class.java)

    private val koota = """{"points": 0, "max": 7, "personA": "Aries", "personB": "Scorpio"}"""
    private val ashtakoot = listOf("varna", "vashya", "tara", "yoni", "grahaMaitri", "gana", "bhakoot", "nadi")
        .joinToString(",", "{", "}") { "\"$it\": $koota" }
    private val manglik = """{"present": true, "effective_present": true, "cancelled": false,
        "summary": "Mangal Dosha is present in both Lagna and Moon charts.",
        "summary_hi": "मंगल दोष लग्न और चंद्र दोनों कुंडलियों में है।"}"""

    private fun envelope(extra: String) = """
        {"success": true, "match": {"ashtakoot": $ashtakoot, "totalGunas": 20, "maxGunas": 36,
          "manglik": {"personA": $manglik, "personB": $manglik}, "verdict": "average"$extra}}
    """.trimIndent()

    @Test
    fun parsesDoshaExceptionsAndHindiManglik() {
        val result = adapter.fromJson(
            envelope(
                """, "remedyHint": null, "doshaExceptions": {
                  "nadi": {"present": false, "cancelled": false, "reasons": []},
                  "bhakoot": {"present": true, "cancelled": true, "reasons": [
                    {"en": "Both Moon signs are ruled by Mars.", "hi": "दोनों चंद्र राशियों का स्वामी मंगल है।"}]}}"""
            )
        )!!.match
        val bhakoot = result.doshaExceptions!!.bhakoot
        assertTrue(bhakoot.present && bhakoot.cancelled)
        assertEquals("दोनों चंद्र राशियों का स्वामी मंगल है।", bhakoot.reasons.single().forLanguage("hi"))
        assertFalse(result.doshaExceptions!!.nadi.present)
        assertEquals("मंगल दोष लग्न और चंद्र दोनों कुंडलियों में है।", result.manglik.personA.summaryText.forLanguage("hi"))
        assertNull(result.remedyHint)
    }

    @Test
    fun olderBackendWithoutExceptionsStillParses() {
        val json = envelope(""", "remedyHint": "Consult astrologer"""")
            .replace(Regex(""",\s*"summary_hi": "[^"]*""""), "")
        val result = adapter.fromJson(json)!!.match
        assertNull(result.doshaExceptions)
        // No summary_hi: Hindi falls back to the English summary.
        assertTrue(result.manglik.personA.summaryText.forLanguage("hi").startsWith("Mangal Dosha"))
    }
}
