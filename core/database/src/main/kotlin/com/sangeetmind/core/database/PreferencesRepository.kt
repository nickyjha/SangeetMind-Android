package com.sangeetmind.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sangeetmind.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sangeetmind_prefs"
)

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val onboardingCompletedKey =
        booleanPreferencesKey(Constants.PREF_ONBOARDING_COMPLETED)

    suspend fun isOnboardingCompleted(): Boolean =
        context.appPreferencesDataStore.data
            .map { prefs -> prefs[onboardingCompletedKey] == true }
            .first()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.appPreferencesDataStore.edit { prefs ->
            prefs[onboardingCompletedKey] = completed
        }
    }
}
