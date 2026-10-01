package com.sangeetmind.features.astrology.chart.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.LagnaInfo
import com.sangeetmind.libs.models.PlanetInfo

/** Sign index (0 = Aries) → grid cell (column, row) in the fixed South Indian layout:
 * Pisces top-left, then Aries, Taurus, Gemini across the top, clockwise round to Aquarius. */
private val SOUTH_CELLS: Map<Int, Pair<Int, Int>> = mapOf(
    11 to (0 to 0), 0 to (1 to 0), 1 to (2 to 0), 2 to (3 to 0),
    3 to (3 to 1), 4 to (3 to 2), 5 to (3 to 3),
    6 to (2 to 3), 7 to (1 to 3), 8 to (0 to 3),
    9 to (0 to 2), 10 to (0 to 1)
)

/**
 * South Indian chart: signs sit in fixed cells, the lagna's cell carries the classical
 * corner stroke, and houses are counted clockwise from it (the small number in each cell).
 * Same inputs and planet labels as [NorthIndianHouseChart], so the two are interchangeable.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SouthIndianHouseChart(
    lagna: LagnaInfo,
    planets: Map<String, PlanetInfo>,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val lagnaSign = lagna.sign.ifBlank { "Aries" }
    val lagnaIdx = ZODIAC_SIGNS.indexOfFirst { it.equals(lagnaSign, ignoreCase = true) }.coerceAtLeast(0)
    val signToPlanets = (0..11).associateWith { mutableListOf<Pair<String, PlanetInfo>>() }
    signToPlanets.getValue(lagnaIdx).add(
        "Lagna" to PlanetInfo(
            sign = lagnaSign,
            degree = lagna.degree,
            absolute = lagna.absolute.toString(),
            absoluteDms = lagna.absoluteDms
        )
    )
    for (name in DEFAULT_WHEEL_PLANET_ORDER) {
        val planet = planets[name] ?: continue
        val idx = ZODIAC_SIGNS.indexOfFirst { it.equals(planet.sign, ignoreCase = true) }
        if (idx < 0) continue
        signToPlanets.getValue(idx).add(name to planet)
    }

    val planetAbbrev = planetAbbreviations()
    val signLabels = ZODIAC_SIGNS.map { sign -> astroTerm(sign).let { if (it.length > 4) it.take(3) else it } }
    val fillColor = MaterialTheme.colorScheme.surfaceVariant
    val glowColor = MaterialTheme.colorScheme.primary
    val strokeColor = MaterialTheme.colorScheme.primary
    val lagnaColor = LocalGrahaColors.current.surya
    val signArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val houseArgb = MaterialTheme.colorScheme.primary.toArgb()
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
            val cell = 100f * scale

            // The empty centre gets the same soft glow as the North chart.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(200f * scale, 200f * scale),
                    radius = 120f * scale
                ),
                radius = 120f * scale,
                center = Offset(200f * scale, 200f * scale)
            )

            val signPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = signArgb
                textAlign = android.graphics.Paint.Align.LEFT
                textSize = 8f * scale
            }
            val housePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = houseArgb
                textAlign = android.graphics.Paint.Align.RIGHT
                textSize = 10f * scale
                isFakeBoldText = true
            }
            val planetPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = planetArgb
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 9f * scale
            }

            for ((signIdx, pos) in SOUTH_CELLS) {
                val (col, row) = pos
                val left = col * cell
                val top = row * cell
                drawRect(fillColor, topLeft = Offset(left, top), size = Size(cell, cell))
                drawRect(
                    strokeColor.copy(alpha = 0.5f),
                    topLeft = Offset(left, top),
                    size = Size(cell, cell),
                    style = Stroke(width = 1.4f * scale)
                )
                if (signIdx == lagnaIdx) {
                    // Classical lagna mark: a stroke across the cell's top-left corner.
                    drawLine(
                        lagnaColor,
                        Offset(left, top + cell * 0.32f),
                        Offset(left + cell * 0.32f, top),
                        strokeWidth = 2.2f * scale
                    )
                }
                val house = ((signIdx - lagnaIdx + 12) % 12) + 1
                drawContext.canvas.nativeCanvas.drawText(signLabels[signIdx], left + 4f * scale, top + 11f * scale, signPaint)
                drawContext.canvas.nativeCanvas.drawText(house.toString(), left + cell - 4f * scale, top + 12f * scale, housePaint)
                signToPlanets.getValue(signIdx).forEachIndexed { i, (name, data) ->
                    val abbrev = planetAbbrev[name] ?: name.take(2)
                    val dign = dignitySuffix(data)
                    val deg = degreeDisplay(data)
                    val line = if (deg != null) "$abbrev$dign $deg" else "$abbrev$dign"
                    drawContext.canvas.nativeCanvas.drawText(
                        line,
                        left + cell / 2f,
                        top + 28f * scale + i * 12f * scale,
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
