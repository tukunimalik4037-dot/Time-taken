package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.TimetableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_entries ORDER BY startTime ASC")
    fun getAllTimetables(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable_entries WHERE date = :date OR repeatType != 'ONCE' ORDER BY startTime ASC")
    fun getTimetablesForDate(date: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable_entries WHERE id = :id")
    fun getTimetableById(id: Long): Flow<TimetableEntity?>

    @Query("SELECT * FROM timetable_entries WHERE id = :id")
    suspend fun getTimetableByIdDirect(id: Long): TimetableEntity?

    @Query("SELECT * FROM timetable_entries WHERE notificationEnabled = 1")
    suspend fun getAllActiveReminders(): List<TimetableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(timetable: TimetableEntity): Long

    @Update
    suspend fun updateTimetable(timetable: TimetableEntity)

    @Query("UPDATE timetable_entries SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteTimetable(timetable: TimetableEntity)

    @Query("DELETE FROM timetable_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM timetable_entries")
    suspend fun deleteAll()
}
