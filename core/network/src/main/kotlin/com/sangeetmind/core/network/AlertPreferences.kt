package com.sangeetmind.core.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Which push alerts this device wants (backend app/services/daily_push.py KINDS). Until the
 * user touches a switch nothing is stored and [savedKinds] is null, so the server's own
 * defaults apply (and later default changes still reach the user).
 */
@Singleton
class AlertPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _kinds = MutableStateFlow(load())
    val kinds: StateFlow<Set<String>> = _kinds.asStateFlow()

    /** The explicit choice to send to the server, or null when never changed. */
    fun savedKinds(): List<String>? =
        if (prefs.contains(KEY)) ALL.filter { it in _kinds.value } else null

    fun setEnabled(kind: String, enabled: Boolean) {
        val next = if (enabled) _kinds.value + kind else _kinds.value - kind
        prefs.edit().putStringSet(KEY, next).apply()
        _kinds.value = next
    }

    private fun load(): Set<String> = prefs.getStringSet(KEY, null)?.toSet() ?: DEFAULTS

    companion object {
        private const val PREFS = "push_alerts"
        private const val KEY = "kinds"

        const val DAILY = "daily"
        const val DASHA = "dasha"
        const val FESTIVAL = "festival"
        const val CHANDRASHTAMA = "chandrashtama"
        const val TRANSIT = "transit"
        const val NAKSHATRA = "nakshatra"
        const val RAHU_KAAL = "rahu_kaal"

        val ALL = listOf(DAILY, DASHA, FESTIVAL, CHANDRASHTAMA, TRANSIT, NAKSHATRA, RAHU_KAAL)

        /** Mirrors the server's DEFAULT_KINDS: nakshatra and Rahu Kaal are opt-in. */
        val DEFAULTS = setOf(DAILY, DASHA, FESTIVAL, CHANDRASHTAMA, TRANSIT)
    }
}
