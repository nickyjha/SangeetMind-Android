package com.sangeetmind.features.astrology.chart

import com.sangeetmind.features.astrology.chart.ui.HouseLabelLayout
import com.sangeetmind.features.astrology.chart.ui.LABEL_LINE_HEIGHT
import com.sangeetmind.features.astrology.chart.ui.NORTH_SAFE_RECTS
import com.sangeetmind.features.astrology.chart.ui.SafeRect
import com.sangeetmind.features.astrology.chart.ui.layoutHouseLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartLabelLayoutTest {
    // "Su□ 29°" is ~3.6 em wide in the default sans font.
    private val labelW = 3.6f

    private fun assertInside(rect: SafeRect, layout: HouseLabelLayout, widths: List<Float>) {
        val eps = 0.01f
        layout.labels.forEachIndexed { i, p ->
            val half = widths[i] * layout.fontSize / 2f
            assertTrue("label $i left", p.x - half >= rect.left - eps)
            assertTrue("label $i right", p.x + half <= rect.left + rect.width + eps)
            assertTrue("label $i top", p.y - layout.fontSize * LABEL_LINE_HEIGHT * 0.8f >= rect.top - eps)
            assertTrue("label $i bottom", p.y <= rect.top + rect.height + eps)
        }
    }

    @Test
    fun fewPlanetsKeepOneColumnAtFullSize() {
        val rect = NORTH_SAFE_RECTS.getValue(1)
        val layout = layoutHouseLabels(rect, listOf(labelW, labelW), baseFont = 9f, minFont = 8f, headerFont = 11f)
        assertEquals(1, layout.columns)
        assertEquals(9f, layout.fontSize, 0.001f)
        assertTrue(layout.fits)
    }

    @Test
    fun fourPlanetsInTopTriangleUseTwoColumnsAndStayInside() {
        val rect = NORTH_SAFE_RECTS.getValue(2)
        val widths = List(4) { labelW }
        val layout = layoutHouseLabels(rect, widths, baseFont = 9f, minFont = 8f, headerFont = 11f)
        assertTrue(layout.fits)
        assertEquals(2, layout.columns)
        assertTrue(layout.fontSize >= 8f)
        assertInside(rect, layout, widths)
    }

    @Test
    fun fourPlanetsInSideTriangleStackInOneColumn() {
        val rect = NORTH_SAFE_RECTS.getValue(3)
        val widths = List(4) { labelW }
        val layout = layoutHouseLabels(rect, widths, baseFont = 9f, minFont = 8f, headerFont = 11f)
        assertTrue(layout.fits)
        assertEquals(1, layout.columns)
        assertInside(rect, layout, widths)
    }

    @Test
    fun crowdedDiamondFitsAndNeverGoesBelowMinFont() {
        val rect = NORTH_SAFE_RECTS.getValue(10)
        val widths = List(6) { labelW }
        val layout = layoutHouseLabels(rect, widths, baseFont = 9f, minFont = 8f, headerFont = 11f)
        assertTrue(layout.fits)
        assertTrue(layout.fontSize >= 8f)
        assertInside(rect, layout, widths)
    }

    @Test
    fun everyNorthHouseFitsFourPlanets() {
        val widths = List(4) { labelW }
        for ((house, rect) in NORTH_SAFE_RECTS) {
            val layout = layoutHouseLabels(rect, widths, baseFont = 9f, minFont = 8f, headerFont = 11f)
            assertTrue("house $house", layout.fits)
            assertInside(rect, layout, widths)
        }
    }

    @Test
    fun headerSitsAboveLabelsAndBlockIsCentred() {
        val rect = SafeRect(100f, 100f, 90f, 90f)
        val layout = layoutHouseLabels(rect, listOf(labelW), baseFont = 9f, minFont = 8f, headerFont = 11f)
        val header = layout.header!!
        assertTrue(header.y < layout.labels[0].y)
        assertEquals(rect.cx, header.x, 0.001f)
        assertEquals(rect.cx, layout.labels[0].x, 0.001f)
    }

    @Test
    fun emptyHouseOnlyHasHeaderAtCentre() {
        val rect = NORTH_SAFE_RECTS.getValue(5)
        val layout = layoutHouseLabels(rect, emptyList(), baseFont = 9f, minFont = 8f, headerFont = 11f)
        assertTrue(layout.labels.isEmpty())
        // Header line box is centred on the safe area.
        val lineH = 11f * LABEL_LINE_HEIGHT
        assertEquals(rect.cy - lineH / 2f + lineH * 0.8f, layout.header!!.y, 0.001f)
    }

    @Test
    fun oddLastRowIsCentred() {
        val rect = SafeRect(200f, 200f, 110f, 84f)
        val layout = layoutHouseLabels(rect, List(5) { labelW }, baseFont = 12f, minFont = 8f)
        // 5 items in 2 columns → last row has a single centred item.
        if (layout.columns == 2) assertEquals(rect.cx, layout.labels[4].x, 0.001f)
    }
}
