package com.sangeetmind.core.network

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives FCM pushes and keeps the backend's copy of the token fresh.
 *
 * Channels: [CHANNEL_DAILY] ("Your day" every morning) and [CHANNEL_ALERTS] (festival
 * reminders, dasha changes). The backend names the channel in the message's
 * `android.notification.channel_id` (background delivery, shown by the system) and in the
 * data key `channel` (foreground delivery, shown here).
 *
 * Deep links — MainActivity reads these intent extras on launch and on `onNewIntent`.
 * Background notifications tapped from the tray carry every FCM data key as a String extra
 * on the launcher intent; foreground ones are built the same way in [buildContentIntent]:
 *  - [EXTRA_SCREEN]      "dashboard" | "readings" | "festivals" | "eclipses"
 *  - [EXTRA_TYPE]        "daily" | "festival" | "dasha" (what triggered the push)
 *  - [EXTRA_DATE]        ISO date the push is about (festival day / dasha start)
 *  - [EXTRA_FESTIVAL_ID] festival id (festival pushes only)
 *
 * Token rotation only fires [onNewToken]; [PushTokenRegistrar] is also called after sign-in
 * and from the dashboard once notifications are allowed.
 */
@AndroidEntryPoint
class SangeetMindMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushTokenRegistrar: PushTokenRegistrar

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ensureChannels(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            runCatching { pushTokenRegistrar.registerToken(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: return
        val body = message.notification?.body ?: data["body"] ?: ""
        val channel = (data["channel"] ?: message.notification?.channelId)
            .takeIf { it == CHANNEL_ALERTS } ?: CHANNEL_DAILY
        showNotification(this, channel, title, body, data)
    }

    companion object {
        const val CHANNEL_DAILY = "daily"
        const val CHANNEL_ALERTS = "alerts"

        const val EXTRA_SCREEN = "screen"
        const val EXTRA_TYPE = "type"
        const val EXTRA_DATE = "date"
        const val EXTRA_FESTIVAL_ID = "festival_id"

        /** All data keys copied onto the content intent (order irrelevant). */
        private val DEEP_LINK_KEYS = listOf(EXTRA_SCREEN, EXTRA_TYPE, EXTRA_DATE, EXTRA_FESTIVAL_ID)

        /** Idempotent; safe to call from any thread, on every API level. */
        fun ensureChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val daily = NotificationChannel(
                CHANNEL_DAILY,
                context.getString(R.string.push_channel_daily_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.push_channel_daily_desc) }
            val alerts = NotificationChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.push_channel_alerts_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.push_channel_alerts_desc) }
            manager.createNotificationChannels(listOf(daily, alerts))
        }

        fun notificationsAllowed(context: Context): Boolean {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        }

        /** Launcher intent for this app with the deep-link extras from the push's data. */
        fun buildContentIntent(context: Context, data: Map<String, String>): Intent? {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            DEEP_LINK_KEYS.forEach { key -> data[key]?.let { intent.putExtra(key, it) } }
            if (!intent.hasExtra(EXTRA_SCREEN)) intent.putExtra(EXTRA_SCREEN, "dashboard")
            return intent
        }

        fun showNotification(
            context: Context,
            channel: String,
            title: String,
            body: String,
            data: Map<String, String>
        ) {
            if (!notificationsAllowed(context)) return
            ensureChannels(context)
            val type = data[EXTRA_TYPE] ?: channel
            val contentIntent = buildContentIntent(context, data)?.let {
                PendingIntent.getActivity(
                    context,
                    type.hashCode(),
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            val notification = NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_push_small)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .build()
            runCatching {
                // One slot per kind: a second "daily" push the same day replaces the first.
                NotificationManagerCompat.from(context).notify(type.hashCode(), notification)
            }
        }
    }
}
