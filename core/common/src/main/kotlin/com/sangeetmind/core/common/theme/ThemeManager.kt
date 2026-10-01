package com.sangeetmind.core.common.theme

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Light is the default: the dark palette is hard to read for most of our users, so the app
 * no longer follows the phone's dark mode unless the user picks "System" in Settings. */
enum class ThemePreference(val code: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        val DEFAULT = LIGHT
        fun fromCode(code: String?): ThemePreference = entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}

/** Single source of truth for the theme, same shape as LanguageManager / ChartStyleManager. */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _preference = MutableStateFlow(ThemePreference.fromCode(prefs.getString(KEY, null)))
    val preference: StateFlow<ThemePreference> = _preference.asStateFlow()

    fun setPreference(preference: ThemePreference) {
        if (preference == _preference.value) return
        prefs.edit().putString(KEY, preference.code).apply()
        _preference.value = preference
    }

    private companion object {
        const val PREFS = "sangeetmind_theme"
        const val KEY = "theme"
    }
}
