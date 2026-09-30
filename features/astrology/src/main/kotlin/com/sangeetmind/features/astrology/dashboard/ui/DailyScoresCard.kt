package com.sangeetmind.features.astrology.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.DailyAction
import com.sangeetmind.libs.models.DailyScoresResponse
import java.time.LocalDate
import java.time.format.TextStyle

private val AREA_ORDER = listOf("self", "wealth", "love", "career")

/** Self / Wealth / Love / Career for yesterday, today and tomorrow, each with a one-line
 * note and an Ask button into ChatMind; lucky number and time; a 7-day overall strip
 * (backend daily_scores_service.py). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DailyScoresCard(scores: DailyScoresResponse, onAsk: (String) -> Unit) {
    if (scores.days.isEmpty()) return
    val graha = LocalGrahaColors.current
    val languageCode = LocalAppLanguage.current.code
    var selected by remember { mutableIntStateOf(scores.days.indexOfFirst { it.label == "today" }.coerceAtLeast(0)) }
    val day = scores.days[selected.coerceIn(0, scores.days.lastIndex)]

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.dashboard_scores_title), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scores.days.forEachIndexed { i, d ->
                    FilterChip(
                        selected = i == selected,
                        onClick = { selected = i },
                        label = { Text(stringResource(dayLabel(d.label))) }
                    )
                }
            }
            AREA_ORDER.forEach { key ->
                val area = day.areas[key] ?: return@forEach
                val color = when (area.level) {
                    "good" -> graha.budha
                    "low" -> graha.mangala
                    else -> graha.surya
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(areaLabel(key)),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(72.dp)
                    )
                    LinearProgressIndicator(
                        progress = area.score / 100f,
                        color = color,
                        trackColor = color.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        "${area.score}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = color,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(48.dp)
                    )
                }
                Text(
                    area.text.forLanguage(languageCode),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        area.reason.forLanguage(languageCode),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onAsk(area.question.forLanguage(languageCode)) }) {
                        Text(stringResource(R.string.dashboard_scores_ask))
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                day.luckyNumber?.let {
                    Column {
                        Text(stringResource(R.string.dashboard_lucky_number), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$it", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                day.luckyTime?.let { t ->
                    Column {
                        Text(stringResource(R.string.dashboard_lucky_time), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${astroTerm(t.name)} ${t.start.orEmpty()}–${t.end.orEmpty()}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (scores.strip.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(stringResource(R.string.dashboard_scores_week), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                WeekStrip(scores)
            }
        }
    }
}

@Composable
private fun WeekStrip(scores: DailyScoresResponse) {
    val graha = LocalGrahaColors.current
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        scores.strip.forEach { s ->
            val color = when {
                s.overall >= 70 -> graha.budha
                s.overall < 50 -> graha.mangala
                else -> graha.surya
            }
            val weekday = runCatching {
                LocalDate.parse(s.date).dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
            }.getOrDefault("")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${s.overall}", style = MaterialTheme.typography.labelMedium, color = color)
                }
                Text(weekday, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun dayLabel(label: String): Int = when (label) {
    "yesterday" -> R.string.dashboard_scores_yesterday
    "tomorrow" -> R.string.dashboard_scores_tomorrow
    else -> R.string.dashboard_scores_today
}

private fun areaLabel(key: String): Int = when (key) {
    "wealth" -> R.string.dashboard_area_wealth
    "love" -> R.string.dashboard_area_love
    "career" -> R.string.dashboard_area_career
    else -> R.string.dashboard_area_self
}


/** Action of the day: one small step for today's weakest area, with a commit button and
 * the on-device streak (ActionStreakStore). */
@Composable
internal fun ActionCard(action: DailyAction, done: Boolean, streak: Int, onDone: () -> Unit) {
    val graha = LocalGrahaColors.current
    val languageCode = LocalAppLanguage.current.code
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dashboard_action_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(stringResource(areaLabel(action.area)), style = MaterialTheme.typography.labelMedium, color = graha.guru)
            }
            Text(
                action.text.forLanguage(languageCode),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (done) {
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dashboard_action_done))
                }
            } else {
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dashboard_action_commit))
                }
            }
            if (streak > 0) {
                Text(
                    stringResource(R.string.dashboard_action_streak_fmt, streak),
                    style = MaterialTheme.typography.labelLarge,
                    color = graha.budha,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
