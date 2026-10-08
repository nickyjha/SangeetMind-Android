package com.sangeetmind.features.astrology.chart.ui

import com.sangeetmind.libs.models.PlanetAspect
import com.sangeetmind.libs.models.PlanetInfo

/**
 * Pure helpers behind "tap a planet to see its drishti" on the house charts: the
 * benefic/malefic colouring rule, the tap → planet hit-test, and the line endpoints
 * (planet label centre → centre of each aspected house). All geometry is in the chart's
 * 400×400 viewBox units unless a function says otherwise; the composables scale to pixels.
 */
internal enum class DrishtiNature { BENEFIC, MALEFIC }

/**
 * Natural (naisargika) nature of a graha, which is what classical drishti colouring uses:
 * Jupiter, Venus, Mercury and the waxing Moon are benefic; Sun, Mars, Saturn, Rahu and
 * Ketu are malefic. Mercury is treated as unafflicted. Null for Lagna or unknown names.
 */
internal fun drishtiNature(planet: String, moonWaxing: Boolean = true): DrishtiNature? =
    when (planet.trim().lowercase()) {
        "jupiter", "venus", "mercury" -> DrishtiNature.BENEFIC
        "moon" -> if (moonWaxing) DrishtiNature.BENEFIC else DrishtiNature.MALEFIC
        "sun", "mars", "saturn", "rahu", "ketu" -> DrishtiNature.MALEFIC
        else -> null
    }

/** A point in viewBox (or pixel) space; kept separate from Compose's Offset so unit tests stay plain JVM. */
internal data class ChartPoint(val x: Float, val y: Float)

/** The drawn bounds of one planet label. */
internal data class PlanetLabelRect(
    val planet: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centre: ChartPoint get() = ChartPoint((left + right) / 2f, (top + bottom) / 2f)
}

private const val LABEL_ASCENT_FRACTION = 0.8f // of the font size, above the baseline
private const val LABEL_DESCENT_FRACTION = 0.25f // below the baseline

/** Bounds of a label anchored at [pos] (centre x, baseline y) with the given width at font size 1. */
internal fun planetLabelRect(planet: String, pos: LabelPos, widthPerUnitFont: Float, fontSize: Float): PlanetLabelRect {
    val halfW = widthPerUnitFont * fontSize / 2f
    return PlanetLabelRect(
        planet = planet,
        left = pos.x - halfW,
        top = pos.y - fontSize * LABEL_ASCENT_FRACTION,
        right = pos.x + halfW,
        bottom = pos.y + fontSize * LABEL_DESCENT_FRACTION
    )
}

/**
 * The planet whose label was tapped: among rects that contain the tap once grown by [slop]
 * on every side, the one whose centre is nearest. Null when the tap hits no label.
 */
internal fun hitTestPlanet(tap: ChartPoint, rects: List<PlanetLabelRect>, slop: Float): String? =
    rects.filter { r ->
        tap.x >= r.left - slop && tap.x <= r.right + slop && tap.y >= r.top - slop && tap.y <= r.bottom + slop
    }.minByOrNull { r ->
        val c = r.centre
        val dx = c.x - tap.x
        val dy = c.y - tap.y
        dx * dx + dy * dy
    }?.planet

/** One dashed line to draw: from the planet label's centre to an aspected house's centre. */
internal data class DrishtiLine(val house: Int, val offset: Int, val strength: Int, val from: ChartPoint, val to: ChartPoint)

/**
 * The planet's aspects to draw. `aspects` carries offset and strength; charts cached before it
 * existed only have `aspectsHouses`, which we treat as full aspects with the offset derived
 * from [ownHouse]. Either way the planet's own house is never a target.
 */
internal fun effectiveAspects(planet: PlanetInfo, ownHouse: Int): List<PlanetAspect> {
    val list = if (planet.aspects.isNotEmpty()) planet.aspects
    else planet.aspectsHouses.map { h -> PlanetAspect(house = h, offset = ((h - ownHouse + 12) % 12) + 1, strength = 100) }
    return list.filter { it.house in 1..12 && it.house != ownHouse }.distinctBy { it.house }.sortedBy { it.house }
}

/**
 * Lines from [from] (the selected planet's label centre) to the centre of every house in
 * [aspects]; [houseCentre] maps a house number to its centre, or null to skip that house.
 */
internal fun drishtiLines(
    from: ChartPoint,
    aspects: List<PlanetAspect>,
    houseCentre: (Int) -> ChartPoint?
): List<DrishtiLine> = aspects.mapNotNull { a ->
    val to = houseCentre(a.house) ?: return@mapNotNull null
    DrishtiLine(house = a.house, offset = a.offset, strength = a.strength, from = from, to = to)
}

/** North Indian house centres (viewBox units): the centroid of each house polygon. */
internal val NORTH_HOUSE_CENTRES: Map<Int, ChartPoint> = NORTH_HOUSE_POLYGONS.mapValues { (_, pts) ->
    ChartPoint(pts.map { it.x }.average().toFloat(), pts.map { it.y }.average().toFloat())
}

/** Centre of the South Indian cell holding [house] counted from the lagna sign (0 = Aries). */
internal fun southHouseCentre(house: Int, lagnaIdx: Int): ChartPoint? {
    if (house !in 1..12) return null
    val signIdx = (lagnaIdx + house - 1 + 12) % 12
    val (col, row) = SOUTH_CELLS[signIdx] ?: return null
    return ChartPoint(col * 100f + 50f, row * 100f + 50f)
}

/** "st"/"nd"/"rd"/"th" for an English ordinal; the Hindi string ignores it. */
internal fun ordinalSuffix(n: Int): String {
    val mod100 = n % 100
    if (mod100 in 11..13) return "th"
    return when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
