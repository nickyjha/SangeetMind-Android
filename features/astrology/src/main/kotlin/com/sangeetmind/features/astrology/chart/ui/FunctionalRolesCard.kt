package com.sangeetmind.features.astrology.chart.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.FunctionalRole
import com.sangeetmind.libs.models.FunctionalRoles

private val ROLE_GRAHAS = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")

/** Backend role keys shown as chips, in display order. Other keys are ignored. */
private val ROLE_CHIPS: List<Pair<String, Int>> = listOf(
    "yogakaraka" to R.string.chart_role_yogakaraka,
    "lagna_lord" to R.string.chart_role_lagna_lord,
    "trikona_lord" to R.string.chart_role_trikona_lord,
    "kendra_lord" to R.string.chart_role_kendra_lord,
    "dusthana_lord" to R.string.chart_role_dusthana_lord
)

@StringRes
private fun natureLabelRes(nature: String): Int = when (nature) {
    "benefic" -> R.string.chart_nature_benefic
    "malefic" -> R.string.chart_nature_malefic
    "mixed" -> R.string.chart_nature_mixed
    else -> R.string.chart_nature_neutral
}

@Composable
private fun natureColor(nature: String): Color {
    val graha = LocalGrahaColors.current
    return when (nature) {
        "benefic" -> graha.budha
        "malefic" -> graha.mangala
        "mixed" -> graha.surya
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * "Planet roles for your <lagna> lagna" (backend `functional_roles`): one compact row per
 * graha — name + houses ruled, then a nature chip and role chips. The caller only shows it
 * when the backend sent the block.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FunctionalRolesCard(roles: FunctionalRoles, lagnaSign: String) {
    val accent = LocalGrahaColors.current.shani
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.chart_roles_title, astroTerm(roles.lagna.ifBlank { lagnaSign })),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.chart_roles_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            val rows = ROLE_GRAHAS.mapNotNull { name -> roles.planets[name]?.let { name to it } }
            rows.forEachIndexed { index, (name, role) ->
                if (index > 0) Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                RoleRow(name, role, accent)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleRow(name: String, role: FunctionalRole, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(96.dp)) {
            Text(
                astroTerm(name),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                if (role.lordships.isEmpty()) stringResource(R.string.chart_roles_no_sign)
                else stringResource(R.string.chart_roles_rules_fmt, role.lordships.joinToString(", ")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Chip(stringResource(natureLabelRes(role.nature)), natureColor(role.nature))
            ROLE_CHIPS.filter { (key, _) -> key in role.roles }.forEach { (_, res) ->
                Chip(stringResource(res), accent)
            }
        }
    }
}
