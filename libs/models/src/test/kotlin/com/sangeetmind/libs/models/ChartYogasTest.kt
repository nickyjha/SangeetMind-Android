package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** `yogas` in POST /v1/chart (app/services/chart_yogas.py), real output for 1985-01-24 06:35 Nagda. */
class ChartYogasTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ChartSummaryResponse::class.java)

    private fun fixture(): String =
        javaClass.classLoader!!.getResource("chart_yogas_nagda.json")!!.readText()

    @Test
    fun parsesRealYogas() {
        val yogas = adapter.fromJson(fixture())!!.yogas
        assertEquals(
            listOf("chandra_mangala", "raja", "dhana", "neecha_bhanga", "dainya_parivartana"),
            yogas.map { it.id }
        )
        val raja = yogas.first { it.id == "raja" }
        assertEquals(listOf("Jupiter", "Sun"), raja.planets)
        assertEquals("राजयोग", raja.name.forLanguage("hi"))
        assertTrue(raja.reason.forLanguage("en").contains("9th lord"))
        assertFalse(raja.isChallenging)
        assertTrue(yogas.last().isChallenging)
        assertTrue(yogas.none { it.cancelled || it.cancellation != null })
    }

    @Test
    fun parsesCancelledYoga() {
        val json = """
            {"lagna": {"sign": "Aries"},
             "yogas": [{"id": "kemadruma", "name": {"en": "Kemadruma Yoga", "hi": "केमद्रुम योग"},
               "kind": "challenging", "planets": ["Moon"],
               "reason": {"en": "r", "hi": "र"}, "meaning": {"en": "m", "hi": "म"},
               "cancelled": true, "cancellation": {"en": "Cancelled: Mars is with the Moon.", "hi": "भंग"}}]}
        """.trimIndent()
        val yoga = adapter.fromJson(json)!!.yogas.single()
        assertTrue(yoga.cancelled && yoga.isChallenging)
        assertEquals("भंग", yoga.cancellation!!.forLanguage("hi"))
    }

    @Test
    fun oldCachedChartWithoutYogasStillParses() {
        assertTrue(adapter.fromJson("""{"lagna": {"sign": "Aries"}}""")!!.yogas.isEmpty())
    }
}
