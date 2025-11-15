package com.sangeetmind.app

import android.app.Application
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
        
        // Initialize Timber for logging (debug builds only)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("SangeetMind Application initialized")
        }
    }
}

