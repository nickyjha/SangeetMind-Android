package com.sangeetmind.libs.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** POST /v1/daily/scores (app/services/daily_scores_service.py), real output for 30 Sep 2026. */
class DailyScoresResponseTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(DailyScoresResponse::class.java)

    @Test
    fun parsesDaysAreasAndStrip() {
        val json = javaClass.classLoader!!.getResource("daily_scores_2026_09_30.json")!!.readText()
        val r = adapter.fromJson(json)!!
        assertEquals(listOf("yesterday", "today", "tomorrow"), r.days.map { it.label })
        val today = r.days[1]
        assertEquals(setOf("self", "wealth", "love", "career"), today.areas.keys)
        val career = today.areas.getValue("career")
        assertTrue(career.score in 15..95)
        assertTrue(career.question.forLanguage("hi").endsWith("?"))
        assertEquals(9, today.luckyNumber)
        assertTrue(today.luckyTime!!.start!!.contains(":"))
        assertEquals(7, r.strip.size)
    }
}
