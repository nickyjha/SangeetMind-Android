package com.sangeetmind.features.astrology.sangeet

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Remembers the mantra last chosen in the japa counter on this device. */
@Singleton
class JapaMantraStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Null until the user has picked one (so the dasha recommendation can pre-select). */
    var lastMantraId: String?
        get() = prefs.getString(KEY_LAST, null)
        set(value) = prefs.edit().putString(KEY_LAST, value).apply()

    private companion object {
        const val PREFS = "japa_mantra"
        const val KEY_LAST = "last_mantra_id"
    }
}
