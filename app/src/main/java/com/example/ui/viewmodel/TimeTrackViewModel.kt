package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TimeTrackApp
import com.example.data.local.model.DailySummaryEntity
import com.example.data.local.model.NotificationLogEntity
import com.example.data.local.model.TimetableEntity
import com.example.data.remote.CloudSyncStatus
import com.example.data.remote.FirebaseSyncManager
import com.example.repository.AdminOverviewStats
import com.example.repository.AggregatedUsageStats
import com.example.repository.PrivacySettingsState
import com.example.repository.ShortVideoMetrics
import com.example.repository.SmartProductivityInsight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ActiveActivityState(
    val currentActivity: TimetableEntity? = null,
    val nextActivity: TimetableEntity? = null,
    val isCurrentlyActive: Boolean = false,
    val formattedTimeRemaining: String = ""
)

class TimeTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TimeTrackApp
    private val timetableRepo = app.timetableRepository
    private val analyticsRepo = app.analyticsRepository
    private val privacyRepo = app.privacyRepository
    private val aiSummaryRepo = app.aiSummaryRepository
    private val adminRepo = app.adminRepository

    val timetables: StateFlow<List<TimetableEntity>> = timetableRepo.allTimetables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = timetableRepo.notificationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privacyState: StateFlow<PrivacySettingsState> = privacyRepo.privacyState
    val cloudSyncStatus: StateFlow<CloudSyncStatus> = FirebaseSyncManager.syncStatus
    val adminStats: StateFlow<AdminOverviewStats> = adminRepo.adminStats

    private val _aggregatedStats = MutableStateFlow(
        AggregatedUsageStats(
            totalScreenTimeMinutes = 258,
            studyMinutes = 130,
            socialMinutes = 84,
            entertainmentMinutes = 35,
            productivityMinutes = 40,
            otherMinutes = 9,
            topApps = emptyList(),
            hourlyActivity = List(24) { 0 },
            shortVideoMetrics = ShortVideoMetrics(
                estimatedSessions = 11,
                estimatedScrollInteractions = 840,
                totalTimeMinutes = 38,
                averageSessionMinutes = 3,
                longestSessionMinutes = 14,
                sessionsPerDay = 11,
                peakHour = "09:00 PM - 10:00 PM",
                trend = "Slightly increased after 9 PM",
                confidence = "MEDIUM (Estimated from activity signals)"
            )
        )
    )
    val aggregatedStats: StateFlow<AggregatedUsageStats> = _aggregatedStats.asStateFlow()

    private val _aiSummary = MutableStateFlow<DailySummaryEntity?>(null)
    val aiSummary: StateFlow<DailySummaryEntity?> = _aiSummary.asStateFlow()

    private val _isGeneratingSummary = MutableStateFlow(false)
    val isGeneratingSummary: StateFlow<Boolean> = _isGeneratingSummary.asStateFlow()

    private val _smartInsights = MutableStateFlow<List<SmartProductivityInsight>>(emptyList())
    val smartInsights: StateFlow<List<SmartProductivityInsight>> = _smartInsights.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Real-time active / next activity flow
    val activeActivityState: StateFlow<ActiveActivityState> = timetables.combine(_toastMessage) { list, _ ->
        computeActiveActivity(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveActivityState())

    // Progress percentage
    val todayProgress: StateFlow<Int> = timetables.combine(_toastMessage) { list, _ ->
        if (list.isEmpty()) 0
        else {
            val completed = list.count { it.status == "COMPLETED" }
            (completed * 100) / list.size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 72)

    init {
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            val stats = analyticsRepo.refreshUsageFromSystem()
            _aggregatedStats.value = stats

            val completed = timetables.value.count { it.status == "COMPLETED" }
            val total = maxOf(1, timetables.value.size)

            _smartInsights.value = aiSummaryRepo.computeSmartInsights(
                screenTimeMinutes = stats.totalScreenTimeMinutes,
                studyMinutes = stats.studyMinutes,
                shortVideoMinutes = stats.shortVideoMetrics.totalTimeMinutes,
                completedTasks = completed,
                totalTasks = total
            )

            // Auto-load or generate summary for today
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val summary = aiSummaryRepo.generateOrGetDailySummary(
                date = todayStr,
                screenTimeMinutes = stats.totalScreenTimeMinutes,
                studyMinutes = stats.studyMinutes,
                socialMinutes = stats.socialMinutes,
                shortVideoMinutes = stats.shortVideoMetrics.totalTimeMinutes,
                otherMinutes = stats.otherMinutes,
                completedTasks = completed,
                totalTasks = total,
                forceRefresh = false
            )
            _aiSummary.value = summary
        }
    }

    fun generateAiSummary(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            _isGeneratingSummary.value = true
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val stats = _aggregatedStats.value
            val completed = timetables.value.count { it.status == "COMPLETED" }
            val total = maxOf(1, timetables.value.size)

            val summary = aiSummaryRepo.generateOrGetDailySummary(
                date = todayStr,
                screenTimeMinutes = stats.totalScreenTimeMinutes,
                studyMinutes = stats.studyMinutes,
                socialMinutes = stats.socialMinutes,
                shortVideoMinutes = stats.shortVideoMetrics.totalTimeMinutes,
                otherMinutes = stats.otherMinutes,
                completedTasks = completed,
                totalTasks = total,
                forceRefresh = forceRefresh
            )
            _aiSummary.value = summary
            _isGeneratingSummary.value = false
            showToast("AI Summary updated with latest data")
        }
    }

    fun saveTimetable(timetable: TimetableEntity) {
        viewModelScope.launch {
            if (timetable.id == 0L) {
                timetableRepo.insertTimetable(timetable)
                showToast("Activity scheduled: ${timetable.title}")
            } else {
                timetableRepo.updateTimetable(timetable)
                showToast("Updated: ${timetable.title}")
            }
        }
    }

    fun markComplete(id: Long) {
        viewModelScope.launch {
            timetableRepo.updateStatus(id, "COMPLETED")
            showToast("Activity marked completed")
            refreshAnalytics()
        }
    }

    fun snoozeTask(id: Long) {
        viewModelScope.launch {
            timetableRepo.updateStatus(id, "POSTPONED")
            showToast("Activity snoozed for 10 minutes")
        }
    }

    fun skipTask(id: Long) {
        viewModelScope.launch {
            timetableRepo.updateStatus(id, "SKIPPED")
            showToast("Activity skipped")
        }
    }

    fun deleteTimetable(timetable: TimetableEntity) {
        viewModelScope.launch {
            timetableRepo.deleteTimetable(timetable)
            showToast("Deleted ${timetable.title}")
        }
    }

    fun toggleScreenMonitoring(enabled: Boolean) {
        privacyRepo.updateScreenMonitoringConsent(enabled)
        analyticsRepo.setMonitoringActive(enabled)
        showToast(if (enabled) "Screen monitoring enabled" else "Screen monitoring disabled")
    }

    fun togglePauseMonitoring() {
        privacyRepo.togglePauseMonitoring()
        val paused = privacyState.value.isMonitoringPaused
        analyticsRepo.setMonitoringPaused(paused)
        showToast(if (paused) "Monitoring paused" else "Monitoring resumed")
    }

    fun deleteTodayAnalytics() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            privacyRepo.deleteTodayAnalytics(todayStr)
            refreshAnalytics()
            showToast("Today's analytics deleted")
        }
    }

    fun deleteAllAnalytics() {
        viewModelScope.launch {
            privacyRepo.deleteAllAnalytics()
            refreshAnalytics()
            showToast("All historical analytics wiped")
        }
    }

    fun clearCloudData() {
        privacyRepo.clearCloudData()
        showToast("Cloud user data wiped")
    }

    fun exportData(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = privacyRepo.exportDataJson()
            onExportReady(json)
            showToast("Data exported successfully")
        }
    }

    fun triggerCloudSync() {
        FirebaseSyncManager.triggerManualSync(app)
        showToast("Cloud sync completed")
    }

    fun broadcastAnnouncement(title: String, content: String) {
        val ok = adminRepo.broadcastAnnouncement(title, content)
        if (ok) showToast("Push campaign broadcasted")
    }

    fun toggleFeatureFlag(key: String, enabled: Boolean) {
        adminRepo.toggleFeatureFlag(key, enabled)
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    private fun computeActiveActivity(list: List<TimetableEntity>): ActiveActivityState {
        val nowCal = Calendar.getInstance()
        val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

        var active: TimetableEntity? = null
        var next: TimetableEntity? = null
        var minDiffToNext = Int.MAX_VALUE

        for (item in list) {
            val startParts = item.startTime.split(":")
            val endParts = item.endTime.split(":")
            val startMins = (startParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (startParts.getOrNull(1)?.toIntOrNull() ?: 0)
            val endMins = (endParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (endParts.getOrNull(1)?.toIntOrNull() ?: 0)

            if (currentMinutes in startMins until endMins && item.status != "COMPLETED") {
                active = item
            } else if (startMins > currentMinutes && item.status != "COMPLETED") {
                val diff = startMins - currentMinutes
                if (diff < minDiffToNext) {
                    minDiffToNext = diff
                    next = item
                }
            }
        }

        if (active == null && next == null && list.isNotEmpty()) {
            next = list.firstOrNull { it.status != "COMPLETED" } ?: list.first()
        }

        return ActiveActivityState(
            currentActivity = active,
            nextActivity = next ?: active,
            isCurrentlyActive = (active != null),
            formattedTimeRemaining = if (active != null) "In Progress" else if (next != null) "Starts at ${next.startTime}" else ""
        )
    }
}
