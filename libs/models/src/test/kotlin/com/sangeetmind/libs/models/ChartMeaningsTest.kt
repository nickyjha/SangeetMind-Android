package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Built-in meanings in POST /v1/chart (app/services/chart_meanings.py), real output for
 * 1985-01-24 06:35 Nagda: planet nakshatras, conjunctions and dasha meanings. */
class ChartMeaningsTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ChartSummaryResponse::class.java)

    private fun chart(): ChartSummaryResponse = adapter.fromJson(
        javaClass.classLoader!!.getResource("chart_meanings_nagda.json")!!.readText()
    )!!

    @Test
    fun parsesPlanetNakshatraMeanings() {
        val planets = chart().planets
        val moon = planets.getValue("Moon")
        assertEquals("Shatabhisha", moon.nakshatra!!.name)
        assertEquals(3, moon.nakshatra!!.pada)
        assertTrue(moon.nakshatraMeaning!!.forLanguage("hi").isNotBlank())
        assertNotNull(planets.getValue("Jupiter").combustMeaning)
        assertNull(planets.getValue("Sun").combustMeaning)
    }

    @Test
    fun parsesConjunctionsAndDashaMeanings() {
        val chart = chart()
        assertEquals(
            listOf(listOf("Sun", "Jupiter"), listOf("Moon", "Mars"), listOf("Moon", "Venus"),
                listOf("Mars", "Venus"), listOf("Saturn", "Ketu")),
            chart.conjunctions.map { it.planets }
        )
        assertEquals(12, chart.conjunctions.last().house)
        val current = chart.vimshottari.current!!
        assertTrue(current.meaning!!.forLanguage("en").startsWith("Wisdom lightens the load"))
        val saturnMd = chart.vimshottari.mahadashas.single()
        assertTrue(saturnMd.bhuktis.all { it.meaning != null })
    }

    @Test
    fun oldCachedChartWithoutMeaningsStillParses() {
        val chart = adapter.fromJson(
            """{"lagna": {"sign": "Aries"}, "planets": {"Sun": {"sign": "Aries"}},
               "vimshottari": {"mahadashas": [{"lord": "Sun", "start": "a", "end": "b",
                 "bhuktis": [{"lord": "Sun", "start": "a", "end": "b"}]}]}}"""
        )!!
        assertNull(chart.planets.getValue("Sun").nakshatra)
        assertTrue(chart.conjunctions.isEmpty())
        assertNull(chart.vimshottari.mahadashas.single().bhuktis.single().meaning)
    }
}
