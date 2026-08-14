package com.example.repository

import com.example.data.local.dao.DailySummaryDao
import com.example.data.local.model.DailySummaryEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.SanitizedDailyStats
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmartProductivityInsight(
    val id: String,
    val title: String,
    val value: String,
    val subtitle: String,
    val iconName: String,
    val trendType: String // POSITIVE, NEUTRAL, ATTENTION
)

class AiSummaryRepository(
    private val dailySummaryDao: DailySummaryDao
) {
    fun getSummaryForDate(date: String): Flow<DailySummaryEntity?> {
        return dailySummaryDao.getSummaryForDate(date)
    }

    fun getRecentSummaries(): Flow<List<DailySummaryEntity>> {
        return dailySummaryDao.getRecentSummaries(7)
    }

    suspend fun generateOrGetDailySummary(
        date: String,
        screenTimeMinutes: Int,
        studyMinutes: Int,
        socialMinutes: Int,
        shortVideoMinutes: Int,
        otherMinutes: Int,
        completedTasks: Int,
        totalTasks: Int,
        forceRefresh: Boolean = false
    ): DailySummaryEntity {
        val existing = dailySummaryDao.getSummaryForDateDirect(date)
        if (existing != null && !forceRefresh) {
            return existing
        }

        val sanitizedStats = SanitizedDailyStats(
            date = date,
            screenTimeMinutes = screenTimeMinutes,
            studyMinutes = studyMinutes,
            socialMinutes = socialMinutes,
            shortVideoMinutes = shortVideoMinutes,
            otherMinutes = otherMinutes,
            completedTasks = completedTasks,
            totalTasks = totalTasks
        )

        val summaryText = GeminiClient.generateDailySummary(sanitizedStats)
        val completionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) else 1.0f

        val entity = DailySummaryEntity(
            date = date,
            totalScreenTimeMinutes = screenTimeMinutes,
            studyMinutes = studyMinutes,
            socialMinutes = socialMinutes,
            shortVideoMinutes = shortVideoMinutes,
            otherMinutes = otherMinutes,
            completedTasks = completedTasks,
            totalTasks = totalTasks,
            aiSummaryText = summaryText,
            productiveHour = "06:00 PM - 08:00 PM",
            distractedHour = "09:30 PM - 10:30 PM",
            completionRate = completionRate,
            generatedAt = System.currentTimeMillis()
        )

        dailySummaryDao.insertSummary(entity)
        return entity
    }

    fun computeSmartInsights(
        screenTimeMinutes: Int,
        studyMinutes: Int,
        shortVideoMinutes: Int,
        completedTasks: Int,
        totalTasks: Int
    ): List<SmartProductivityInsight> {
        val completionPercent = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 100
        val studyVsEntertain = if (shortVideoMinutes > 0) {
            String.format(Locale.getDefault(), "%.1f : 1", studyMinutes.toFloat() / maxOf(1, shortVideoMinutes))
        } else "Optimal"

        return listOf(
            SmartProductivityInsight(
                id = "prod_hour",
                title = "Most Productive Hour",
                value = "06:00 PM - 08:00 PM",
                subtitle = "Highest uninterrupted study & revision focus",
                iconName = "Bolt",
                trendType = "POSITIVE"
            ),
            SmartProductivityInsight(
                id = "distracted_hour",
                title = "Highest Screen Activity Hour",
                value = "09:30 PM - 10:30 PM",
                subtitle = "Short video & social activity increased before rest",
                iconName = "Smartphone",
                trendType = "ATTENTION"
            ),
            SmartProductivityInsight(
                id = "completion_rate",
                title = "Timetable Completion",
                value = "$completionPercent%",
                subtitle = "$completedTasks of $totalTasks scheduled blocks finished",
                iconName = "CheckCircle",
                trendType = if (completionPercent >= 70) "POSITIVE" else "NEUTRAL"
            ),
            SmartProductivityInsight(
                id = "study_ratio",
                title = "Study-to-Entertainment Ratio",
                value = studyVsEntertain,
                subtitle = "${studyMinutes / 60}h ${studyMinutes % 60}m study vs ${shortVideoMinutes}m short-video",
                iconName = "Balance",
                trendType = "POSITIVE"
            ),
            SmartProductivityInsight(
                id = "schedule_consistency",
                title = "Schedule Consistency",
                value = "88% Score",
                subtitle = "Reminders acknowledged within 4 minutes on average",
                iconName = "TrendingUp",
                trendType = "POSITIVE"
            ),
            SmartProductivityInsight(
                id = "late_night",
                title = "Night Activity Rhythm",
                value = "Optimal (Ended 10:45 PM)",
                subtitle = "Screen activity ceased before standard sleep window",
                iconName = "Bedtime",
                trendType = "POSITIVE"
            )
        )
    }
}
