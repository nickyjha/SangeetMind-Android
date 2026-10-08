package com.sangeetmind.features.astrology.chart.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.PlanetAspect

/** Classical drishti colours: green for a benefic graha's glance, red for a malefic's. */
internal val DRISHTI_BENEFIC_COLOR = Color(0xFF43A047)
internal val DRISHTI_MALEFIC_COLOR = Color(0xFFE53935)
private const val DRISHTI_LINE_ALPHA = 0.7f

internal fun DrishtiNature?.lineColor(fallback: Color): Color = when (this) {
    DrishtiNature.BENEFIC -> DRISHTI_BENEFIC_COLOR
    DrishtiNature.MALEFIC -> DRISHTI_MALEFIC_COLOR
    null -> fallback
}

/** Scales a viewBox-unit rect to pixels. */
internal fun PlanetLabelRect.scaled(scale: Float) =
    PlanetLabelRect(planet, left * scale, top * scale, right * scale, bottom * scale)

internal fun ChartPoint.scaled(scale: Float) = ChartPoint(x * scale, y * scale)

/**
 * Draws the selected planet's drishti on top of a chart: dashed lines (pixel coordinates in
 * [lines]) to each aspected house centre, thinner for partial aspects, and a rounded
 * highlight box around the planet's label. Called last in the chart's draw pass.
 */
internal fun DrawScope.drawDrishtiOverlay(
    selectedRect: PlanetLabelRect,
    lines: List<DrishtiLine>,
    nature: DrishtiNature?,
    highlightColor: Color,
    scale: Float
) {
    val color = nature.lineColor(highlightColor).copy(alpha = DRISHTI_LINE_ALPHA)
    val dash = PathEffect.dashPathEffect(floatArrayOf(7f * scale, 5f * scale), 0f)
    val fullWidth = 2.dp.toPx()
    val partialWidth = 1.2.dp.toPx()
    for (line in lines) {
        drawLine(
            color = color,
            start = Offset(line.from.x, line.from.y),
            end = Offset(line.to.x, line.to.y),
            strokeWidth = if (line.strength < 100) partialWidth else fullWidth,
            pathEffect = dash
        )
    }
    val pad = 4f * scale
    drawRoundRect(
        color = highlightColor,
        topLeft = Offset(selectedRect.left - pad, selectedRect.top - pad),
        size = Size(selectedRect.right - selectedRect.left + 2 * pad, selectedRect.bottom - selectedRect.top + 2 * pad),
        cornerRadius = CornerRadius(6f * scale, 6f * scale),
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * The legend under a chart ("- - - Benefic drishti   - - - Malefic drishti") and, when a
 * planet is selected, a one-line caption listing the houses it aspects.
 *
 * @param selectedAspects the selected planet's aspects, or null when nothing is selected.
 */
@Composable
internal fun DrishtiLegend(
    selectedPlanet: String?,
    selectedAspects: List<PlanetAspect>?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DashSample(DRISHTI_BENEFIC_COLOR)
            Text(stringResource(R.string.chart_drishti_legend_benefic), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(10.dp))
            DashSample(DRISHTI_MALEFIC_COLOR)
            Text(stringResource(R.string.chart_drishti_legend_malefic), style = MaterialTheme.typography.labelSmall)
        }
        if (selectedPlanet != null) {
            val name = astroTerm(selectedPlanet)
            val full = stringResource(R.string.chart_drishti_full)
            val caption = if (selectedAspects.isNullOrEmpty()) {
                stringResource(R.string.chart_drishti_caption_none_fmt, name)
            } else {
                val houses = selectedAspects.joinToString(", ") { it.house.toString() }
                // map {} is inline, so the composable stringResource calls are allowed inside it.
                val detail = selectedAspects.map { a ->
                    val ordinal = stringResource(R.string.chart_drishti_ordinal_fmt, a.offset, ordinalSuffix(a.offset))
                    val strength = if (a.strength >= 100) full else stringResource(R.string.chart_drishti_partial_fmt, a.strength)
                    stringResource(R.string.chart_drishti_aspect_fmt, ordinal, strength)
                }.joinToString(", ")
                stringResource(R.string.chart_drishti_caption_fmt, name, houses, detail)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(R.string.chart_drishti_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashSample(color: Color) {
    Canvas(modifier = Modifier.size(width = 22.dp, height = 8.dp).padding(end = 2.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()), 0f)
        )
    }
}
