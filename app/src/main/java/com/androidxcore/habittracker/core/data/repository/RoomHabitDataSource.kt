package com.androidxcore.habittracker.core.data.repository

import com.androidxcore.habittracker.core.data.database.dao.HabitDao
import com.androidxcore.habittracker.core.data.database.dao.HabitEntryDao
import com.androidxcore.habittracker.core.data.database.entity.HabitEntryEntity
import com.androidxcore.habittracker.core.data.database.entity.toEntity
import com.androidxcore.habittracker.core.data.database.entity.toHabit
import com.androidxcore.habittracker.core.domain.error.DataError
import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.util.EmptyResult
import com.androidxcore.habittracker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class RoomHabitDataSource @Inject constructor(
    private val habitDao: HabitDao,
    private val entryDao: HabitEntryDao
) : HabitRepository {

    override fun observeHabits(): Flow<List<Habit>> =
        habitDao.observeAll().map { entities -> entities.map { it.toHabit() } }

    override suspend fun getById(id: String): Result<Habit, DataError.Local> {
        return try {
            val entity = habitDao.getById(id)
            if (entity != null) Result.Success(entity.toHabit())
            else Result.Error(DataError.Local.NOT_FOUND)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun upsertHabit(habit: Habit): EmptyResult<DataError.Local> {
        return try {
            habitDao.upsert(habit.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun deleteHabitById(id: String): EmptyResult<DataError.Local> {
        return try {
            habitDao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun completeHabit(
        habitId: String,
        completedAt: Long
    ): EmptyResult<DataError.Local> {
        return try {
            entryDao.insert(
                HabitEntryEntity(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    completedAt = completedAt,
                    note = null
                )
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun isCompletedToday(
        habitId: String,
        startOfDay: Long,
        endOfDay: Long
    ): Result<Boolean, DataError.Local> {
        return try {
            val count = entryDao.countCompletionsToday(habitId, startOfDay, endOfDay)
            Result.Success(count > 0)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun getEntriesInRange(
        habitId: String,
        from: Long,
        to: Long
    ): List<Long> {
        return try {
            entryDao.getEntriesInRange(habitId, from, to).map { it.completedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
