package com.sangeetmind.features.astrology.accuracy

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.R

/** Entry card for the Full Reading screen. */
@Composable
fun AccuracySurveyCard(onOpen: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.survey_card_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.survey_card_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = onOpen) { Text(stringResource(R.string.survey_card_button)) }
        }
    }
}

/** Full-screen opt-in survey: consent first, every question optional, withdraw anytime. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccuracySurveyDialog(onDismiss: () -> Unit, viewModel: AccuracySurveyViewModel = hiltViewModel()) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.survey_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.survey_close))
                    }
                }
                if (s.loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    return@Column
                }
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(stringResource(R.string.survey_intro), style = MaterialTheme.typography.bodyMedium)
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.survey_privacy_title), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.survey_privacy_body), style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = s.consent, onCheckedChange = viewModel::setConsent)
                                Text(stringResource(R.string.survey_consent), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    val a = s.answers
                    Question(R.string.survey_q_birth_time, a.birthTimeSource, listOf(
                        "certificate" to R.string.survey_a_certificate,
                        "family" to R.string.survey_a_family,
                        "estimate" to R.string.survey_a_estimate
                    )) { v -> viewModel.update { it.copy(birthTimeSource = v) } }
                    Question(R.string.survey_q_marital, a.maritalStatus, listOf(
                        "never_married" to R.string.survey_a_never_married,
                        "married" to R.string.survey_a_married,
                        "remarried" to R.string.survey_a_remarried,
                        "divorced" to R.string.survey_a_divorced,
                        "separated" to R.string.survey_a_separated,
                        "widowed" to R.string.survey_a_widowed
                    )) { v -> viewModel.update { it.copy(maritalStatus = v) } }
                    Question(R.string.survey_q_children, a.children?.let { if (it >= 3) "3" else it.toString() }, listOf(
                        "0" to R.string.survey_a_0, "1" to R.string.survey_a_1,
                        "2" to R.string.survey_a_2, "3" to R.string.survey_a_3plus
                    )) { v -> viewModel.update { it.copy(children = v?.toInt()) } }
                    Question(R.string.survey_q_career, a.career, listOf(
                        "struggling" to R.string.survey_a_struggling,
                        "steady" to R.string.survey_a_steady,
                        "successful" to R.string.survey_a_successful,
                        "highly_successful" to R.string.survey_a_highly_successful
                    )) { v -> viewModel.update { it.copy(career = v) } }
                    Question(R.string.survey_q_wealth, a.wealth, listOf(
                        "below_average" to R.string.survey_a_below_average,
                        "average" to R.string.survey_a_average,
                        "above_average" to R.string.survey_a_above_average,
                        "wealthy" to R.string.survey_a_wealthy
                    )) { v -> viewModel.update { it.copy(wealth = v) } }
                    Question(R.string.survey_q_education, a.education, listOf(
                        "school" to R.string.survey_a_school,
                        "graduate" to R.string.survey_a_graduate,
                        "postgraduate" to R.string.survey_a_postgraduate,
                        "doctorate" to R.string.survey_a_doctorate
                    )) { v -> viewModel.update { it.copy(education = v) } }
                    Question(R.string.survey_q_property, a.property, listOf(
                        "none" to R.string.survey_a_none_property,
                        "one" to R.string.survey_a_one_property,
                        "several" to R.string.survey_a_several_property
                    )) { v -> viewModel.update { it.copy(property = v) } }
                    Question(R.string.survey_q_abroad, a.settledAbroad?.toString(), listOf(
                        "true" to R.string.survey_a_yes, "false" to R.string.survey_a_no
                    )) { v -> viewModel.update { it.copy(settledAbroad = v?.toBoolean()) } }
                    Question(R.string.survey_q_illness, a.seriousIllness, listOf(
                        "none" to R.string.survey_a_none_illness,
                        "minor" to R.string.survey_a_minor,
                        "serious" to R.string.survey_a_serious
                    )) { v -> viewModel.update { it.copy(seriousIllness = v) } }

                    s.message?.let { m ->
                        val id = when (m) {
                            "saved" -> R.string.survey_msg_saved
                            "withdrawn" -> R.string.survey_msg_withdrawn
                            "no_kundli" -> R.string.survey_msg_no_kundli
                            else -> R.string.survey_msg_error
                        }
                        Text(stringResource(id), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = viewModel::save,
                        enabled = s.consent && !s.saving,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(if (s.submitted) R.string.survey_update else R.string.survey_submit)) }
                    if (s.submitted) {
                        TextButton(onClick = viewModel::withdraw, enabled = !s.saving, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.survey_withdraw))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/** One optional question: chips toggle; tapping the selected chip clears it ("prefer not to say"). */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Question(
    @StringRes title: Int,
    selected: String?,
    options: List<Pair<String, Int>>,
    onSelect: (String?) -> Unit
) {
    Column {
        Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(if (selected == value) null else value) },
                    label = { Text(stringResource(label)) }
                )
            }
        }
    }
}
