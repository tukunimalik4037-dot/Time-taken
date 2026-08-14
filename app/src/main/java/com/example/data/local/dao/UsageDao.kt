package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.model.UsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Query("SELECT * FROM screen_usage_records WHERE date = :date ORDER BY durationMinutes DESC")
    fun getUsageForDate(date: String): Flow<List<UsageEntity>>

    @Query("SELECT * FROM screen_usage_records ORDER BY endTime DESC")
    fun getAllUsage(): Flow<List<UsageEntity>>

    @Query("SELECT * FROM screen_usage_records WHERE startTime >= :start AND endTime <= :end ORDER BY durationMinutes DESC")
    fun getUsageBetween(start: Long, end: Long): Flow<List<UsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: UsageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageBatch(usages: List<UsageEntity>)

    @Query("DELETE FROM screen_usage_records WHERE date = :date")
    suspend fun deleteForDate(date: String)

    @Query("DELETE FROM screen_usage_records")
    suspend fun deleteAllUsage()
}
