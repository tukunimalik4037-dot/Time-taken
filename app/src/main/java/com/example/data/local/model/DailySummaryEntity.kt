package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val totalScreenTimeMinutes: Int,
    val studyMinutes: Int,
    val socialMinutes: Int,
    val shortVideoMinutes: Int,
    val otherMinutes: Int,
    val completedTasks: Int,
    val totalTasks: Int,
    val aiSummaryText: String,
    val productiveHour: String = "6 PM - 8 PM",
    val distractedHour: String = "9 PM - 11 PM",
    val completionRate: Float = 0f,
    val generatedAt: Long = System.currentTimeMillis()
)
