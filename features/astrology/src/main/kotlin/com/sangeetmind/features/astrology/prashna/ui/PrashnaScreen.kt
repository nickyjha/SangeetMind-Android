package com.sangeetmind.features.astrology.prashna.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.chart.ui.Chip
import com.sangeetmind.features.astrology.chart.ui.grahaColorFor
import com.sangeetmind.features.astrology.prashna.PRASHNA_QUESTIONS
import com.sangeetmind.features.astrology.prashna.PrashnaViewModel
import com.sangeetmind.libs.models.PrashnaResponse
import com.sangeetmind.libs.models.RulingPlanetsResponse

/** KP horary: today's ruling planets, then a question type and a number 1-249 give a
 * verdict from the cusp sub-lord of the question's house (backend kp_horary.py). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrashnaScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrashnaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val graha = LocalGrahaColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.prashna_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = graha.budha.copy(alpha = 0.14f),
                    titleContentColor = graha.budha,
                    navigationIconContentColor = graha.budha
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            uiState.ruling?.let { RulingCard(it) }

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.prashna_pick_question), style = MaterialTheme.typography.titleSmall)
            FlowRow(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRASHNA_QUESTIONS.forEach { q ->
                    FilterChip(
                        selected = uiState.question == q,
                        onClick = { viewModel.setQuestion(q) },
                        label = { Text(stringResource(questionLabel(q))) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.prashna_number_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = uiState.number,
                onValueChange = viewModel::setNumber,
                label = { Text(stringResource(R.string.prashna_number)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            )
            if (uiState.error != null && uiState.errorRetryable) {
                ErrorCard(
                    message = uiState.error,
                    onRetry = { viewModel.ask() },
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = viewModel::ask, enabled = !uiState.isAsking, modifier = Modifier.fillMaxWidth()) {
                if (uiState.isAsking) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.prashna_ask))
            }

            uiState.answer?.let {
                Spacer(modifier = Modifier.height(16.dp))
                AnswerCard(it)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.prashna_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RulingCard(rp: RulingPlanetsResponse) {
    val graha = LocalGrahaColors.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.prashna_ruling_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.prashna_ruling_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rp.rulingPlanets.forEach { Chip(astroTerm(it), grahaColorFor(it, graha)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnswerCard(r: PrashnaResponse) {
    val graha = LocalGrahaColors.current
    val languageCode = LocalAppLanguage.current.code
    val (label, color) = when (r.verdict) {
        "yes" -> stringResource(R.string.prashna_verdict_yes) to graha.budha
        "delayed" -> stringResource(R.string.prashna_verdict_delayed) to graha.surya
        else -> stringResource(R.string.prashna_verdict_no) to graha.mangala
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(r.questionText.forLanguage(languageCode), style = MaterialTheme.typography.labelLarge)
            Text(label, style = MaterialTheme.typography.headlineSmall, color = color)
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Chip(stringResource(R.string.prashna_number_chip_fmt, r.number), MaterialTheme.colorScheme.onSurfaceVariant)
                Chip(stringResource(R.string.prashna_lagna_fmt, astroTerm(r.ascendant.sign)), graha.shani)
                Chip(stringResource(R.string.prashna_csl_fmt, r.mainHouse, astroTerm(r.cuspSubLord)), grahaColorFor(r.cuspSubLord, graha))
            }
            Text(
                r.reason.forLanguage(languageCode),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (r.fruitfulPlanets.isNotEmpty() && r.verdict != "no") {
                Text(
                    stringResource(R.string.prashna_fruitful),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 10.dp)
                )
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    r.fruitfulPlanets.forEach { Chip(astroTerm(it), grahaColorFor(it, graha)) }
                }
            }
        }
    }
}

private fun questionLabel(q: String): Int = when (q) {
    "job" -> R.string.prashna_q_job
    "property" -> R.string.prashna_q_property
    "abroad" -> R.string.prashna_q_abroad
    "children" -> R.string.prashna_q_children
    "exam" -> R.string.prashna_q_exam
    else -> R.string.prashna_q_marriage
}
