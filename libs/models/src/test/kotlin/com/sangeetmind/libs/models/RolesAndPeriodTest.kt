package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** `functional_roles` + `current_period` in POST /v1/chart (real backend output for
 * 1985-01-24 06:35 Nagda at 2026-10-07), and both absent on older deploys. */
class RolesAndPeriodTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val chartAdapter = moshi.adapter(ChartSummaryResponse::class.java)
    private val profileAdapter = moshi.adapter(AstroProfileSummary::class.java)

    private fun chart(): ChartSummaryResponse = chartAdapter.fromJson(
        javaClass.classLoader!!.getResource("roles_period_nagda.json")!!.readText()
    )!!

    @Test
    fun parsesFunctionalRoles() {
        val roles = chart().functionalRoles!!
        assertEquals("Sagittarius", roles.lagna)
        assertEquals(9, roles.planets.size)
        val mercury = roles.planets.getValue("Mercury")
        assertEquals(listOf(7, 10), mercury.lordships)
        assertEquals(listOf("kendra_lord"), mercury.roles)
        assertEquals("neutral", mercury.nature)
        assertTrue(mercury.kendradhipatiDosha)
        val jupiter = roles.planets.getValue("Jupiter")
        assertEquals(listOf("lagna_lord", "trikona_lord", "kendra_lord"), jupiter.roles)
        assertEquals("benefic", jupiter.nature)
        assertTrue(roles.planets.getValue("Rahu").lordships.isEmpty())
    }

    @Test
    fun parsesCurrentPeriod() {
        val period = chart().currentPeriod!!
        assertEquals("Saturn", period.mahadasha!!.lord)
        assertEquals("Jupiter", period.antardasha!!.lord)
        assertEquals("Moon", period.pratyantardasha!!.lord)
        assertEquals("Mercury", period.nextMahadasha!!.lord)
        assertTrue(period.nextMahadasha!!.start.startsWith("2027-04-20"))
        assertEquals("pratyantardasha", period.nextChange!!.level)
        assertEquals("Mars", period.nextChange!!.lordAfter)
        assertTrue(period.nextChange!!.at.startsWith("2026-10-09"))
    }

    @Test
    fun absentOnOlderBackends() {
        val chart = chartAdapter.fromJson("""{"lagna":{"sign":"Aries"}}""")!!
        assertNull(chart.functionalRoles)
        assertNull(chart.currentPeriod)
        val profile = profileAdapter.fromJson("""{"moon_sign":"Aquarius","lagna":"Sagittarius"}""")!!
        assertNull(profile.currentPeriod)
    }

    @Test
    fun profileCarriesCurrentPeriod() {
        val period = chart().currentPeriod!!
        val json = """{"moon_sign":"Aquarius","lagna":"Sagittarius","current_period":""" +
            moshi.adapter(CurrentPeriod::class.java).toJson(period) + "}"
        assertEquals(period, profileAdapter.fromJson(json)!!.currentPeriod)
    }
}
