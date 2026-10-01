package com.sangeetmind.core.ui.chart

import androidx.compose.runtime.staticCompositionLocalOf
import com.sangeetmind.core.common.chart.ChartStyle

/** The chart drawing style in use. Installed by MainActivity. */
val LocalChartStyle = staticCompositionLocalOf { ChartStyle.DEFAULT }

/** How a screen asks to switch chart style. Installed by MainActivity; no DI needed in features. */
val LocalChartStyleSwitcher = staticCompositionLocalOf<(ChartStyle) -> Unit> { {} }
