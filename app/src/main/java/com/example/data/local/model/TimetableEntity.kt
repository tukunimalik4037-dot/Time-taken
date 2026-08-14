package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_entries")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // Format: YYYY-MM-DD
    val startTime: String, // Format: HH:mm (24-hour)
    val endTime: String, // Format: HH:mm (24-hour)
    val category: String = "Study", // Study, School, Gaming, Homework, Revision, Work, Fitness, Personal
    val repeatType: String = "DAILY", // ONCE, DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    val customDays: String = "1,2,3,4,5", // Comma separated day numbers (1=Mon..7=Sun)
    val reminderMinutes: Int = 10, // 0 = at start, 5, 10, 15, etc.
    val notificationEnabled: Boolean = true,
    val notificationSound: String = "Default",
    val vibrationEnabled: Boolean = true,
    val status: String = "PENDING", // PENDING, COMPLETED, SKIPPED, POSTPONED
    val colorHex: String = "#6366F1",
    val iconName: String = "Book",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
