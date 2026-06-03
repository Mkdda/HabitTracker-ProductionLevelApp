package com.androidxcore.habittracker.fake

import com.androidxcore.habittracker.core.domain.error.DataError
import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.util.EmptyResult
import com.androidxcore.habittracker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeHabitLocalDataSource : HabitRepository {

    private val habits = MutableStateFlow<List<Habit>>(emptyList())
    private val entries = mutableMapOf<String, MutableList<Long>>()

    var shouldReturnError = false

    override fun observeHabits(): Flow<List<Habit>> = habits

    override suspend fun getById(id: String): Result<Habit, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val habit = habits.value.find { it.id == id }
        return if (habit != null) Result.Success(habit)
        else Result.Error(DataError.Local.NOT_FOUND)
    }

    override suspend fun upsertHabit(habit: Habit): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        habits.update { current ->
            val existing = current.indexOfFirst { it.id == habit.id }
            if (existing >= 0) current.toMutableList().also { it[existing] = habit }
            else current + habit
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteHabitById(id: String): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        habits.update { it.filter { h -> h.id != id } }
        entries.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun completeHabit(
        habitId: String,
        completedAt: Long
    ): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        entries.getOrPut(habitId) { mutableListOf() }.add(completedAt)
        return Result.Success(Unit)
    }

    override suspend fun isCompletedToday(
        habitId: String,
        startOfDay: Long,
        endOfDay: Long
    ): Result<Boolean, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val completed = entries[habitId]?.any { it in startOfDay..endOfDay } ?: false
        return Result.Success(completed)
    }

    override suspend fun getEntriesInRange(
        habitId: String,
        from: Long,
        to: Long
    ): List<Long> = entries[habitId]?.filter { it in from..to } ?: emptyList()

    fun addHabit(habit: Habit) {
        habits.update { it + habit }
    }

    fun addEntry(habitId: String, timestamp: Long) {
        entries.getOrPut(habitId) { mutableListOf() }.add(timestamp)
    }
}
