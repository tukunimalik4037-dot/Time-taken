package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.model.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    fun getSummaryForDate(date: String): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    suspend fun getSummaryForDateDirect(date: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int = 7): Flow<List<DailySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailySummaryEntity)

    @Query("DELETE FROM daily_summaries")
    suspend fun deleteAllSummaries()
}
