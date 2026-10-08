package com.sangeetmind.features.astrology.chart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.common.chart.ChartStyle
import com.sangeetmind.core.ui.chart.LocalChartStyle
import com.sangeetmind.core.ui.chart.LocalChartStyleSwitcher
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.LagnaInfo
import com.sangeetmind.libs.models.PlanetInfo

/**
 * Which planet's drishti a chart shows. [onTap] is the tap-to-toggle rule: a new planet
 * selects it, the same planet again (or empty space, null) clears the selection.
 */
class PlanetSelection(val planet: String?, val onTap: (String?) -> Unit)

/** Hoisted, process-death-safe selection for one chart; it resets whenever [keys] change (e.g. the chart shown). */
@Composable
fun rememberPlanetSelection(vararg keys: Any?): PlanetSelection {
    var selected by rememberSaveable(*keys) { mutableStateOf<String?>(null) }
    return PlanetSelection(selected) { tapped -> selected = if (tapped == null || tapped == selected) null else tapped }
}

/**
 * A house chart in the user's chosen style ([LocalChartStyle]); every screen draws through this.
 * Pass a [selection] (see [rememberPlanetSelection]) to let the viewer tap a planet and see
 * its drishti; [moonWaxing] colours the Moon's lines as benefic or malefic.
 */
@Composable
fun HouseChart(
    lagna: LagnaInfo,
    planets: Map<String, PlanetInfo>,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    selection: PlanetSelection? = null,
    moonWaxing: Boolean = true
) {
    val selected = selection?.planet
    val onTap = selection?.onTap ?: {}
    when (LocalChartStyle.current) {
        ChartStyle.SOUTH -> SouthIndianHouseChart(lagna, planets, title, subtitle, modifier, selected, onTap, moonWaxing)
        ChartStyle.NORTH -> NorthIndianHouseChart(lagna, planets, title, subtitle, modifier, selected, onTap, moonWaxing)
    }
}

/** "North Indian chart" / "South Indian chart" for the current style. */
@Composable
fun chartStyleSubtitle(): String = stringResource(
    if (LocalChartStyle.current == ChartStyle.SOUTH) R.string.chart_subtitle_south_indian
    else R.string.chart_subtitle_north_indian
)

/** Two chips that switch the app-wide chart style. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartStyleSelector(modifier: Modifier = Modifier) {
    val style = LocalChartStyle.current
    val switch = LocalChartStyleSwitcher.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChartStyle.entries.forEach { option ->
            FilterChip(
                selected = style == option,
                onClick = { switch(option) },
                label = {
                    Text(
                        stringResource(
                            if (option == ChartStyle.SOUTH) R.string.chart_style_south else R.string.chart_style_north
                        )
                    )
                }
            )
        }
    }
}
