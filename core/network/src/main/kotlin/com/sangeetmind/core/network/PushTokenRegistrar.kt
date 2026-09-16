package com.sangeetmind.core.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.sangeetmind.libs.models.DeviceRegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Registers the current FCM token with the backend (POST /v1/users/me/devices). */
@Singleton
class PushTokenRegistrar @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val deviceApi: DeviceApi
) {
    suspend fun registerCurrentToken(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = Tasks.await(firebaseMessaging.token)
            val locale = Locale.getDefault().language.ifBlank { "en" }
            deviceApi.registerDevice(DeviceRegisterRequest(token = token, locale = locale))
            Unit
        }
    }
}
