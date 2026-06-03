package com.androidxcore.habittracker.core.domain.repository

import com.androidxcore.habittracker.core.domain.error.DataError
import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.util.EmptyResult
import com.androidxcore.habittracker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    suspend fun getById(id: String): Result<Habit, DataError.Local>
    suspend fun upsertHabit(habit: Habit): EmptyResult<DataError.Local>
    suspend fun deleteHabitById(id: String): EmptyResult<DataError.Local>
    suspend fun completeHabit(habitId: String, completedAt: Long): EmptyResult<DataError.Local>
    suspend fun isCompletedToday(habitId: String, startOfDay: Long, endOfDay: Long): Result<Boolean, DataError.Local>
    suspend fun getEntriesInRange(habitId: String, from: Long, to: Long): List<Long>
}
