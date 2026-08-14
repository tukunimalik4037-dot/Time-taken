package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DailySummaryDao
import com.example.data.local.dao.NotificationLogDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.dao.UsageDao
import com.example.data.local.model.DailySummaryEntity
import com.example.data.local.model.NotificationLogEntity
import com.example.data.local.model.TimetableEntity
import com.example.data.local.model.UsageEntity

@Database(
    entities = [
        TimetableEntity::class,
        UsageEntity::class,
        DailySummaryEntity::class,
        NotificationLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TimeTrackDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun usageDao(): UsageDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        @Volatile
        private var INSTANCE: TimeTrackDatabase? = null

        fun getDatabase(context: Context): TimeTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeTrackDatabase::class.java,
                    "timetrack_ai_db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
