package com.sangeetmind.libs.models

import java.time.LocalDate
import java.time.LocalTime

/** What [BirthDetailsParser] could pick out of pasted text; every field is optional. */
data class ParsedBirthDetails(
    val name: String? = null,
    val date: String? = null, // YYYY-MM-DD
    val time: String? = null, // HH:MM, 24h
    val place: String? = null
) {
    val isEmpty: Boolean get() = name == null && date == null && time == null && place == null
}

/**
 * Pulls name, date, time and place out of free text such as a WhatsApp message:
 * "Rahul, 12 Jan 1990, 4:30 pm, Patna", "Name: Rahul\nDOB: 12/01/1990\nTOB: 16:30\nPOB: Patna",
 * "राहुल, 12 जनवरी 1990, रात 11 बजे, पटना". Numeric dates are read as day/month/year
 * (swapped only when that is impossible). Whatever comes before the date is the name,
 * whatever comes after the date/time is the place.
 */
object BirthDetailsParser {

    fun parse(raw: String): ParsedBirthDetails {
        var text = raw.replace(' ', ' ').trim()
        if (text.isBlank()) return ParsedBirthDetails()

        // Labelled fields first; they beat position-based guesses.
        var name = NAME_LABEL.find(text)?.let { m -> text = text.removeRange(m.range); cleanSegment(m.groupValues[1]) }
        var place = PLACE_LABEL.find(text)?.let { m -> text = text.removeRange(m.range); cleanSegment(m.groupValues[1]) }

        // The date (or, failing that, the time) splits the text: name before it, place after.
        var cutMarked = false
        var date: String? = null
        for (pattern in DATE_PATTERNS) {
            val m = pattern.regex.find(text) ?: continue
            date = pattern.toIso(m) ?: continue
            text = text.replaceRange(m.range, CUT)
            cutMarked = true
            break
        }

        var time: String? = null
        for (m in TIME.findAll(text)) {
            val prefix = m.groupValues[1].lowercase()
            val minutes = m.groupValues[3]
            val marker = m.groupValues[4].lowercase()
            if (prefix.isEmpty() && minutes.isEmpty() && marker.isEmpty()) continue
            val hour = m.groupValues[2].toIntOrNull() ?: continue
            val h24 = to24h(hour, prefix.ifEmpty { marker }) ?: continue
            val minute = minutes.toIntOrNull() ?: 0
            time = runCatching { LocalTime.of(h24, minute) }.getOrNull()?.let { "%02d:%02d".format(h24, minute) }
                ?: continue
            text = text.replaceRange(m.range, if (cutMarked) SEPARATOR else CUT)
            cutMarked = true
            break
        }

        text = STRAY_LABEL.replace(text, SEPARATOR)
        val before = segments(text.substringBefore(CUT))
        val after = if (cutMarked) segments(text.substringAfter(CUT)) else emptyList()

        if (name == null && place == null) {
            when {
                after.isNotEmpty() -> {
                    place = after.joinToString(", ")
                    name = before.firstOrNull()
                }
                before.size >= 2 -> {
                    name = before.first()
                    place = before.drop(1).joinToString(", ")
                }
                else -> place = before.firstOrNull()
            }
        } else if (name == null) {
            name = before.firstOrNull() ?: after.firstOrNull()
        } else if (place == null) {
            place = after.ifEmpty { before }.takeIf { it.isNotEmpty() }?.joinToString(", ")
        }

        return ParsedBirthDetails(
            name = name?.takeIf { it.isNotBlank() },
            date = date,
            time = time,
            place = place?.takeIf { it.isNotBlank() }
        )
    }

    private fun to24h(hour: Int, marker: String): Int? {
        if (hour !in 0..23) return null
        return when (marker.replace(".", "")) {
            "am", "सुबह" -> if (hour == 12) 0 else hour
            "pm", "दोपहर", "शाम" -> if (hour < 12) hour + 12 else hour
            // "रात 2 बजे" is 2 am, "रात 11 बजे" is 11 pm.
            "रात" -> if (hour in 6..11) hour + 12 else hour
            else -> hour
        }
    }

    private fun segments(part: String): List<String> =
        SEGMENT_SPLIT.split(CONNECTOR.replace(part, ","))
            .map { cleanSegment(it) }
            .filter { it.isNotEmpty() && it.lowercase() !in JUNK && it.any { c -> c.isLetter() } }

    private fun cleanSegment(s: String): String =
        s.trim().trim(',', ';', '|', '-', '–', ':', '.', '(', ')').trim()

    private const val SEPARATOR = " , "
    private const val CUT = " \u0001 "

    // Word boundaries that also work around Devanagari (Java's \b is ASCII-only on some runtimes).
    private const val LB = "(?<![\\p{L}\\p{N}])"
    private const val RB = "(?![\\p{L}\\p{N}])"

    private val MONTHS: Map<String, Int> = buildMap {
        listOf(
            "january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november", "december"
        ).forEachIndexed { i, m -> put(m, i + 1); put(m.take(3), i + 1) }
        put("sept", 9)
        listOf(
            "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
            "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"
        ).forEachIndexed { i, m -> put(m, i + 1) }
        put("फ़रवरी", 2); put("सितम्बर", 9); put("अक्तूबर", 10); put("नवम्बर", 11); put("दिसम्बर", 12)
    }
    private val MONTH_ALT = MONTHS.keys.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }

    private class DatePattern(val regex: Regex, val toIso: (MatchResult) -> String?)

    private fun iso(y: Int, m: Int, d: Int): String? =
        runCatching { LocalDate.of(y, m, d) }.getOrNull()?.toString()

    private fun fullYear(y: Int): Int = when {
        y >= 100 -> y
        y <= LocalDate.now().year % 100 -> 2000 + y
        else -> 1900 + y
    }

    private val DATE_PATTERNS = listOf(
        DatePattern(Regex("$LB(\\d{4})-(\\d{1,2})-(\\d{1,2})$RB")) { m ->
            iso(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
        },
        DatePattern(Regex("$LB(\\d{1,2})(?:st|nd|rd|th)?\\s*(?:of\\s+)?($MONTH_ALT)\\.?,?\\s*(\\d{4})$RB", RegexOption.IGNORE_CASE)) { m ->
            iso(m.groupValues[3].toInt(), MONTHS.getValue(m.groupValues[2].lowercase()), m.groupValues[1].toInt())
        },
        DatePattern(Regex("$LB($MONTH_ALT)\\.?\\s+(\\d{1,2})(?:st|nd|rd|th)?,?\\s+(\\d{4})$RB", RegexOption.IGNORE_CASE)) { m ->
            iso(m.groupValues[3].toInt(), MONTHS.getValue(m.groupValues[1].lowercase()), m.groupValues[2].toInt())
        },
        DatePattern(Regex("$LB(\\d{1,2})[/.\\-](\\d{1,2})[/.\\-](\\d{4}|\\d{2})$RB")) { m ->
            val a = m.groupValues[1].toInt()
            val b = m.groupValues[2].toInt()
            val y = fullYear(m.groupValues[3].toInt())
            // Day/month/year as written in India; month/day only when day/month can't be right.
            if (a > 12 && b <= 12 || b <= 12 && a <= 12) iso(y, b, a) else iso(y, a, b)
        }
    )

    private val TIME = Regex(
        "(?:(सुबह|दोपहर|शाम|रात)\\s*)?(?<![\\d:.])(\\d{1,2})(?:[:.](\\d{2}))?(?:[:.]\\d{2})?\\s*" +
            "(am|pm|a\\.m\\.?|p\\.m\\.?|सुबह|दोपहर|शाम|रात|बजे|hrs|hours)?(?![\\d])",
        RegexOption.IGNORE_CASE
    )

    private const val NAME_WORDS = "(?:full\\s*name|name|naam|नाम)"
    private const val PLACE_WORDS = "(?:place\\s*of\\s*birth|birth\\s*place|birthplace|pob|place|city|town|" +
        "जन्म\\s*(?:का\\s*|की\\s*)?(?:स्थान|जगह|शहर)|जन्मस्थान|स्थान|जगह|शहर)"
    private const val OTHER_WORDS = "(?:date\\s*of\\s*birth|birth\\s*date|dob|time\\s*of\\s*birth|birth\\s*time|tob|" +
        "date|time|born|जन्म\\s*(?:का\\s*|की\\s*)?(?:तारीख़?|तिथि|समय)|जन्मतिथि|जन्म|तारीख़?|तिथि|समय)"
    private const val LABEL_SEP = "\\s*[:=\\-–]\\s*"
    private const val NEXT_LABEL = "(?=\\s*,?\\s*(?:$NAME_WORDS|$PLACE_WORDS|$OTHER_WORDS)$LABEL_SEP|\\s*$)"

    private val NAME_LABEL = Regex("$LB$NAME_WORDS$LABEL_SEP([^,\\n;|]+?)$NEXT_LABEL", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val PLACE_LABEL = Regex("$LB$PLACE_WORDS$LABEL_SEP([^\\n;|]+?)$NEXT_LABEL", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val STRAY_LABEL = Regex("$LB(?:$NAME_WORDS|$PLACE_WORDS|$OTHER_WORDS)$LABEL_SEP", RegexOption.IGNORE_CASE)

    // "born in Mumbai on" — each connector becomes a comma, so consecutive ones both split.
    private val CONNECTOR = Regex("(?<=\\s|^)(?:born|at|in|on|of|को|में|पर|and|और)(?=\\s|$)", RegexOption.IGNORE_CASE)
    private val SEGMENT_SPLIT = Regex("[,\\n;|]|\\s+[-–]\\s+")
    private val JUNK = setOf(
        "born", "at", "in", "on", "of", "ist", "hrs", "hours", "time", "date", "dob", "tob", "pob",
        "am", "pm", "बजे", "को", "में", "पर", "जन्म", "और", "and", "the"
    )
}
