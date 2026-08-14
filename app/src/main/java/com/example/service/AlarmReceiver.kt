package com.example.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.TimeTrackDatabase
import com.example.data.local.model.NotificationLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val timetableId = intent.getLongExtra(NotificationHelper.EXTRA_TIMETABLE_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, timetableId.toInt())

        Log.d(TAG, "onReceive action=$action, timetableId=$timetableId")

        val db = TimeTrackDatabase.getDatabase(context)

        when (action) {
            NotificationHelper.ACTION_TIMETABLE_ALARM -> {
                if (timetableId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val item = db.timetableDao().getTimetableByIdDirect(timetableId)
                        if (item != null && item.notificationEnabled && item.status != "COMPLETED") {
                            NotificationHelper.showTimetableReminder(
                                context = context,
                                timetable = item,
                                isAtStart = (item.reminderMinutes == 0)
                            )
                            db.notificationLogDao().insertLog(
                                NotificationLogEntity(
                                    timetableId = item.id,
                                    title = item.title,
                                    message = "Reminder triggered for ${item.startTime}",
                                    actionTaken = "TRIGGERED"
                                )
                            )
                            // If repeating, schedule next occurrence
                            if (item.repeatType != "ONCE") {
                                AlarmScheduler.scheduleAlarm(context, item)
                            }
                        }
                    }
                }
            }

            NotificationHelper.ACTION_COMPLETE_TASK -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)

                if (timetableId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.timetableDao().updateStatus(timetableId, "COMPLETED")
                        val item = db.timetableDao().getTimetableByIdDirect(timetableId)
                        val title = item?.title ?: "Activity"
                        db.notificationLogDao().insertLog(
                            NotificationLogEntity(
                                timetableId = timetableId,
                                title = title,
                                message = "Marked as completed from notification",
                                actionTaken = "COMPLETED"
                            )
                        )
                    }
                }
            }

            NotificationHelper.ACTION_SNOOZE_TASK -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)

                if (timetableId != -1L) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L) // 10 minutes snooze

                    val snoozeAlarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                        this.action = NotificationHelper.ACTION_TIMETABLE_ALARM
                        putExtra(NotificationHelper.EXTRA_TIMETABLE_ID, timetableId)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        timetableId.toInt() + 50000,
                        snoozeAlarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                snoozeTime,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                snoozeTime,
                                pendingIntent
                            )
                        }
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Failed to schedule snooze: ${e.message}")
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        db.timetableDao().updateStatus(timetableId, "POSTPONED")
                        val item = db.timetableDao().getTimetableByIdDirect(timetableId)
                        val title = item?.title ?: "Activity"
                        db.notificationLogDao().insertLog(
                            NotificationLogEntity(
                                timetableId = timetableId,
                                title = title,
                                message = "Snoozed for 10 minutes",
                                actionTaken = "SNOOZED"
                            )
                        )
                    }
                }
            }

            NotificationHelper.ACTION_SKIP_TASK -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)

                if (timetableId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.timetableDao().updateStatus(timetableId, "SKIPPED")
                        val item = db.timetableDao().getTimetableByIdDirect(timetableId)
                        val title = item?.title ?: "Activity"
                        db.notificationLogDao().insertLog(
                            NotificationLogEntity(
                                timetableId = timetableId,
                                title = title,
                                message = "Skipped from notification",
                                actionTaken = "SKIPPED"
                            )
                        )
                    }
                }
            }
        }
    }
}
