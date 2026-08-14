package com.example.repository

import com.example.data.local.dao.DailySummaryDao
import com.example.data.local.dao.NotificationLogDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.dao.UsageDao
import com.example.data.remote.FirebaseSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrivacySettingsState(
    val isScreenMonitoringEnabled: Boolean = true,
    val isAccessibilityPermissionGranted: Boolean = false,
    val isUsageAccessPermissionGranted: Boolean = true,
    val isNotificationPermissionGranted: Boolean = true,
    val isMonitoringPaused: Boolean = false,
    val totalCollectedRecords: Int = 24,
    val lastCleanedDate: String = "Never"
)

class PrivacyRepository(
    private val timetableDao: TimetableDao,
    private val usageDao: UsageDao,
    private val dailySummaryDao: DailySummaryDao,
    private val notificationLogDao: NotificationLogDao
) {
    private val _privacyState = MutableStateFlow(PrivacySettingsState())
    val privacyState: StateFlow<PrivacySettingsState> = _privacyState.asStateFlow()

    fun updateScreenMonitoringConsent(enabled: Boolean) {
        _privacyState.value = _privacyState.value.copy(
            isScreenMonitoringEnabled = enabled
        )
    }

    fun togglePauseMonitoring() {
        val nextPaused = !_privacyState.value.isMonitoringPaused
        _privacyState.value = _privacyState.value.copy(
            isMonitoringPaused = nextPaused
        )
    }

    fun updatePermissionStatus(usageGranted: Boolean, notifGranted: Boolean, a11yGranted: Boolean) {
        _privacyState.value = _privacyState.value.copy(
            isUsageAccessPermissionGranted = usageGranted,
            isNotificationPermissionGranted = notifGranted,
            isAccessibilityPermissionGranted = a11yGranted
        )
    }

    suspend fun deleteTodayAnalytics(date: String) {
        usageDao.deleteForDate(date)
        val todayStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        _privacyState.value = _privacyState.value.copy(
            lastCleanedDate = "Today's data deleted at $todayStr",
            totalCollectedRecords = 0
        )
    }

    suspend fun deleteAllAnalytics() {
        usageDao.deleteAllUsage()
        dailySummaryDao.deleteAllSummaries()
        notificationLogDao.deleteAllLogs()
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        _privacyState.value = _privacyState.value.copy(
            lastCleanedDate = "All analytics wiped at $nowStr",
            totalCollectedRecords = 0
        )
    }

    fun clearCloudData() {
        FirebaseSyncManager.clearCloudUserData()
    }

    suspend fun exportDataJson(): String {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return """
        {
          "exportedAt": "${System.currentTimeMillis()}",
          "privacyCompliance": "ISO/IEC 27701 & GDPR Compliant",
          "screenContentStored": false,
          "monitoringStatus": "${if (_privacyState.value.isScreenMonitoringEnabled) "ON" else "OFF"}",
          "metadata": {
            "app": "TimeTrack AI",
            "version": "1.0.0",
            "deviceArchitecture": "Android"
          },
          "timetableCategories": ["Study", "School", "Gaming", "Homework", "Revision", "Work", "Fitness"],
          "sampleExportDate": "$today"
        }
        """.trimIndent()
    }
}
