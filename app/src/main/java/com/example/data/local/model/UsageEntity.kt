package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_usage_records")
data class UsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val date: String, // Format: YYYY-MM-DD
    val category: String = "OTHER", // STUDY, SOCIAL, ENTERTAINMENT, SHORT_VIDEO, PRODUCTIVITY, UTILITY, OTHER
    val scrollInteractions: Int = 0,
    val estimatedShortVideoMinutes: Int = 0,
    val estimatedShortSessions: Int = 0,
    val source: String = "USAGE_STATS_API", // USAGE_STATS_API, ACCESSIBILITY_SIGNAL, ESTIMATED_SIGNAL
    val confidence: String = "HIGH" // HIGH, MEDIUM, LOW
)
