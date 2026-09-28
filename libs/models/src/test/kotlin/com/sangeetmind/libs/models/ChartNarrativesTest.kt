package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** `narratives` in POST /v1/chart (app/services/kundli_narratives.py): English + `*_hi` siblings. */
class ChartNarrativesTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ChartNarratives::class.java)

    @Test
    fun parsesHindiSiblings() {
        val json = """
            {"nakshatra_phal": {"nakshatra": "Uttara Ashadha", "pada": 1, "lord": "Sun",
               "text": "Principled and patient.", "text_hi": "उसूलों वाले और धैर्यवान।"},
             "ascendant_summary": {"sign": "Virgo", "text": "Virgo Ascendant: practical.",
               "text_hi": "कन्या लग्न: व्यावहारिक।"},
             "vimshottari_mahadasha_phal": {"current_mahadasha": "Jupiter", "current_antardasha": "Saturn",
               "mahadasha_text": "Jupiter mahadasha expands wisdom.", "mahadasha_text_hi": "गुरु की महादशा ज्ञान बढ़ाती है।",
               "antardasha_note": "Within Jupiter mahadasha...", "antardasha_note_hi": "गुरु की महादशा में..."},
             "planet_considerations": ["Sun in Aries, house 8 from Virgo lagna (exalted)."],
             "planet_considerations_hi": ["सूर्य मेष में, कन्या लग्न से आठवें भाव में (उच्च)।"],
             "disclaimer": "Static summaries.", "disclaimer_hi": "ये सामान्य सारांश हैं।"}
        """.trimIndent()
        val n = adapter.fromJson(json)!!
        assertEquals("उसूलों वाले और धैर्यवान।", n.nakshatraPhal.textHi)
        assertEquals("कन्या लग्न: व्यावहारिक।", n.ascendantSummary.textHi)
        assertEquals("गुरु की महादशा में...", n.vimshottariMahadashaPhal.antardashaNoteHi)
        assertEquals(1, n.planetConsiderationsHi.size)
        assertEquals("ये सामान्य सारांश हैं।", n.disclaimerHi)
    }

    @Test
    fun oldResponseWithoutHindiStillParses() {
        val n = adapter.fromJson("""{"nakshatra_phal": {"text": "t"}, "disclaimer": "d"}""")!!
        assertEquals("t", n.nakshatraPhal.text)
        assertTrue(n.nakshatraPhal.textHi.isEmpty() && n.planetConsiderationsHi.isEmpty())
    }
}
