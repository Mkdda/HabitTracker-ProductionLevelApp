package com.androidxcore.habittracker.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidxcore.habittracker.core.data.database.entity.HabitEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEntryDao {

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY completedAt DESC")
    fun observeByHabit(habitId: String): Flow<List<HabitEntryEntity>>

    @Query("""
        SELECT * FROM habit_entries 
        WHERE habitId = :habitId 
        AND completedAt BETWEEN :from AND :to
        ORDER BY completedAt DESC
    """)
    suspend fun getEntriesInRange(habitId: String, from: Long, to: Long): List<HabitEntryEntity>

    @Query("""
        SELECT COUNT(*) FROM habit_entries 
        WHERE habitId = :habitId 
        AND completedAt BETWEEN :startOfDay AND :endOfDay
    """)
    suspend fun countCompletionsToday(habitId: String, startOfDay: Long, endOfDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HabitEntryEntity)

    @Delete
    suspend fun delete(entry: HabitEntryEntity)
}