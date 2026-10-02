package com.sangeetmind.features.astrology.dashboard

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remembers how far the notification-permission nudge has gone on this device: whether the
 * system dialog was shown once (Android 13+ only lets us ask twice; we ask once from the
 * dashboard and then rely on the rationale card) and whether the user dismissed that card.
 */
@Singleton
class NotificationPromptStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var systemDialogShown: Boolean
        get() = prefs.getBoolean(KEY_ASKED, false)
        set(value) = prefs.edit().putBoolean(KEY_ASKED, value).apply()

    var rationaleDismissed: Boolean
        get() = prefs.getBoolean(KEY_DISMISSED, false)
        set(value) = prefs.edit().putBoolean(KEY_DISMISSED, value).apply()

    private companion object {
        const val PREFS = "notification_prompt"
        const val KEY_ASKED = "system_dialog_shown"
        const val KEY_DISMISSED = "rationale_dismissed"
    }
}
