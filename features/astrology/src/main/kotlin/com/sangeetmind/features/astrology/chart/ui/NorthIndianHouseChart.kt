package com.sangeetmind.features.astrology.chart.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.LagnaInfo
import com.sangeetmind.libs.models.PlanetInfo

private val ZODIAC_SIGNS = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

private val PLANET_ABBREV = mapOf(
    "Sun" to "Su", "Moon" to "Mo", "Mercury" to "Me", "Venus" to "Ve", "Mars" to "Ma",
    "Jupiter" to "Ju", "Saturn" to "Sa", "Rahu" to "Ra", "Ketu" to "Ke", "Lagna" to "La"
)

private val DEFAULT_WHEEL_PLANET_ORDER = listOf(
    "Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu"
)

/** North Indian chart polygons in a 400×400 viewBox. */
private val HOUSE_POLYGONS: Map<Int, List<Offset>> = mapOf(
    1 to listOf(Offset(200f, 0f), Offset(300f, 100f), Offset(200f, 200f), Offset(100f, 100f)),
    2 to listOf(Offset(0f, 0f), Offset(100f, 100f), Offset(200f, 0f)),
    3 to listOf(Offset(0f, 0f), Offset(0f, 200f), Offset(100f, 100f)),
    4 to listOf(Offset(0f, 200f), Offset(100f, 300f), Offset(200f, 200f), Offset(100f, 100f)),
    5 to listOf(Offset(0f, 200f), Offset(0f, 400f), Offset(100f, 300f)),
    6 to listOf(Offset(0f, 400f), Offset(100f, 300f), Offset(200f, 400f)),
    7 to listOf(Offset(100f, 300f), Offset(200f, 400f), Offset(300f, 300f), Offset(200f, 200f)),
    8 to listOf(Offset(200f, 400f), Offset(300f, 300f), Offset(400f, 400f)),
    9 to listOf(Offset(300f, 300f), Offset(400f, 400f), Offset(400f, 200f)),
    10 to listOf(Offset(200f, 200f), Offset(300f, 300f), Offset(400f, 200f), Offset(300f, 100f)),
    11 to listOf(Offset(300f, 100f), Offset(400f, 200f), Offset(400f, 0f)),
    12 to listOf(Offset(200f, 0f), Offset(300f, 100f), Offset(400f, 0f))
)

private val HOUSE_CENTROIDS: Map<Int, Offset> = mapOf(
    1 to Offset(200f, 100f), 2 to Offset(100f, 33f), 3 to Offset(33f, 100f), 4 to Offset(100f, 200f),
    5 to Offset(33f, 300f), 6 to Offset(100f, 367f), 7 to Offset(200f, 300f), 8 to Offset(300f, 367f),
    9 to Offset(367f, 300f), 10 to Offset(300f, 200f), 11 to Offset(367f, 100f), 12 to Offset(300f, 33f)
)

internal fun signToHouseNumber(lagnaSign: String, planetSign: String): Int {
    val lagnaIdx = ZODIAC_SIGNS.indexOfFirst { it.equals(lagnaSign, ignoreCase = true) }
    val planetIdx = ZODIAC_SIGNS.indexOfFirst { it.equals(planetSign, ignoreCase = true) }
    if (lagnaIdx < 0 || planetIdx < 0) return 1
    return ((planetIdx - lagnaIdx + 12) % 12) + 1
}

internal fun dignitySuffix(planet: PlanetInfo): String = buildString {
    if (planet.retrograde) append('*')
    if (planet.combust) append('^')
    if (planet.exalted) append('↑')
    if (planet.debilitated) append('↓')
    if (planet.vargottama) append('□')
}

internal fun degreeDisplay(planet: PlanetInfo): String? {
    planet.absoluteDms?.takeIf { it.isNotBlank() }?.let { return it }
    if (planet.degree.isFinite()) return "${planet.degree.toInt()}°"
    val abs = planet.absolute.toDoubleOrNull()
    if (abs != null) return "${(abs % 30).toInt()}°"
    return null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NorthIndianHouseChart(
    lagna: LagnaInfo,
    planets: Map<String, PlanetInfo>,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val lagnaSign = lagna.sign.ifBlank { "Aries" }
    val houseToPlanets = (1..12).associateWith { mutableListOf<Pair<String, PlanetInfo>>() }.toMutableMap()
    houseToPlanets.getValue(1).add(
        "Lagna" to PlanetInfo(
            sign = lagnaSign,
            degree = lagna.degree,
            absolute = lagna.absolute.toString(),
            absoluteDms = lagna.absoluteDms
        )
    )
    for (name in DEFAULT_WHEEL_PLANET_ORDER) {
        val planet = planets[name] ?: continue
        if (planet.sign.isBlank()) continue
        val house = planet.house?.takeIf { it in 1..12 }
            ?: signToHouseNumber(lagnaSign, planet.sign)
        houseToPlanets.getValue(house).add(name to planet)
    }

    val lagnaIdx = ZODIAC_SIGNS.indexOfFirst { it.equals(lagnaSign, ignoreCase = true) }
        .coerceAtLeast(0)
    val houseToRashi = (1..12).associateWith { h -> ((lagnaIdx + h - 1 + 12) % 12) + 1 }

    val fillColor = MaterialTheme.colorScheme.surfaceVariant
    val glowColor = MaterialTheme.colorScheme.primary
    val strokeColor = MaterialTheme.colorScheme.primary
    val innerLineColor = LocalGrahaColors.current.rahu
    val rashiArgb = MaterialTheme.colorScheme.primary.toArgb()
    val planetArgb = MaterialTheme.colorScheme.onSurface.toArgb()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(vertical = 8.dp)
        ) {
            val scale = size.minDimension / 400f
            fun Offset.scaled() = Offset(x * scale, y * scale)

            // Soft radial glow behind the lagna house, echoing the sun at the chart's center.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(200f, 200f).scaled(),
                    radius = 170f * scale
                ),
                radius = 170f * scale,
                center = Offset(200f, 200f).scaled()
            )

            HOUSE_POLYGONS.forEach { (houseNum, points) ->
                val path = Path().apply {
                    val first = points.first().scaled()
                    moveTo(first.x, first.y)
                    points.drop(1).forEach { lineTo(it.scaled().x, it.scaled().y) }
                    close()
                }
                drawPath(path, color = fillColor)
                // Inner diagonals (houses 1,4,7,10) in the violet accent; outer boundary in gold.
                val lineColor = if (houseNum in setOf(1, 4, 7, 10)) innerLineColor.copy(alpha = 0.55f)
                else strokeColor.copy(alpha = 0.5f)
                drawPath(path, color = lineColor, style = Stroke(width = 1.4f * scale))
            }

            val rashiPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = rashiArgb
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 11f * scale
                isFakeBoldText = true
            }

            val planetPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = planetArgb
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 9f * scale
            }

            for (houseNum in 1..12) {
                val centroid = HOUSE_CENTROIDS.getValue(houseNum).scaled()
                val items = houseToPlanets.getValue(houseNum)
                val rashiY = centroid.y - if (items.isNotEmpty()) 18f * scale else 0f
                drawContext.canvas.nativeCanvas.drawText(
                    houseToRashi.getValue(houseNum).toString(),
                    centroid.x,
                    rashiY,
                    rashiPaint
                )
                items.forEachIndexed { i, (name, data) ->
                    val abbrev = PLANET_ABBREV[name] ?: name.take(2)
                    val dign = dignitySuffix(data)
                    val deg = degreeDisplay(data)
                    val line = if (deg != null) "$abbrev$dign $deg" else "$abbrev$dign"
                    drawContext.canvas.nativeCanvas.drawText(
                        line,
                        centroid.x,
                        centroid.y + 4f * scale + i * 12f * scale,
                        planetPaint
                    )
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(stringResource(R.string.chart_legend_retrograde), style = MaterialTheme.typography.labelSmall)
            Text(stringResource(R.string.chart_legend_combust), style = MaterialTheme.typography.labelSmall)
            Text(stringResource(R.string.chart_legend_exalted), style = MaterialTheme.typography.labelSmall)
            Text(stringResource(R.string.chart_legend_debilitated), style = MaterialTheme.typography.labelSmall)
            Text(stringResource(R.string.chart_legend_vargottama), style = MaterialTheme.typography.labelSmall)
        }
    }
}
