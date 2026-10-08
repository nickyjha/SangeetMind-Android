package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The plain-words Shadbala fields (app/services/shadbala.py: strength_label, rank_by_ratio,
 * summary, summary_hi and the block-level summary) parse, and are all optional so an older
 * backend payload still decodes. */
class ShadbalaSummaryParseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ChartShadbala::class.java)

    @Test
    fun parsesSummaryFields() {
        val json = """
            {
              "planets": {
                "Saturn": {
                  "sign": "Scorpio", "house": 12,
                  "components": {"sthana": 1, "dig": 2, "kala": 3, "cheshta": 4, "naisargika": 5, "drik": 6},
                  "total_virupas": 399.2, "total_rupas": 6.65, "required_virupas": 300,
                  "strength_ratio": 1.33, "is_strong": true,
                  "ishta_phala": 36.41, "kashta_phala": 12.52,
                  "strength_label": "very_strong", "rank_by_ratio": 1,
                  "summary": "Your strongest planet (1.33): delivers well in its periods, mostly helpful results (helpful 36 / difficult 13).",
                  "summary_hi": "आपका सबसे मज़बूत ग्रह (1.33): अपनी दशा में अच्छा फल देता है, ज़्यादातर अच्छे नतीजे (अच्छा 36 / मुश्किल 13)।"
                }
              },
              "ranking": [{"planet": "Saturn", "total_virupas": 399.2}],
              "ranking_by_ratio": [{"planet": "Saturn", "strength_ratio": 1.33}],
              "summary": {"strongest": "Saturn", "weakest": "Jupiter", "weak_planets": ["Jupiter"]},
              "method": "bphs_full", "note": "n"
            }
        """.trimIndent()
        val sb = adapter.fromJson(json)!!
        val saturn = sb.planets.getValue("Saturn")
        assertEquals(1.33, saturn.strengthRatio!!, 1e-9)
        assertEquals("very_strong", saturn.strengthLabel)
        assertEquals(1, saturn.rankByRatio)
        assertEquals(true, saturn.summary!!.startsWith("Your strongest planet (1.33)"))
        assertEquals(true, saturn.summaryHi!!.startsWith("आपका सबसे मज़बूत ग्रह"))
        assertEquals("Saturn", sb.summary!!.strongest)
        assertEquals("Jupiter", sb.summary!!.weakest)
        assertEquals(listOf("Jupiter"), sb.summary!!.weakPlanets)
    }

    @Test
    fun olderPayloadWithoutSummaryStillParses() {
        val json = """
            {"planets": {"Sun": {"sign": "Capricorn", "total_virupas": 393.0, "total_rupas": 6.55}},
             "ranking": [{"planet": "Sun", "total_virupas": 393.0}], "method": "bphs_full", "note": ""}
        """.trimIndent()
        val sb = adapter.fromJson(json)!!
        val sun = sb.planets.getValue("Sun")
        assertNull(sun.strengthRatio)
        assertNull(sun.strengthLabel)
        assertNull(sun.rankByRatio)
        assertNull(sun.summary)
        assertNull(sun.summaryHi)
        assertNull(sb.summary)
    }
}
