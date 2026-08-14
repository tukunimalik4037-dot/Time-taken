package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timetableId: Long,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionTaken: String = "TRIGGERED" // TRIGGERED, COMPLETED, SNOOZED, SKIPPED, MISSED
)
