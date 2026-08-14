package com.example.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdminOverviewStats(
    val totalRegisteredUsers: Int = 14280,
    val activeUsers24h: Int = 3840,
    val totalTimetablesCreated: Long = 189204,
    val notificationsDelivered24h: Int = 11200,
    val notificationOpenRate: Float = 0.89f,
    val appVersion: String = "1.0.0 (Build 101)",
    val cloudFunctionsHealthy: Boolean = true,
    val securityRulesActive: Boolean = true,
    val privacyPolicyUrl: String = "https://timetrack.ai/privacy",
    val termsUrl: String = "https://timetrack.ai/terms",
    val announcementsList: List<AdminAnnouncement> = listOf(
        AdminAnnouncement("1", "v1.0 Release", "Real-time Timetable & Privacy-First Screen Activity Engine launched", "2026-08-10"),
        AdminAnnouncement("2", "AI Model Update", "Gemini 3.5 Flash powered intelligent summaries now active", "2026-08-12")
    ),
    val featureFlags: Map<String, Boolean> = mapOf(
        "enable_gemini_summaries" to true,
        "enable_cloud_firestore_sync" to true,
        "enable_short_video_estimation" to true,
        "enable_high_priority_alarms" to true,
        "enable_strict_privacy_sandboxing" to true
    )
)

data class AdminAnnouncement(
    val id: String,
    val title: String,
    val content: String,
    val date: String
)

class AdminRepository {
    private val _adminStats = MutableStateFlow(AdminOverviewStats())
    val adminStats: StateFlow<AdminOverviewStats> = _adminStats.asStateFlow()

    fun toggleFeatureFlag(key: String, enabled: Boolean) {
        val currentFlags = _adminStats.value.featureFlags.toMutableMap()
        currentFlags[key] = enabled
        _adminStats.value = _adminStats.value.copy(featureFlags = currentFlags)
    }

    fun broadcastAnnouncement(title: String, content: String): Boolean {
        if (title.isBlank() || content.isBlank()) return false
        val newAnnouncement = AdminAnnouncement(
            id = System.currentTimeMillis().toString(),
            title = title,
            content = content,
            date = "Today"
        )
        val updatedList = listOf(newAnnouncement) + _adminStats.value.announcementsList
        _adminStats.value = _adminStats.value.copy(
            announcementsList = updatedList,
            notificationsDelivered24h = _adminStats.value.notificationsDelivered24h + 1
        )
        return true
    }
}
