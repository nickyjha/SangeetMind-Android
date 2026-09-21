package com.sangeetmind.features.astrology.kundli.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.kundli.KundliOnboardingViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliOnboardingScreen(
    onSaved: () -> Unit,
    viewModel: KundliOnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Tell us about your birth details",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "This creates your kundli (birth chart) — the basis for every reading in the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = viewModel::onFullNameChange,
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.birthDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of birth") },
                trailingIcon = { Icon(Icons.Default.DateRange, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.birthTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time of birth (24h)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showTimePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.birthPlaceQuery,
            onValueChange = viewModel::onBirthPlaceQueryChange,
            label = { Text("Place of birth") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            trailingIcon = {
                if (uiState.isSearchingPlace) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
        )

        if (uiState.placeSuggestions.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(uiState.placeSuggestions) { suggestion ->
                        ListItem(
                            headlineContent = { Text(suggestion.label) },
                            modifier = Modifier.clickable { viewModel.onPlaceSelected(suggestion) }
                        )
                        Divider()
                    }
                }
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::save,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Generate my kundli")
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = runCatching {
            java.time.LocalDate.parse(uiState.birthDate, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.onBirthDateChange(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val (initialHour, initialMinute) = remember(uiState.birthTime) {
            val parts = uiState.birthTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 6
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            hour to minute
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Time of birth", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            viewModel.onBirthTimeChange(
                                "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                            )
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}
