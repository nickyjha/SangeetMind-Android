package com.sangeetmind.core.common.chart

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** How house charts are drawn: the North Indian diamond or the South Indian fixed-sign grid. */
enum class ChartStyle(val code: String) {
    NORTH("north"),
    SOUTH("south");

    companion object {
        val DEFAULT = NORTH
        fun fromCode(code: String?): ChartStyle = entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}

/**
 * Single source of truth for the chart style, same shape as LanguageManager: a StateFlow
 * backed by SharedPreferences so every chart on every screen follows one setting.
 */
@Singleton
class ChartStyleManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _style = MutableStateFlow(ChartStyle.fromCode(prefs.getString(KEY, null)))
    val style: StateFlow<ChartStyle> = _style.asStateFlow()

    fun setStyle(style: ChartStyle) {
        if (style == _style.value) return
        prefs.edit().putString(KEY, style.code).apply()
        _style.value = style
    }

    private companion object {
        const val PREFS = "sangeetmind_chart_style"
        const val KEY = "chart_style"
    }
}
