package com.sangeetmind.core.common.language

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the display language.
 *
 * Backed by SharedPreferences rather than DataStore on purpose: `Activity.attachBaseContext`
 * runs before any coroutine can, and the very first frame must already be in the saved
 * language (no English flash), so the read has to be synchronous — see [readSync].
 */
@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = prefsOf(context)

    private val _language = MutableStateFlow(readSync(context))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    /** Snapshot for non-Compose callers (repositories building a request). */
    val current: AppLanguage get() = _language.value

    fun setLanguage(language: AppLanguage) {
        if (language == _language.value) return
        prefs.edit().putString(KEY, language.code).apply()
        Locale.setDefault(language.locale)
        _language.value = language
    }

    companion object {
        private const val PREFS = "sangeetmind_language"
        private const val KEY = "app_language"

        private fun prefsOf(context: Context): SharedPreferences =
            context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        /** Synchronous read for `attachBaseContext`, before Hilt has injected anything. */
        fun readSync(context: Context): AppLanguage =
            AppLanguage.fromCode(prefsOf(context).getString(KEY, null))
    }
}
