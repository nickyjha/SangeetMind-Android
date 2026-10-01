package com.sangeetmind.app

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for SangeetMind
 * Initializes Hilt DI and Timber logging
 */
@HiltAndroidApp
class SangeetMindApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Debug: logcat. Release: warnings/errors go to Crashlytics as breadcrumbs, and any
        // Throwable logged with Timber.e(t, ...) is recorded as a non-fatal.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("SangeetMind Application initialized")
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}

private class CrashlyticsTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log((tag?.let { "[$it] " } ?: "") + message)
        if (t != null) crashlytics.recordException(t)
    }
}

