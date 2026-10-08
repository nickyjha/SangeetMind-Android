package com.sangeetmind.features.astrology.chart

import com.sangeetmind.features.astrology.chart.ui.ChartPoint
import com.sangeetmind.features.astrology.chart.ui.DrishtiNature
import com.sangeetmind.features.astrology.chart.ui.LabelPos
import com.sangeetmind.features.astrology.chart.ui.NORTH_HOUSE_CENTRES
import com.sangeetmind.features.astrology.chart.ui.PlanetLabelRect
import com.sangeetmind.features.astrology.chart.ui.drishtiLines
import com.sangeetmind.features.astrology.chart.ui.drishtiNature
import com.sangeetmind.features.astrology.chart.ui.effectiveAspects
import com.sangeetmind.features.astrology.chart.ui.hitTestPlanet
import com.sangeetmind.features.astrology.chart.ui.ordinalSuffix
import com.sangeetmind.features.astrology.chart.ui.planetLabelRect
import com.sangeetmind.features.astrology.chart.ui.southHouseCentre
import com.sangeetmind.libs.models.PlanetAspect
import com.sangeetmind.libs.models.PlanetInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartDrishtiTest {

    // --- benefic / malefic rule ---

    @Test
    fun naturalBeneficsAreGreen() {
        for (p in listOf("Jupiter", "Venus", "Mercury", "Moon")) {
            assertEquals(p, DrishtiNature.BENEFIC, drishtiNature(p))
        }
    }

    @Test
    fun naturalMaleficsAreRed() {
        for (p in listOf("Sun", "Mars", "Saturn", "Rahu", "Ketu")) {
            assertEquals(p, DrishtiNature.MALEFIC, drishtiNature(p))
        }
    }

    @Test
    fun waningMoonIsMalefic() {
        assertEquals(DrishtiNature.MALEFIC, drishtiNature("Moon", moonWaxing = false))
        assertEquals(DrishtiNature.BENEFIC, drishtiNature("Moon", moonWaxing = true))
    }

    @Test
    fun ruleIgnoresCaseAndRejectsLagna() {
        assertEquals(DrishtiNature.MALEFIC, drishtiNature("saturn"))
        assertNull(drishtiNature("Lagna"))
        assertNull(drishtiNature(""))
    }

    // --- hit-test ---

    private val rects = listOf(
        PlanetLabelRect("Sun", left = 10f, top = 10f, right = 40f, bottom = 20f),
        PlanetLabelRect("Moon", left = 10f, top = 30f, right = 40f, bottom = 40f),
        PlanetLabelRect("Mars", left = 100f, top = 100f, right = 130f, bottom = 110f)
    )

    @Test
    fun tapInsideLabelReturnsThatPlanet() {
        assertEquals("Mars", hitTestPlanet(ChartPoint(115f, 105f), rects, slop = 8f))
    }

    @Test
    fun tapWithinSlopStillHits() {
        // 5 units right of Mars's right edge, inside the 8-unit slop.
        assertEquals("Mars", hitTestPlanet(ChartPoint(135f, 105f), rects, slop = 8f))
        assertNull(hitTestPlanet(ChartPoint(145f, 105f), rects, slop = 8f))
    }

    @Test
    fun tapInOverlapPicksNearestCentre() {
        // y = 26 is within slop of both Sun (bottom 20) and Moon (top 30); Moon's centre (35) is nearer than Sun's (15).
        assertEquals("Moon", hitTestPlanet(ChartPoint(25f, 26f), rects, slop = 8f))
        assertEquals("Sun", hitTestPlanet(ChartPoint(25f, 23f), rects, slop = 8f))
    }

    @Test
    fun tapOnEmptySpaceReturnsNull() {
        assertNull(hitTestPlanet(ChartPoint(300f, 300f), rects, slop = 8f))
        assertNull(hitTestPlanet(ChartPoint(300f, 300f), emptyList(), slop = 8f))
    }

    @Test
    fun labelRectSurroundsTheDrawnText() {
        // Label anchored at centre x = 200, baseline y = 100, 3.6 em wide at 10 units.
        val r = planetLabelRect("Sun", LabelPos(200f, 100f), widthPerUnitFont = 3.6f, fontSize = 10f)
        assertEquals(182f, r.left, 0.001f)
        assertEquals(218f, r.right, 0.001f)
        assertTrue(r.top < 100f && r.top > 90f)
        assertTrue(r.bottom > 100f && r.bottom < 105f)
        assertEquals(200f, r.centre.x, 0.001f)
    }

    // --- line endpoints ---

    @Test
    fun northLinesRunFromLabelCentreToHouseCentres() {
        val from = ChartPoint(100f, 27f) // Jupiter's label in house 2
        val aspects = listOf(
            PlanetAspect(house = 6, offset = 5, strength = 100),
            PlanetAspect(house = 8, offset = 7, strength = 100),
            PlanetAspect(house = 10, offset = 9, strength = 100)
        )
        val lines = drishtiLines(from, aspects) { NORTH_HOUSE_CENTRES[it] }
        assertEquals(listOf(6, 8, 10), lines.map { it.house })
        lines.forEach { assertEquals(from, it.from) }
        // House 10 is the right diamond, centred on (300, 200); house 6 the bottom-left triangle.
        assertEquals(ChartPoint(300f, 200f), lines.last().to)
        assertEquals(ChartPoint(100f, 1100f / 3f), lines.first().to)
        assertEquals(ChartPoint(200f, 100f), NORTH_HOUSE_CENTRES.getValue(1))
    }

    @Test
    fun southHouseCentreFollowsTheLagnaCell() {
        // Aries lagna: house 1 is Aries's cell (column 1, row 0); house 7 is Libra (column 2, row 3).
        assertEquals(ChartPoint(150f, 50f), southHouseCentre(1, lagnaIdx = 0))
        assertEquals(ChartPoint(250f, 350f), southHouseCentre(7, lagnaIdx = 0))
        // Capricorn lagna (idx 9, cell column 0 row 2): house 4 is Aries.
        assertEquals(ChartPoint(50f, 250f), southHouseCentre(1, lagnaIdx = 9))
        assertEquals(ChartPoint(150f, 50f), southHouseCentre(4, lagnaIdx = 9))
        assertNull(southHouseCentre(13, lagnaIdx = 0))
    }

    @Test
    fun linesSkipHousesWithoutACentre() {
        val lines = drishtiLines(ChartPoint(0f, 0f), listOf(PlanetAspect(house = 7, offset = 7))) { null }
        assertTrue(lines.isEmpty())
    }

    @Test
    fun partialStrengthIsCarriedOnTheLine() {
        val lines = drishtiLines(ChartPoint(0f, 0f), listOf(PlanetAspect(house = 4, offset = 3, strength = 75))) {
            NORTH_HOUSE_CENTRES[it]
        }
        assertEquals(75, lines.single().strength)
        assertEquals(3, lines.single().offset)
    }

    // --- aspects fallback ---

    @Test
    fun effectiveAspectsPreferStructuredListAndDropOwnHouse() {
        val mars = PlanetInfo(
            sign = "Aries",
            aspects = listOf(PlanetAspect(1, 1, 100), PlanetAspect(4, 4, 100), PlanetAspect(7, 7, 100), PlanetAspect(8, 8, 100)),
            aspectsHouses = listOf(4, 7, 8)
        )
        assertEquals(listOf(4, 7, 8), effectiveAspects(mars, ownHouse = 1).map { it.house })
    }

    @Test
    fun effectiveAspectsFallBackToHouseListWithDerivedOffsets() {
        val saturn = PlanetInfo(sign = "Leo", aspectsHouses = listOf(7, 12, 2))
        val result = effectiveAspects(saturn, ownHouse = 5)
        assertEquals(listOf(2, 7, 12), result.map { it.house })
        assertEquals(mapOf(2 to 10, 7 to 3, 12 to 8), result.associate { it.house to it.offset })
        assertTrue(result.all { it.strength == 100 })
    }

    @Test
    fun ordinalSuffixes() {
        assertEquals("rd", ordinalSuffix(3))
        assertEquals("th", ordinalSuffix(4))
        assertEquals("th", ordinalSuffix(5))
        assertEquals("th", ordinalSuffix(7))
        assertEquals("th", ordinalSuffix(10))
        assertEquals("nd", ordinalSuffix(2))
        assertEquals("th", ordinalSuffix(12))
    }
}
