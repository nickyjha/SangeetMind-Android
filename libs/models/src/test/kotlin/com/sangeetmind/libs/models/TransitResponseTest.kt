package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Parses a real POST /v1/transit response (the Nagda test chart on 2026-09-24, produced by
 * app/services/transit_service.py) to catch field-name drift between backend and app. */
class TransitResponseTest {
    // Same Moshi setup as ApiClient.provideMoshi().
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(TransitResponse::class.java)

    private fun load(): TransitResponse {
        val json = javaClass.classLoader!!.getResource("transit_nagda_2026-09-24.json")!!.readText()
        return adapter.fromJson(json)!!
    }

    @Test
    fun parsesNatalAndGocharFields() {
        val response = load()
        assertEquals("Sagittarius", response.natal.lagna.sign)
        assertEquals("Aquarius", response.natal.moonSign)

        val saturn = response.transit.planets.getValue("Saturn")
        assertEquals("Pisces", saturn.sign)
        assertEquals(4, saturn.houseFromNatalLagna)
        assertEquals(2, saturn.houseFromNatalMoon)
        assertEquals("challenging", saturn.gocharEffect)
        assertEquals("Aries", saturn.nextSignChange?.sign)
        assertEquals("direct", saturn.nextStation?.type)
        assertEquals(listOf(1, 6, 10), saturn.aspectsNatalHouses.map { it.house })
        // H1 = 10th aspect, H6 = 3rd, H10 = 7th.
        assertEquals(listOf(75, 75, 100), saturn.aspectsNatalHouses.map { it.strength })

        assertNull(response.transit.planets.getValue("Rahu").nextStation)
        assertEquals(9, response.transit.planets.size)
    }

    @Test
    fun toleratesOlderBackendWithoutGocharFields() {
        val old = """{"natal":{"lagna":{"sign":"Leo"},"planets":{}},
            "transit":{"date":"2026-01-01","planets":{"Sun":{"sign":"Sagittarius","house_from_natal_lagna":5}}}}"""
        val sun = adapter.fromJson(old)!!.transit.planets.getValue("Sun")
        assertEquals(5, sun.houseFromNatalLagna)
        assertNull(sun.gocharEffect)
        assertEquals(emptyList<PlanetAspect>(), sun.aspectsNatalHouses)
    }
}
