package com.sangeetmind.features.astrology.chart.ui

import com.sangeetmind.libs.models.ChartShadbala
import com.sangeetmind.libs.models.ShadbalaPlanet

/** Pure helpers behind [ShadbalaCard] (no Compose), so the chip/weak-list logic is unit-testable. */

/** Chip tone for a planet: STRONG when it meets its own BPHS minimum, WEAK when it does not. */
enum class ShadbalaTone { STRONG, WEAK }

/** Weak = strength_ratio < 1 (same rule as the backend's `summary.weak_planets`); the label is
 * only a fallback for payloads that carry a label but no ratio. Null when neither is present,
 * so the card hides the chip cleanly against an older backend. */
internal fun shadbalaTone(ratio: Double?, label: String?): ShadbalaTone? = when {
    ratio != null -> if (ratio >= 1.0) ShadbalaTone.STRONG else ShadbalaTone.WEAK
    label == "weak" || label == "very_weak" -> ShadbalaTone.WEAK
    label == "very_strong" || label == "strong" || label == "adequate" -> ShadbalaTone.STRONG
    else -> null
}

/** Planets in display order, strongest first: by rank_by_ratio, then strength_ratio, then the
 * legacy total-virupas ranking when the backend sends no ratios. */
internal fun shadbalaDisplayOrder(shadbala: ChartShadbala): List<String> {
    val planets = shadbala.planets
    val hasRatio = planets.values.any { it.strengthRatio != null }
    return if (hasRatio) {
        planets.entries
            .sortedWith(
                compareBy<Map.Entry<String, ShadbalaPlanet>> { it.value.rankByRatio ?: Int.MAX_VALUE }
                    .thenByDescending { it.value.strengthRatio ?: Double.NEGATIVE_INFINITY }
                    .thenByDescending { it.value.totalVirupas }
            )
            .map { it.key }
    } else {
        shadbala.ranking.map { it.planet }.filter { it in planets }.ifEmpty {
            planets.entries.sortedByDescending { it.value.totalVirupas }.map { it.key }
        }
    }
}

/** Planets whose strength_ratio < 1, weakest first. Prefers the backend's explicit list and
 * falls back to the per-planet ratios; empty when neither is available. */
internal fun weakShadbalaPlanets(shadbala: ChartShadbala): List<String> {
    val explicit = shadbala.summary?.weakPlanets.orEmpty().filter { it in shadbala.planets }
    if (explicit.isNotEmpty()) return explicit
    return shadbala.planets.entries
        .filter { (it.value.strengthRatio ?: 1.0) < 1.0 }
        .sortedBy { it.value.strengthRatio }
        .map { it.key }
}

/** Japa mantra id for a graha's beej mantra (features/astrology/sangeet/JapaMantras.kt). */
internal fun beejMantraIdFor(planet: String): String? = when (planet) {
    "Sun" -> "surya_beej"
    "Moon" -> "chandra_beej"
    "Mars" -> "mangal_beej"
    "Mercury" -> "budh_beej"
    "Jupiter" -> "guru_beej"
    "Venus" -> "shukra_beej"
    "Saturn" -> "shani_beej"
    else -> null
}

/** The one-sentence summary in the app language; Hindi falls back to English when missing. */
internal fun shadbalaSummaryText(planet: ShadbalaPlanet, languageCode: String): String? {
    val hi = planet.summaryHi?.takeIf { it.isNotBlank() }
    val en = planet.summary?.takeIf { it.isNotBlank() }
    return if (languageCode == "hi") hi ?: en else en
}
