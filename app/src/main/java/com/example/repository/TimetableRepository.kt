package com.example.repository

import android.content.Context
import com.example.data.local.dao.NotificationLogDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.model.NotificationLogEntity
import com.example.data.local.model.TimetableEntity
import com.example.service.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimetableRepository(
    private val timetableDao: TimetableDao,
    private val notificationLogDao: NotificationLogDao,
    private val context: Context
) {
    val allTimetables: Flow<List<TimetableEntity>> = timetableDao.getAllTimetables()
    val notificationLogs: Flow<List<NotificationLogEntity>> = notificationLogDao.getRecentLogs(30)

    fun getTimetablesForDate(date: String): Flow<List<TimetableEntity>> {
        return timetableDao.getTimetablesForDate(date)
    }

    fun getTimetableById(id: Long): Flow<TimetableEntity?> {
        return timetableDao.getTimetableById(id)
    }

    suspend fun insertTimetable(item: TimetableEntity): Long {
        val id = timetableDao.insertTimetable(item)
        val createdItem = item.copy(id = id)
        if (createdItem.notificationEnabled) {
            AlarmScheduler.scheduleAlarm(context, createdItem)
        }
        return id
    }

    suspend fun updateTimetable(item: TimetableEntity) {
        timetableDao.updateTimetable(item)
        if (item.notificationEnabled) {
            AlarmScheduler.scheduleAlarm(context, item)
        } else {
            AlarmScheduler.cancelAlarm(context, item.id)
        }
    }

    suspend fun updateStatus(id: Long, status: String) {
        timetableDao.updateStatus(id, status)
        val item = timetableDao.getTimetableByIdDirect(id)
        if (item != null) {
            notificationLogDao.insertLog(
                NotificationLogEntity(
                    timetableId = id,
                    title = item.title,
                    message = "Status changed to $status",
                    actionTaken = status
                )
            )
        }
    }

    suspend fun deleteTimetable(item: TimetableEntity) {
        AlarmScheduler.cancelAlarm(context, item.id)
        timetableDao.deleteTimetable(item)
    }

    suspend fun populateDefaultsIfEmpty() {
        val existing = timetableDao.getAllActiveReminders()
        if (existing.isEmpty()) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val sampleItems = listOf(
                TimetableEntity(
                    title = "Morning Study Block",
                    description = "Advanced Algorithms & Data Structures practice",
                    date = todayStr,
                    startTime = "06:00",
                    endTime = "07:30",
                    category = "Study",
                    repeatType = "WEEKDAYS",
                    reminderMinutes = 10,
                    notificationEnabled = true,
                    status = "COMPLETED",
                    colorHex = "#6366F1",
                    iconName = "Book"
                ),
                TimetableEntity(
                    title = "School / Lecture Session",
                    description = "Software Engineering lecture & lab work",
                    date = todayStr,
                    startTime = "08:00",
                    endTime = "14:00",
                    category = "School",
                    repeatType = "WEEKDAYS",
                    reminderMinutes = 15,
                    notificationEnabled = true,
                    status = "COMPLETED",
                    colorHex = "#3B82F6",
                    iconName = "School"
                ),
                TimetableEntity(
                    title = "Recreational Gaming & Break",
                    description = "Unwind and play favorite multiplayer game",
                    date = todayStr,
                    startTime = "16:00",
                    endTime = "17:30",
                    category = "Gaming",
                    repeatType = "DAILY",
                    reminderMinutes = 5,
                    notificationEnabled = true,
                    status = "COMPLETED",
                    colorHex = "#EC4899",
                    iconName = "Gamepad"
                ),
                TimetableEntity(
                    title = "Homework & Assignments",
                    description = "Mobile App Architecture assignment submission",
                    date = todayStr,
                    startTime = "19:00",
                    endTime = "20:30",
                    category = "Homework",
                    repeatType = "WEEKDAYS",
                    reminderMinutes = 10,
                    notificationEnabled = true,
                    status = "PENDING",
                    colorHex = "#F59E0B",
                    iconName = "Assignment"
                ),
                TimetableEntity(
                    title = "Mathematics & Physics Revision",
                    description = "Review key formulas and problem sets",
                    date = todayStr,
                    startTime = "21:00",
                    endTime = "22:00",
                    category = "Revision",
                    repeatType = "DAILY",
                    reminderMinutes = 10,
                    notificationEnabled = true,
                    status = "PENDING",
                    colorHex = "#8B5CF6",
                    iconName = "Quiz"
                )
            )

            sampleItems.forEach {
                val id = timetableDao.insertTimetable(it)
                AlarmScheduler.scheduleAlarm(context, it.copy(id = id))
            }
        }
    }
}
