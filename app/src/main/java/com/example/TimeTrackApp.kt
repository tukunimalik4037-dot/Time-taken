package com.example

import android.app.Application
import com.example.data.local.TimeTrackDatabase
import com.example.repository.AdminRepository
import com.example.repository.AiSummaryRepository
import com.example.repository.AnalyticsRepository
import com.example.repository.PrivacyRepository
import com.example.repository.TimetableRepository
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeTrackApp : Application() {

    lateinit var database: TimeTrackDatabase
        private set

    lateinit var timetableRepository: TimetableRepository
        private set

    lateinit var analyticsRepository: AnalyticsRepository
        private set

    lateinit var privacyRepository: PrivacyRepository
        private set

    lateinit var aiSummaryRepository: AiSummaryRepository
        private set

    lateinit var adminRepository: AdminRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize notification channels
        NotificationHelper.createNotificationChannels(this)

        // 2. Initialize Database & Repositories
        database = TimeTrackDatabase.getDatabase(this)

        timetableRepository = TimetableRepository(
            timetableDao = database.timetableDao(),
            notificationLogDao = database.notificationLogDao(),
            context = this
        )

        analyticsRepository = AnalyticsRepository(
            usageDao = database.usageDao(),
            context = this
        )

        privacyRepository = PrivacyRepository(
            timetableDao = database.timetableDao(),
            usageDao = database.usageDao(),
            dailySummaryDao = database.dailySummaryDao(),
            notificationLogDao = database.notificationLogDao()
        )

        aiSummaryRepository = AiSummaryRepository(
            dailySummaryDao = database.dailySummaryDao()
        )

        adminRepository = AdminRepository()

        // 3. Seed realistic timetable defaults if newly installed
        CoroutineScope(Dispatchers.IO).launch {
            timetableRepository.populateDefaultsIfEmpty()
        }
    }

    companion object {
        lateinit var instance: TimeTrackApp
            private set
    }
}
