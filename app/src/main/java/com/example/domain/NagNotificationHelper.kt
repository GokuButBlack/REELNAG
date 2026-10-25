package com.example.domain

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
import com.example.MainActivity
import com.example.R
import com.example.data.UserSettings
import java.util.Calendar

sealed class NotificationDeliveryStatus {
    data class Delivered(val message: String) : NotificationDeliveryStatus()
    data class SuppressedQuietHours(val reason: String) : NotificationDeliveryStatus()
    data class PermissionMissing(val reason: String) : NotificationDeliveryStatus()
    data class DisabledInSettings(val reason: String) : NotificationDeliveryStatus()
}

object NagNotificationHelper {
    private const val CHANNEL_ID = "reelnag_smart_nags"
    private const val CHANNEL_NAME = "Smart Nag Reminders"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Witty personality-driven reminders for your saved Instagram Reels"
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Attempts to post a real system notification only when permissions and quiet hours allow.
     * Never claims a notification was delivered unless NotificationManagerCompat actually dispatches it.
     */
    fun trySendNagNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        settings: UserSettings
    ): NotificationDeliveryStatus {
        if (!settings.notificationsEnabled) {
            return NotificationDeliveryStatus.DisabledInSettings(
                "In-app reminder saved! System notifications are turned off in your ReelNag Profile."
            )
        }

        val now = Calendar.getInstance()
        val inQuietHours = SmartNagMessageEngine.isWithinQuietHours(
            quietHoursEnabled = settings.quietHoursEnabled,
            startStr = settings.quietHoursStart,
            endStr = settings.quietHoursEnd,
            currentHour = now.get(Calendar.HOUR_OF_DAY),
            currentMinute = now.get(Calendar.MINUTE)
        )
        if (inQuietHours) {
            return NotificationDeliveryStatus.SuppressedQuietHours(
                "Quiet Hours (${settings.quietHoursStart}–${settings.quietHoursEnd}) are active: kept as an in-app banner without buzzing your phone."
            )
        }

        if (!hasNotificationPermission(context)) {
            return NotificationDeliveryStatus.PermissionMissing(
                "Saved to your in-app 'Due Now' list! Grant Android Notification permission in Profile if you also want system tray alerts."
            )
        }

        return try {
            ensureChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("${settings.personality.badgeEmoji} ${settings.personality.displayName}: $title")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            NotificationDeliveryStatus.Delivered("System notification delivered to your Android status bar!")
        } catch (e: SecurityException) {
            NotificationDeliveryStatus.PermissionMissing(
                "System notification permission was not granted; reminder is active in your in-app Due Now banner."
            )
        }
    }
}
