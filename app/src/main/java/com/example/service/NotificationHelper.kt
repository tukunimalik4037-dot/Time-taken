package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.model.TimetableEntity

object NotificationHelper {
    const val CHANNEL_TIMETABLE = "timetable_reminders_channel"
    const val CHANNEL_SUMMARIES = "summaries_channel"
    const val CHANNEL_MONITOR = "monitor_service_channel"

    const val ACTION_TIMETABLE_ALARM = "com.example.ACTION_TIMETABLE_ALARM"
    const val ACTION_COMPLETE_TASK = "com.example.ACTION_COMPLETE_TASK"
    const val ACTION_SNOOZE_TASK = "com.example.ACTION_SNOOZE_TASK"
    const val ACTION_SKIP_TASK = "com.example.ACTION_SKIP_TASK"

    const val EXTRA_TIMETABLE_ID = "extra_timetable_id"
    const val EXTRA_TIMETABLE_TITLE = "extra_timetable_title"
    const val EXTRA_TIMETABLE_START = "extra_timetable_start"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_TIMETABLE,
                context.getString(R.string.channel_timetable_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_timetable_reminders_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            val summaryChannel = NotificationChannel(
                CHANNEL_SUMMARIES,
                context.getString(R.string.channel_summaries),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_summaries_desc)
            }

            val monitorChannel = NotificationChannel(
                CHANNEL_MONITOR,
                context.getString(R.string.channel_monitoring),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_monitoring_desc)
            }

            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(summaryChannel)
            manager.createNotificationChannel(monitorChannel)
        }
    }

    fun showTimetableReminder(context: Context, timetable: TimetableEntity, isAtStart: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = timetable.id.toInt()

        // Content intent opens main activity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "timetable")
            putExtra("timetable_id", timetable.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Complete
        val completeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TIMETABLE_ID, timetable.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze (10 min)
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_TASK
            putExtra(EXTRA_TIMETABLE_ID, timetable.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SKIP_TASK
            putExtra(EXTRA_TIMETABLE_ID, timetable.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 3,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isAtStart) {
            "⏰ Starting Now: ${timetable.title}"
        } else {
            "🔔 Upcoming: ${timetable.title} in ${timetable.reminderMinutes}m"
        }

        val message = if (timetable.description.isNotBlank()) {
            "${timetable.startTime} - ${timetable.endTime} • ${timetable.category}\n${timetable.description}"
        } else {
            "${timetable.startTime} - ${timetable.endTime} • ${timetable.category}"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_TIMETABLE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Done", completePendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 10m", snoozePendingIntent)
            .addAction(android.R.drawable.ic_delete, "Skip", skipPendingIntent)

        if (timetable.vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 300, 200, 300))
        }

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Notifications permission not granted
        }
    }

    fun showSummaryNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "insights")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SUMMARIES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(9999, builder.build())
        } catch (e: SecurityException) {
            // Notifications permission not granted
        }
    }
}
