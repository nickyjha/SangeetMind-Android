package com.sangeetmind.features.astrology.chart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.common.chart.ChartStyle
import com.sangeetmind.core.ui.chart.LocalChartStyle
import com.sangeetmind.core.ui.chart.LocalChartStyleSwitcher
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.LagnaInfo
import com.sangeetmind.libs.models.PlanetInfo

/** A house chart in the user's chosen style ([LocalChartStyle]); every screen draws through this. */
@Composable
fun HouseChart(
    lagna: LagnaInfo,
    planets: Map<String, PlanetInfo>,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    when (LocalChartStyle.current) {
        ChartStyle.SOUTH -> SouthIndianHouseChart(lagna, planets, title, subtitle, modifier)
        ChartStyle.NORTH -> NorthIndianHouseChart(lagna, planets, title, subtitle, modifier)
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
