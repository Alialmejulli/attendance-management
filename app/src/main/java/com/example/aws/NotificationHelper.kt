package com.example.aws

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    // ── Channel IDs ────────────────────────────────────────────────
    private const val CHANNEL_SESSION = "ams_session_channel"
    private const val CHANNEL_CONFIRM = "ams_confirm_channel"

    // ── Notification IDs ───────────────────────────────────────────
    const val NOTIF_ACTIVE_SESSION      = 2001
    const val NOTIF_ATTENDANCE_CONFIRMED = 2002

    // ── Create channels ────────────────────────────────────────────
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // HIGH importance = drops from top as heads-up notification
            val sessionChannel = NotificationChannel(
                CHANNEL_SESSION,
                "Attendance Sessions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when an attendance session is active for your course"
                enableVibration(true)
                enableLights(true)
            }

            // LOW importance = silent, appears in shade only
            val confirmChannel = NotificationChannel(
                CHANNEL_CONFIRM,
                "Attendance Confirmations",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Confirms when your attendance has been recorded"
            }

            manager.createNotificationChannel(sessionChannel)
            manager.createNotificationChannel(confirmChannel)
        }
    }

    // ── Notification 1: Active session — drops from top ────────────
    fun showActiveSessionNotification(
        context: Context,
        courseName: String,
        courseCode: String,
        code: String,
        sectionId: String
    ) {
        // Tapping opens mark attendance directly
        val intent = Intent(context, StudentAttendanceActivity::class.java).apply {
            putExtra("section_id",  sectionId)
            putExtra("course_code", courseCode)
            putExtra("course_name", courseName)
            putExtra("auto_code", code)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIF_ACTIVE_SESSION, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📋 Attendance Session Active")
            .setContentText("$courseName ($courseCode) — Code: $code")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Your instructor has started an attendance session for $courseName.\n\n" +
                                "Session code: $code\n\n" +
                                "Tap to mark your attendance now."
                    )
            )
            // These three together guarantee heads-up drop from top
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ACTIVE_SESSION, notification)
    }

    // ── Notification 2: Attendance confirmed — silent receipt ───────
    fun showAttendanceConfirmedNotification(
        context: Context,
        courseName: String,
        courseCode: String,
        status: String
    ) {
        val statusText = when (status) {
            "P"  -> "Present ✓"
            "L"  -> "Late ✓"
            else -> "Recorded ✓"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_CONFIRM)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Attendance Recorded — $statusText")
            .setContentText("$courseName ($courseCode)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Your attendance for $courseName has been recorded as $statusText.\n\n" +
                                "Keep this as your receipt."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ATTENDANCE_CONFIRMED, notification)
    }

    // ── Cancel a specific notification ─────────────────────────────
    fun cancel(context: Context, notifId: Int) {
        context.getSystemService(NotificationManager::class.java).cancel(notifId)
    }
}