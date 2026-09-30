package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /llm/small (app/services/llm_small_reading_service.py), response shape. */
class SmallReadingResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(SmallReadingResponse::class.java)

    @Test
    fun parsesPointsTipAndRemedy() {
        val json = javaClass.classLoader!!.getResource("small_reading_love_style.json")!!.readText()
        val r = adapter.fromJson(json)!!
        assertTrue(r.ok)
        assertEquals("love_style", r.topic)
        val reading = r.reading!!
        assertEquals("बातों से प्यार", reading.points.single().title)
        assertEquals("Venus", reading.remedies.single().forPlanet)
        assertTrue(reading.tip.isNotBlank())
    }
}
