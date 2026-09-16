package com.sangeetmind.core.network

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Registers rotated FCM tokens with the backend. Note this only fires on token
 * creation/rotation, not on every app start — [PushTokenRegistrar] is also called
 * right after a successful sign-in to cover the common case of a token that predates
 * that sign-in.
 */
@AndroidEntryPoint
class SangeetMindMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceApi: DeviceApi

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            runCatching {
                deviceApi.registerDevice(com.sangeetmind.libs.models.DeviceRegisterRequest(token = token))
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Displaying a system notification for foreground pushes is deferred —
        // Phase 2 only wires up token registration and delivery.
    }
}
