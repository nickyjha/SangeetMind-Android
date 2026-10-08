package com.sangeetmind.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangeetmind.core.ui.R
import com.sangeetmind.core.ui.language.LocalAppLanguage
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pure helpers behind [DateField]. The DatePicker works in UTC-midnight millis, so all
 * conversions go through [ZoneOffset.UTC] — using the device zone shifts the day by one
 * for users east/west of UTC.
 */
object DateFieldFormat {
    fun parseIso(iso: String?): LocalDate? =
        iso?.trim()?.takeIf { it.isNotEmpty() }?.let {
            runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        }

    fun toUtcMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromUtcMillis(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun toIso(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    /** "2 Oct 2026" (or the Hindi month name for a Hindi locale). Unparseable input is returned as-is. */
    fun display(iso: String, locale: Locale): String =
        parseIso(iso)?.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale)) ?: iso

    /**
     * A server timestamp ("2026-10-02T18:30:00+00:00", "2026-10-02 18:30:00", "2026-10-02")
     * shown as its calendar day in [zone] — "3 Oct 2026". Offset-less values keep their own
     * date; anything unparseable is returned as-is.
     */
    fun displayTimestamp(ts: String, locale: Locale, zone: ZoneId = ZoneId.systemDefault()): String {
        val trimmed = ts.trim()
        val date = runCatching { OffsetDateTime.parse(trimmed).atZoneSameInstant(zone).toLocalDate() }.getOrNull()
            ?: runCatching { Instant.parse(trimmed).atZone(zone).toLocalDate() }.getOrNull()
            ?: parseIso(trimmed.take(10))
        return date?.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale)) ?: ts
    }

    /** True when [date] lies within the optional inclusive bounds. */
    fun isAllowed(date: LocalDate, minDate: LocalDate?, maxDate: LocalDate?): Boolean =
        (minDate == null || !date.isBefore(minDate)) && (maxDate == null || !date.isAfter(maxDate))

    // A bare yyyy-MM-dd: not part of a longer number/ISO timestamp ("2027-04-20T10:00" stays).
    private val isoDateInText = Regex("(?<![\\d-])(\\d{4}-\\d{2}-\\d{2})(?![\\d-]|T\\d)")

    /**
     * Every bare ISO date inside free text ("from 2027-04-20 the ...") shown as "20 Apr 2027"
     * in [locale]; everything else in the text is left untouched, as is any date that does not
     * exist (2027-13-40).
     */
    fun displayDatesInText(text: String, locale: Locale): String =
        isoDateInText.replace(text) { m -> display(m.value, locale) }
}

/** Free text with each bare ISO `yyyy-MM-dd` inside it shown as "20 Apr 2027" in the app language. */
@Composable
fun isoDatesInText(text: String): String = DateFieldFormat.displayDatesInText(text, LocalAppLanguage.current.locale)

/** An ISO `yyyy-MM-dd` from the server as "2 Oct 2026" in the app language. */
@Composable
fun isoDateText(iso: String): String = DateFieldFormat.display(iso, LocalAppLanguage.current.locale)

/** A server timestamp as its local calendar day ("2 Oct 2026") in the app language. */
@Composable
fun timestampDateText(ts: String): String = DateFieldFormat.displayTimestamp(ts, LocalAppLanguage.current.locale)

/**
 * Read-only date field that opens a Material3 [DatePickerDialog]. The value in and out is
 * an ISO `yyyy-MM-dd` string (empty = nothing chosen), so ViewModels keep their string API;
 * the field itself shows a friendly "2 Oct 2026" in the app language.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    isoDate: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val locale = LocalAppLanguage.current.locale

    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (isoDate.isBlank()) "" else DateFieldFormat.display(isoDate, locale),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            supportingText = supportingText,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { open = true }
            )
        }
    }

    if (open) {
        val initial = DateFieldFormat.parseIso(isoDate)
            ?: LocalDate.now().let { today ->
                when {
                    minDate != null && today.isBefore(minDate) -> minDate
                    maxDate != null && today.isAfter(maxDate) -> maxDate
                    else -> today
                }
            }
        val minYear = minDate?.year ?: 1900
        val maxYear = maxDate?.year ?: 2100
        val state = rememberDatePickerState(
            initialSelectedDateMillis = DateFieldFormat.toUtcMillis(initial),
            yearRange = minYear..maxOf(minYear, maxYear)
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val picked = DateFieldFormat.fromUtcMillis(millis)
                            if (DateFieldFormat.isAllowed(picked, minDate, maxDate)) {
                                onDateChange(DateFieldFormat.toIso(picked))
                            }
                        }
                        open = false
                    },
                    enabled = state.selectedDateMillis != null
                ) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(
                state = state,
                dateValidator = { millis ->
                    DateFieldFormat.isAllowed(DateFieldFormat.fromUtcMillis(millis), minDate, maxDate)
                }
            )
        }
    }
}
