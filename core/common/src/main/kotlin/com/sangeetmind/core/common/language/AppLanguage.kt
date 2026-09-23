package com.sangeetmind.core.common.language

import java.util.Locale

/**
 * Every language the app can be displayed in.
 *
 * Adding a language is: one entry here + a `values-<code>/` folder per module holding the
 * translated `strings*.xml`. Nothing else needs to know the list — the picker iterates
 * [entries], the backend gets [code] via `Accept-Language` and per-endpoint params, and
 * `stringResource` resolves through the locale-overridden context [MainActivity] installs.
 *
 * [code] is the BCP-47 language tag the backend understands (it uses "en"/"hi").
 */
enum class AppLanguage(val code: String, val nativeName: String, val englishName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "हिंदी", "Hindi");

    val locale: Locale get() = Locale(code)

    companion object {
        val DEFAULT = ENGLISH

        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
    }
}
