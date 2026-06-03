package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.error.DataError
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.util.EmptyResult
import com.androidxcore.habittracker.core.domain.util.Error
import com.androidxcore.habittracker.core.domain.util.Result
import java.util.Calendar
import javax.inject.Inject

sealed interface CompleteHabitError : Error {
    data object AlreadyCompletedToday : CompleteHabitError
    data class Storage(val error: DataError.Local) : CompleteHabitError
}

class CompleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): EmptyResult<CompleteHabitError> {
        val (startOfDay, endOfDay) = getTodayRange()

        when (val check = repository.isCompletedToday(habitId, startOfDay, endOfDay)) {
            is Result.Success -> if (check.data) return Result.Error(CompleteHabitError.AlreadyCompletedToday)
            is Result.Error -> return Result.Error(CompleteHabitError.Storage(check.error))
        }

        return when (val result = repository.completeHabit(habitId, System.currentTimeMillis())) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(CompleteHabitError.Storage(result.error))
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return start to end
    }
}
