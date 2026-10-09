package com.sangeetmind.core.common.profile

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether the person is married: a fact the chart cannot know. Same shape as ChartStyleManager
 * (StateFlow over SharedPreferences) so the Full Reading, the Marriage reading and the phal
 * engine (`?married=true`, which drops the Mangal-dosha matching penalty) all follow one setting.
 */
@Singleton
class MaritalStatusManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _married = MutableStateFlow(prefs.getBoolean(KEY, false))
    val married: StateFlow<Boolean> = _married.asStateFlow()

    fun setMarried(married: Boolean) {
        if (married == _married.value) return
        prefs.edit().putBoolean(KEY, married).apply()
        _married.value = married
    }

    private companion object {
        const val PREFS = "sangeetmind_profile"
        const val KEY = "married"
    }
}
