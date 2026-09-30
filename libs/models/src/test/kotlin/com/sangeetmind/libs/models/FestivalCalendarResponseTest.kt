package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/** GET /v1/festivals (app/services/festival_service.py), real output for Delhi 2026 (two entries). */
class FestivalCalendarResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(FestivalCalendarResponse::class.java)

    @Test
    fun parsesNamesAndDates() {
        val json = javaClass.classLoader!!.getResource("festivals_delhi_2026.json")!!.readText()
        val cal = adapter.fromJson(json)!!
        assertEquals(2026, cal.year)
        val diwali = cal.festivals.last()
        assertEquals("diwali", diwali.id)
        assertEquals("2026-11-08", diwali.date)
        assertEquals("दीपावली", diwali.nameFor("hi"))
        assertEquals("Diwali", diwali.nameFor("en"))
    }
}
