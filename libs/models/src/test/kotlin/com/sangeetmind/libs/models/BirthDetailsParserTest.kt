package com.sangeetmind.libs.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BirthDetailsParserTest {

    private fun parse(text: String) = BirthDetailsParser.parse(text)

    @Test
    fun commaSeparatedWhatsAppStyle() {
        val p = parse("Rahul Kumar, 12 Jan 1990, 4:30 pm, Patna, Bihar")
        assertEquals("Rahul Kumar", p.name)
        assertEquals("1990-01-12", p.date)
        assertEquals("16:30", p.time)
        assertEquals("Patna, Bihar", p.place)
    }

    @Test
    fun labelledLines() {
        val p = parse("Name: Priya Sharma\nDOB: 24/01/1985\nTOB: 06:35\nPOB: Nagda, Madhya Pradesh")
        assertEquals("Priya Sharma", p.name)
        assertEquals("1985-01-24", p.date)
        assertEquals("06:35", p.time)
        assertEquals("Nagda, Madhya Pradesh", p.place)
    }

    @Test
    fun labelsOnOneLine() {
        val p = parse("name: Amit, dob: 2001-07-03, time: 11.15 am, place: Lucknow")
        assertEquals("Amit", p.name)
        assertEquals("2001-07-03", p.date)
        assertEquals("11:15", p.time)
        assertEquals("Lucknow", p.place)
    }

    @Test
    fun proseWithBornAtIn() {
        val p = parse("Sunita born 3rd March 1978 at 12:05 am in Jaipur")
        assertEquals("Sunita", p.name)
        assertEquals("1978-03-03", p.date)
        assertEquals("00:05", p.time)
        assertEquals("Jaipur", p.place)
    }

    @Test
    fun placeBeforeDate() {
        val p = parse("Vikram born in Mumbai on 5 Nov 1995 at 9 pm")
        assertEquals("Vikram", p.name)
        assertEquals("1995-11-05", p.date)
        assertEquals("21:00", p.time)
        assertEquals("Mumbai", p.place)
    }

    @Test
    fun hindiText() {
        val p = parse("राहुल, 12 जनवरी 1990, रात 11 बजे, पटना")
        assertEquals("राहुल", p.name)
        assertEquals("1990-01-12", p.date)
        assertEquals("23:00", p.time)
        assertEquals("पटना", p.place)
    }

    @Test
    fun hindiLabelsAndMorning() {
        val p = parse("नाम: सीता\nजन्म तारीख: 15-08-1992\nसमय: सुबह 6:20\nजगह: वाराणसी")
        assertEquals("सीता", p.name)
        assertEquals("1992-08-15", p.date)
        assertEquals("06:20", p.time)
        assertEquals("वाराणसी", p.place)
    }

    @Test
    fun monthFirstAndTwoDigitYear() {
        assertEquals("1990-01-12", parse("January 12, 1990").date)
        assertEquals("1990-01-12", parse("12/01/90 Patna").date)
        // 01/24 can only be month/day.
        assertEquals("1985-01-24", parse("01/24/1985").date)
    }

    @Test
    fun timeNeedsMinutesOrMarker() {
        assertNull(parse("Rahul, 12 Jan 1990, Patna 6").time)
        assertEquals("18:00", parse("12 Jan 1990 6 pm Patna").time)
        assertEquals("12:00", parse("12 Jan 1990 12 pm Patna").time)
    }

    @Test
    fun onlyPlace() {
        val p = parse("Patna")
        assertNull(p.name)
        assertNull(p.date)
        assertEquals("Patna", p.place)
    }

    @Test
    fun emptyText() {
        assertTrue(parse("   ").isEmpty)
    }
}
