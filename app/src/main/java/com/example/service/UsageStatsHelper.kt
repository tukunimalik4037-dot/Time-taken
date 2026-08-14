package com.example.service

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.example.data.local.model.UsageEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object UsageStatsHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTodayUsageStats(context: Context): List<UsageEntity> {
        val calendar = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        if (!hasUsageStatsPermission(context)) {
            // Return baseline sample items for new/unpermissioned launch if permitted
            return emptyList()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val packageManager = context.packageManager
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyList()

        val results = mutableListOf<UsageEntity>()

        for (stat in statsList) {
            val totalTimeInForeground = stat.totalTimeInForeground
            val durationMinutes = (totalTimeInForeground / (1000 * 60)).toInt()

            if (durationMinutes > 0) {
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    stat.packageName.substringAfterLast(".")
                }

                val category = categorizeApp(stat.packageName, appName)
                val isShortVideoApp = isShortVideoPlatform(stat.packageName, appName)
                val estimatedShortMins = if (isShortVideoApp) (durationMinutes * 0.65).toInt() else 0
                val estimatedShortSessions = if (isShortVideoApp) maxOf(1, durationMinutes / 4) else 0

                results.add(
                    UsageEntity(
                        packageName = stat.packageName,
                        appName = appName,
                        startTime = stat.firstTimeStamp,
                        endTime = stat.lastTimeStamp,
                        durationMinutes = durationMinutes,
                        date = todayStr,
                        category = category,
                        scrollInteractions = if (isShortVideoApp) durationMinutes * 18 else durationMinutes * 4,
                        estimatedShortVideoMinutes = estimatedShortMins,
                        estimatedShortSessions = estimatedShortSessions,
                        source = "USAGE_STATS_API",
                        confidence = if (isShortVideoApp) "MEDIUM" else "HIGH"
                    )
                )
            }
        }

        return results.sortedByDescending { it.durationMinutes }
    }

    fun categorizeApp(packageName: String, appName: String): String {
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()

        return when {
            lowerPkg.contains("duolingo") || lowerPkg.contains("khan") || lowerPkg.contains("coursera") ||
            lowerPkg.contains("quizlet") || lowerPkg.contains("classroom") || lowerPkg.contains("anki") ||
            lowerName.contains("study") || lowerName.contains("learn") || lowerName.contains("notes") ||
            lowerName.contains("dictionary") || lowerName.contains("calculator") -> "STUDY"

            lowerPkg.contains("instagram") || lowerPkg.contains("tiktok") || lowerPkg.contains("youtube") ||
            lowerPkg.contains("snapchat") || lowerPkg.contains("reddit") || lowerPkg.contains("twitter") ||
            lowerPkg.contains("threads") || lowerPkg.contains("facebook") -> "SOCIAL"

            lowerPkg.contains("game") || lowerPkg.contains("pubg") || lowerPkg.contains("roblox") ||
            lowerPkg.contains("minecraft") || lowerPkg.contains("steam") || lowerPkg.contains("supercell") -> "ENTERTAINMENT"

            lowerPkg.contains("chrome") || lowerPkg.contains("drive") || lowerPkg.contains("docs") ||
            lowerPkg.contains("sheets") || lowerPkg.contains("notion") || lowerPkg.contains("slack") ||
            lowerPkg.contains("teams") || lowerPkg.contains("zoom") || lowerPkg.contains("gmail") -> "PRODUCTIVITY"

            else -> "OTHER"
        }
    }

    fun isShortVideoPlatform(packageName: String, appName: String): Boolean {
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()
        return lowerPkg.contains("tiktok") ||
               lowerPkg.contains("instagram") ||
               lowerPkg.contains("youtube") ||
               lowerPkg.contains("snapchat") ||
               lowerName.contains("reels") ||
               lowerName.contains("shorts")
    }
}
