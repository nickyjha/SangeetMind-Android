package com.sangeetmind.features.astrology.chart

import com.sangeetmind.features.astrology.chart.ui.ShadbalaTone
import com.sangeetmind.features.astrology.chart.ui.beejMantraIdFor
import com.sangeetmind.features.astrology.chart.ui.shadbalaDisplayOrder
import com.sangeetmind.features.astrology.chart.ui.shadbalaSummaryText
import com.sangeetmind.features.astrology.chart.ui.shadbalaTone
import com.sangeetmind.features.astrology.chart.ui.weakShadbalaPlanets
import com.sangeetmind.libs.models.ChartShadbala
import com.sangeetmind.libs.models.ShadbalaPlanet
import com.sangeetmind.libs.models.ShadbalaRanking
import com.sangeetmind.libs.models.ShadbalaSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure helpers behind the Shadbala card (ShadbalaSummary.kt). Values mirror the backend's
 * Nagda reference chart: Saturn 1.33 (rank 1) … Jupiter 0.879 (rank 7, the only weak one). */
class ShadbalaSummaryTest {
    private fun planet(
        virupas: Double,
        ratio: Double? = null,
        rank: Int? = null,
        label: String? = null,
        summary: String? = null,
        summaryHi: String? = null
    ) = ShadbalaPlanet(
        totalVirupas = virupas,
        totalRupas = virupas / 60.0,
        strengthRatio = ratio,
        rankByRatio = rank,
        strengthLabel = label,
        summary = summary,
        summaryHi = summaryHi
    )

    // Note Sun has more virupas than Jupiter but Jupiter is ranked by ratio, not raw total.
    private val nagda = ChartShadbala(
        planets = mapOf(
            "Sun" to planet(393.0, 1.01, 6, "adequate"),
            "Moon" to planet(389.0, 1.081, 5, "adequate"),
            "Mars" to planet(386.0, 1.285, 2, "strong"),
            "Mercury" to planet(516.0, 1.229, 3, "strong"),
            "Jupiter" to planet(343.0, 0.879, 7, "weak"),
            "Venus" to planet(399.0, 1.209, 4, "strong"),
            "Saturn" to planet(399.0, 1.33, 1, "very_strong")
        ),
        ranking = listOf(ShadbalaRanking("Mercury", 516.0), ShadbalaRanking("Saturn", 399.0)),
        summary = ShadbalaSummary(strongest = "Saturn", weakest = "Jupiter", weakPlanets = listOf("Jupiter"))
    )

    @Test
    fun toneFollowsRatioAgainstOwnMinimum() {
        assertEquals(ShadbalaTone.STRONG, shadbalaTone(1.33, "very_strong"))
        assertEquals(ShadbalaTone.STRONG, shadbalaTone(1.0, "adequate"))
        assertEquals(ShadbalaTone.WEAK, shadbalaTone(0.999, "weak"))
        assertEquals(ShadbalaTone.WEAK, shadbalaTone(0.5, null))
        // Ratio wins over a label that disagrees.
        assertEquals(ShadbalaTone.STRONG, shadbalaTone(1.2, "weak"))
    }

    @Test
    fun toneFallsBackToLabelAndHidesWhenNeitherPresent() {
        assertEquals(ShadbalaTone.WEAK, shadbalaTone(null, "very_weak"))
        assertEquals(ShadbalaTone.STRONG, shadbalaTone(null, "adequate"))
        assertNull(shadbalaTone(null, null))
        assertNull(shadbalaTone(null, "something_else"))
    }

    @Test
    fun displayOrderUsesRatioRankNotRawVirupas() {
        assertEquals(
            listOf("Saturn", "Mars", "Mercury", "Venus", "Moon", "Sun", "Jupiter"),
            shadbalaDisplayOrder(nagda)
        )
    }

    @Test
    fun displayOrderWithoutRanksSortsByRatioThenVirupas() {
        val noRanks = ChartShadbala(
            planets = mapOf(
                "Sun" to planet(100.0, 1.1),
                "Moon" to planet(500.0, 0.9),
                "Mars" to planet(200.0, 1.1)
            )
        )
        assertEquals(listOf("Mars", "Sun", "Moon"), shadbalaDisplayOrder(noRanks))
    }

    @Test
    fun displayOrderFallsBackToLegacyRankingWithoutRatios() {
        val legacy = ChartShadbala(
            planets = mapOf("Sun" to planet(100.0), "Moon" to planet(300.0), "Mars" to planet(200.0)),
            ranking = listOf(ShadbalaRanking("Moon", 300.0), ShadbalaRanking("Mars", 200.0), ShadbalaRanking("Sun", 100.0))
        )
        assertEquals(listOf("Moon", "Mars", "Sun"), shadbalaDisplayOrder(legacy))
        // No ranking list either: sort by virupas.
        assertEquals(listOf("Moon", "Mars", "Sun"), shadbalaDisplayOrder(legacy.copy(ranking = emptyList())))
    }

    @Test
    fun weakPlanetsPreferBackendListThenRatios() {
        assertEquals(listOf("Jupiter"), weakShadbalaPlanets(nagda))
        val fromRatios = nagda.copy(summary = null, planets = nagda.planets + ("Sun" to planet(393.0, 0.95, 6)))
        assertEquals(listOf("Jupiter", "Sun"), weakShadbalaPlanets(fromRatios))
        // Older backend: no ratios, no summary -> nothing to chant for (no buttons shown).
        val legacy = ChartShadbala(planets = mapOf("Sun" to planet(100.0), "Jupiter" to planet(50.0)))
        assertEquals(emptyList<String>(), weakShadbalaPlanets(legacy))
    }

    @Test
    fun beejMantraIdsMatchJapaMantras() {
        assertEquals("surya_beej", beejMantraIdFor("Sun"))
        assertEquals("chandra_beej", beejMantraIdFor("Moon"))
        assertEquals("mangal_beej", beejMantraIdFor("Mars"))
        assertEquals("budh_beej", beejMantraIdFor("Mercury"))
        assertEquals("guru_beej", beejMantraIdFor("Jupiter"))
        assertEquals("shukra_beej", beejMantraIdFor("Venus"))
        assertEquals("shani_beej", beejMantraIdFor("Saturn"))
        assertNull(beejMantraIdFor("Rahu"))
    }

    @Test
    fun summaryTextPicksHindiOnlyForHindiAndFallsBack() {
        val both = planet(1.0, summary = "Strongest.", summaryHi = "सबसे मज़बूत।")
        assertEquals("Strongest.", shadbalaSummaryText(both, "en"))
        assertEquals("सबसे मज़बूत।", shadbalaSummaryText(both, "hi"))
        val enOnly = planet(1.0, summary = "Strongest.", summaryHi = " ")
        assertEquals("Strongest.", shadbalaSummaryText(enOnly, "hi"))
        assertNull(shadbalaSummaryText(planet(1.0), "en"))
    }
}
