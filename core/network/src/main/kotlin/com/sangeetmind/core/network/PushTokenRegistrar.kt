package com.sangeetmind.core.network

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.libs.models.DeviceRegisterRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers the current FCM token with the backend (POST /v1/users/me/devices) together
 * with what the daily push needs: the app language, the device's IANA timezone and the
 * preferred local hour, so the server can send "Your day" at the user's local morning.
 */
@Singleton
class PushTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging,
    private val deviceApi: DeviceApi,
    private val languageManager: LanguageManager,
    private val alertPreferences: AlertPreferences
) {
    suspend fun registerCurrentToken(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = Tasks.await(firebaseMessaging.token)
            registerToken(token)
        }
    }

    /** Used by [SangeetMindMessagingService.onNewToken], which already has the token. */
    suspend fun registerToken(token: String) {
        SangeetMindMessagingService.ensureChannels(context)
        deviceApi.registerDevice(buildRequest(token))
    }

    fun buildRequest(token: String): DeviceRegisterRequest = DeviceRegisterRequest(
        token = token,
        platform = "android",
        locale = languageManager.current.code,
        timezone = TimeZone.getDefault().id,
        pushHourLocal = DEFAULT_PUSH_HOUR,
        appVersion = appVersion(),
        alertKinds = alertPreferences.savedKinds()
    )

    private fun appVersion(): String? = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull()

    companion object {
        /** Local hour for "Your day". A per-user picker can override this later. */
        const val DEFAULT_PUSH_HOUR = 7
    }
}
