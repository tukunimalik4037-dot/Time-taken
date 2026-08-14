package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.TimeTrackDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "Received system broadcast: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val db = TimeTrackDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeTimetables = db.timetableDao().getAllActiveReminders()
                    Log.d(TAG, "Rescheduling ${activeTimetables.size} active timetable alarms after $action")
                    activeTimetables.forEach { item ->
                        AlarmScheduler.scheduleAlarm(context, item)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling alarms: ${e.message}")
                }
            }
        }
    }
}
