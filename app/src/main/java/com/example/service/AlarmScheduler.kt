package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.model.TimetableEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, timetable: TimetableEntity) {
        if (!timetable.notificationEnabled) {
            cancelAlarm(context, timetable.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = calculateTriggerTime(timetable)

        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Trigger time is in the past for timetable ${timetable.id}")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationHelper.ACTION_TIMETABLE_ALARM
            putExtra(NotificationHelper.EXTRA_TIMETABLE_ID, timetable.id)
            putExtra(NotificationHelper.EXTRA_TIMETABLE_TITLE, timetable.title)
            putExtra(NotificationHelper.EXTRA_TIMETABLE_START, timetable.startTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timetable.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        10 * 60 * 1000L,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for ${timetable.title} at $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context, timetableId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationHelper.ACTION_TIMETABLE_ALARM
            putExtra(NotificationHelper.EXTRA_TIMETABLE_ID, timetableId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timetableId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun calculateTriggerTime(timetable: TimetableEntity): Long {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        // Parse start time (HH:mm)
        val timeParts = timetable.startTime.split(":")
        val startHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
        val startMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        // Parse date (YYYY-MM-DD) if ONCE
        if (timetable.repeatType == "ONCE") {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val date = sdf.parse(timetable.date)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                // Keep today
            }
        }

        calendar.set(Calendar.HOUR_OF_DAY, startHour)
        calendar.set(Calendar.MINUTE, startMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Subtract reminder minutes (e.g. 10m before)
        calendar.add(Calendar.MINUTE, -timetable.reminderMinutes)

        // If repeat type is not ONCE, adjust for future day if today's time has passed
        when (timetable.repeatType) {
            "DAILY" -> {
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "WEEKDAYS" -> {
                while (calendar.timeInMillis <= now ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                ) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "WEEKENDS" -> {
                while (calendar.timeInMillis <= now ||
                    (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                     calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
                ) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "CUSTOM" -> {
                val allowedDays = timetable.customDays.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (allowedDays.isNotEmpty()) {
                    var attempts = 0
                    while (attempts < 14) {
                        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.MONDAY -> 1
                            Calendar.TUESDAY -> 2
                            Calendar.WEDNESDAY -> 3
                            Calendar.THURSDAY -> 4
                            Calendar.FRIDAY -> 5
                            Calendar.SATURDAY -> 6
                            Calendar.SUNDAY -> 7
                            else -> 1
                        }
                        if (calendar.timeInMillis > now && allowedDays.contains(dayOfWeek)) {
                            break
                        }
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        attempts++
                    }
                } else if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        return calendar.timeInMillis
    }
}
