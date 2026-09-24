package com.sangeetmind.features.astrology.gochar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.chart.ui.Chip
import com.sangeetmind.features.astrology.chart.ui.NorthIndianHouseChart
import com.sangeetmind.features.astrology.chart.ui.grahaColorFor
import com.sangeetmind.features.astrology.gochar.GocharUiState
import com.sangeetmind.features.astrology.gochar.GocharViewModel
import com.sangeetmind.libs.models.PlanetInfo
import com.sangeetmind.libs.models.TransitPlanet
import com.sangeetmind.libs.models.TransitResponse
import com.sangeetmind.libs.models.toTitleCase
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val GOCHAR_PLANETS = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")

/** Gochar: where the grahas are on a date, read against the natal chart — houses from the
 * lagna and from the natal Moon (classical gochar), each planet's drishti onto natal houses,
 * and upcoming sign changes / stations (app/services/transit_service.py). Its own screen,
 * like Varshaphal, because it's a different moment from the birth chart. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GocharScreen(
    onNavigateBack: () -> Unit,
    viewModel: GocharViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val graha = LocalGrahaColors.current
    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gochar_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(CoreR.string.common_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = graha.shani.copy(alpha = 0.14f),
                    titleContentColor = graha.shani,
                    navigationIconContentColor = graha.shani,
                    actionIconContentColor = graha.shani
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading && uiState.transit == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.hasNoKundli -> {
                    Text(
                        text = stringResource(R.string.gochar_no_kundli),
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = viewModel::refresh) { Text(stringResource(CoreR.string.common_retry)) }
                    }
                }
                else -> {
                    GocharContent(
                        uiState = uiState,
                        onDateChange = viewModel::selectDate,
                        onPickDate = { showPicker = true }
                    )
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.selectDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showPicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun GocharContent(
    uiState: GocharUiState,
    onDateChange: (LocalDate) -> Unit,
    onPickDate: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val transit = uiState.transit
    val dateLabel = uiState.date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
    // Resolved here, not in the LazyColumn builder (a non-composable LazyListScope lambda).
    val chartTitle = stringResource(R.string.gochar_chart_title, dateLabel)
    val chartSubtitle = stringResource(R.string.gochar_chart_subtitle)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = uiState.kundli?.fullName?.toTitleCase()?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.gochar_your_default),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            DateStepper(
                dateLabel = dateLabel,
                isToday = uiState.isToday,
                onPrevious = { onDateChange(uiState.date.minusMonths(1)) },
                onNext = { onDateChange(uiState.date.plusMonths(1)) },
                onToday = { onDateChange(LocalDate.now()) },
                onPickDate = onPickDate
            )
        }
        if (transit != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    NorthIndianHouseChart(
                        lagna = transit.natal.lagna,
                        planets = transit.transit.planets.mapValues { (_, p) -> p.toPlanetInfo() },
                        title = chartTitle,
                        subtitle = chartSubtitle,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item { TransitPlanetsCard(transit, uiState.date) }
            item { UpcomingCard(transit, uiState.date) }
        }
    }
}

@Composable
private fun DateStepper(
    dateLabel: String,
    isToday: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onPickDate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.gochar_cd_previous_month))
            }
            Text(
                dateLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClickLabel = stringResource(R.string.gochar_cd_pick_date), onClick = onPickDate)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.gochar_cd_next_month))
            }
        }
        if (isToday) {
            Text(
                stringResource(R.string.gochar_now),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TextButton(onClick = onToday) { Text(stringResource(R.string.gochar_today)) }
        }
    }
}

/** One row per graha in the compact chip-row style of the Birth Chart's house details:
 * name + sign on the left, the from-Moon verdict as a chip on the right, then muted-label
 * chip rows for houses, drishti and what changes next. */
@Composable
private fun TransitPlanetsCard(transit: TransitResponse, date: LocalDate) {
    val graha = LocalGrahaColors.current
    val planets = GOCHAR_PLANETS.mapNotNull { name -> transit.transit.planets[name]?.let { name to it } }
    val moonSign = transit.natal.moonSign

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.gochar_planets_title), style = MaterialTheme.typography.titleMedium)
            if (moonSign != null) {
                Text(
                    stringResource(R.string.gochar_planets_legend, astroTerm(moonSign)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            planets.forEachIndexed { index, (name, planet) ->
                if (index > 0) Divider()
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(astroTerm(name), style = MaterialTheme.typography.bodyMedium, color = grahaColorFor(name, graha))
                            Text(astroTerm(planet.sign), style = MaterialTheme.typography.bodyMedium)
                            // The mean nodes are always retrograde, so the tag only means something for the rest.
                            if (planet.retrograde && name != "Rahu" && name != "Ketu") {
                                Chip(astroTerm("Retrograde"), MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        when (planet.gocharEffect) {
                            "favourable" -> Chip(stringResource(R.string.gochar_favourable), graha.budha)
                            "challenging" -> Chip(stringResource(R.string.gochar_challenging), graha.mangala)
                        }
                    }
                    val houseChips = buildList {
                        planet.houseFromNatalLagna?.let { add(stringResource(R.string.gochar_from_lagna_fmt, it)) }
                        planet.houseFromNatalMoon?.let { add(stringResource(R.string.gochar_from_moon_fmt, it)) }
                    }
                    if (houseChips.isNotEmpty()) {
                        ChipRow(stringResource(R.string.gochar_label_house)) {
                            houseChips.forEach { Chip(it, MaterialTheme.colorScheme.primary) }
                        }
                    }
                    if (planet.aspectsNatalHouses.isNotEmpty()) {
                        ChipRow(stringResource(R.string.gochar_label_aspects)) {
                            planet.aspectsNatalHouses.forEach { aspect ->
                                val label = if (aspect.strength < 100) {
                                    stringResource(R.string.gochar_aspect_strength_fmt, aspect.house, aspect.strength)
                                } else {
                                    stringResource(R.string.gochar_aspect_full_fmt, aspect.house)
                                }
                                Chip(label, grahaColorFor(name, graha))
                            }
                        }
                    }
                    val nextChips = buildList {
                        planet.nextSignChange?.let {
                            add(stringResource(R.string.gochar_next_sign_fmt, astroTerm(it.sign), eventDate(it.date, date)))
                        }
                        planet.nextStation?.let {
                            val res = if (it.type == "retrograde") {
                                R.string.gochar_station_retrograde_fmt
                            } else {
                                R.string.gochar_station_direct_fmt
                            }
                            add(stringResource(res, eventDate(it.date, date)))
                        }
                    }
                    if (nextChips.isNotEmpty()) {
                        ChipRow(stringResource(R.string.gochar_label_next)) {
                            nextChips.forEach { Chip(it, MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
    }
}

private data class GocharEvent(val at: LocalDateTime, val planet: String, val sign: String?, val station: String?, val house: Int?)

/** Every planet's next sign change and station, in date order. The Moon is left out: it
 * changes sign every ~2.5 days and would crowd out the slower, more meaningful shifts. */
@Composable
private fun UpcomingCard(transit: TransitResponse, date: LocalDate) {
    val graha = LocalGrahaColors.current
    val lagnaIdx = ZODIAC.indexOf(transit.natal.lagna.sign)
    val events = GOCHAR_PLANETS.filter { it != "Moon" }.flatMap { name ->
        val p = transit.transit.planets[name] ?: return@flatMap emptyList()
        listOfNotNull(
            p.nextSignChange?.let { change ->
                parseLocal(change.date)?.let { at ->
                    val signIdx = ZODIAC.indexOf(change.sign)
                    val house = if (lagnaIdx >= 0 && signIdx >= 0) ((signIdx - lagnaIdx + 12) % 12) + 1 else null
                    GocharEvent(at, name, change.sign, null, house)
                }
            },
            p.nextStation?.let { station -> parseLocal(station.date)?.let { GocharEvent(it, name, null, station.type, null) } }
        )
    }.sortedBy { it.at }
    if (events.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.gochar_upcoming_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.gochar_upcoming_legend),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            events.forEachIndexed { index, event ->
                if (index > 0) Divider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        eventDate(event.at, date, withTime = false),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(92.dp)
                    )
                    val text = when {
                        event.sign != null -> stringResource(R.string.gochar_event_enters_fmt, astroTerm(event.planet), astroTerm(event.sign))
                        event.station == "retrograde" -> stringResource(R.string.gochar_event_retrograde_fmt, astroTerm(event.planet))
                        else -> stringResource(R.string.gochar_event_direct_fmt, astroTerm(event.planet))
                    }
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = grahaColorFor(event.planet, graha),
                        modifier = Modifier.weight(1f)
                    )
                    event.house?.let {
                        Chip(stringResource(R.string.gochar_event_house_fmt, it), MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(label: String, chips: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            chips()
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

private val ZODIAC = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

private fun parseLocal(iso: String): LocalDateTime? = runCatching { LocalDateTime.parse(iso) }.getOrNull()

@Composable
private fun eventDate(iso: String, from: LocalDate): String =
    parseLocal(iso)?.let { eventDate(it, from) } ?: iso

/** "17 Oct" for this year, "3 Jun 2027" for later years; within 3 days it adds the time
 * (unless [withTime] is off), which matters for the fast Moon. */
@Composable
private fun eventDate(at: LocalDateTime, from: LocalDate, withTime: Boolean = true): String {
    val locale: Locale = LocalConfiguration.current.locales[0]
    val pattern = when {
        withTime && ChronoUnit.DAYS.between(from, at.toLocalDate()) < 3 -> "d MMM, h:mm a"
        at.year != from.year -> "d MMM yyyy"
        else -> "d MMM"
    }
    return at.format(DateTimeFormatter.ofPattern(pattern, locale))
}

/** Transit positions dropped into the natal chart's houses for the diamond wheel. */
private fun TransitPlanet.toPlanetInfo() = PlanetInfo(
    sign = sign,
    degree = degree,
    absolute = absolute,
    house = houseFromNatalLagna,
    retrograde = retrograde,
    combust = combust,
    exalted = exalted,
    debilitated = debilitated
)
