package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /v1/kp/prashna (app/services/kp_horary.py): number 100, marriage, Delhi 30 Sep 2026 18:00. */
class PrashnaResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(PrashnaResponse::class.java)

    @Test
    fun parsesRealAnswer() {
        val json = javaClass.classLoader!!.getResource("prashna_100_marriage.json")!!.readText()
        val r = adapter.fromJson(json)!!
        assertEquals(100, r.number)
        assertEquals("Leo", r.ascendant.sign)
        assertEquals(7, r.mainHouse)
        assertEquals("yes", r.verdict)
        assertEquals("Mercury", r.cuspSubLord)
        assertEquals("Mars", r.significations.starLord)
        assertTrue(r.reason.forLanguage("hi").startsWith("आपके 7वें भाव"))
        assertTrue(r.rulingPlanets.isNotEmpty())
    }
}
