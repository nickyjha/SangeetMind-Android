package com.sangeetmind.features.astrology.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.dashboard.ui.Graha

/**
 * The five bottom tabs. [route] is the NavHost route of the tab's root screen — Home
 * keeps the historic "dashboard" route so push deep links / the post-auth start
 * destination keep working. Ask has no root of its own: it pushes ChatMind.
 */
enum class MainTab(val route: String) {
    HOME("dashboard"),
    KUNDLI("tab_kundli"),
    ASK("chatmind"),
    CALENDAR("tab_calendar"),
    ME("tab_me");

    companion object {
        /** Tab roots that show the bottom bar (Ask opens a full screen, so not here). */
        val barRoutes: Set<String> = setOf(HOME.route, KUNDLI.route, CALENDAR.route, ME.route)
        fun forRoute(route: String?): MainTab? = entries.firstOrNull { it.route == route && it != ASK }
    }
}

/** The theme-correct tone for a graha (bright in dark theme, deep in light). */
@Composable
fun Graha.themedTone(): Color {
    val c = LocalGrahaColors.current
    return when (this) {
        Graha.SURYA -> c.surya
        Graha.CHANDRA -> c.chandra
        Graha.MANGALA -> c.mangala
        Graha.BUDHA -> c.budha
        Graha.GURU -> c.guru
        Graha.SHUKRA -> c.shukra
        Graha.SHANI -> c.shani
        Graha.RAHU -> c.rahu
    }
}

/**
 * Bottom navigation: Home · Kundli · (Ask) · Calendar · Me. Ask sits in the centre as a
 * raised saffron disc — ChatMind is the core AI feature, so it gets the emphasis
 * (same pattern as Melooha's centre "Ask").
 */
@Composable
fun MainBottomBar(
    current: MainTab?,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            TabItem(MainTab.HOME, current, Icons.Filled.Home, Icons.Outlined.Home, R.string.dashboard_tab_home, onSelect)
            TabItem(MainTab.KUNDLI, current, Icons.Filled.DonutLarge, Icons.Outlined.DonutLarge, R.string.dashboard_tab_kundli, onSelect)
            // Centre slot: label only — the raised disc is drawn over it below.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(MainTab.ASK) }
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(modifier = Modifier.height(44.dp))
                Text(
                    stringResource(R.string.dashboard_tab_ask),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TabItem(MainTab.CALENDAR, current, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, R.string.dashboard_tab_calendar, onSelect)
            TabItem(MainTab.ME, current, Icons.Filled.Person, Icons.Outlined.Person, R.string.dashboard_tab_me, onSelect)
        }
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        // Fill is the theme primary (Surya-derived saffron), white-on-saffron icon.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(60.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clickable { onSelect(MainTab.ASK) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = stringResource(R.string.dashboard_tab_ask),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: MainTab,
    current: MainTab?,
    selectedIcon: ImageVector,
    icon: ImageVector,
    labelRes: Int,
    onSelect: (MainTab) -> Unit
) {
    val selected = current == tab
    NavigationBarItem(
        selected = selected,
        onClick = { onSelect(tab) },
        icon = { Icon(if (selected) selectedIcon else icon, contentDescription = null) },
        label = { Text(stringResource(labelRes), maxLines = 1) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * The one top-bar style every tab root uses: neutral background, title in the display
 * font (Fraunces), graha colour never on the bar itself — only on the items below.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabRootTopBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/** Small-caps style section label above a group of [HubRow]s. */
@Composable
fun HubSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}

data class HubItem(
    val icon: ImageVector,
    val graha: Graha,
    val title: String,
    val description: String?,
    val onClick: () -> Unit
)

/** A card holding a group of rows separated by hairlines — one consistent list style. */
@Composable
fun HubGroup(items: List<HubItem>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column {
            items.forEachIndexed { i, item ->
                HubRow(item)
                if (i < items.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun HubRow(item: HubItem) {
    val tone = item.graha.themedTone()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tone.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = tone, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (item.description != null) {
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
