package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** `birth`, `ayanamsa`, `vimshottari.balance_at_birth`, `doshas.manglik.severity` (chart cache v19). */
class BirthSummaryTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(ChartSummaryResponse::class.java)

    @Test
    fun parsesBirthSummaryFields() {
        val json = """
            {"lagna": {"sign": "Capricorn"},
             "birth": {"date": "1985-01-24", "time": "06:35:30", "timezone": "Asia/Kolkata",
                       "utc_offset": "+05:30", "utc": "1985-01-24T01:05:00Z", "julian_day": 2446089.54},
             "ayanamsa": {"name": "Lahiri", "degrees": 23.6385, "dms": "23°38′19″"},
             "vimshottari": {"mahadashas": [], "current": null,
                             "balance_at_birth": {"lord": "Moon", "years": 3, "months": 4, "days": 12, "total_years": 3.3671}},
             "doshas": {"manglik": {"present": true, "effective_present": true, "cancelled": false,
                                    "severity": "moderate", "summary": "", "summary_hi": ""}}}
        """.trimIndent()
        val chart = adapter.fromJson(json)!!
        assertEquals("+05:30", chart.birth!!.utcOffset)
        assertEquals("Asia/Kolkata", chart.birth!!.timezone)
        assertEquals("Lahiri", chart.ayanamsa!!.name)
        assertEquals("23°38′19″", chart.ayanamsa!!.dms)
        val bal = chart.vimshottari.balanceAtBirth!!
        assertEquals("Moon", bal.lord)
        assertEquals(3, bal.years)
        assertEquals(4, bal.months)
        assertEquals(12, bal.days)
        assertEquals("moderate", chart.doshas.manglik.severity)
    }

    @Test
    fun olderCachedChartsLeaveThemNull() {
        val chart = adapter.fromJson("""{"lagna": {"sign": "Aries"}}""")!!
        assertNull(chart.birth)
        assertNull(chart.ayanamsa)
        assertNull(chart.vimshottari.balanceAtBirth)
        assertNull(chart.doshas.manglik.severity)
    }
}
