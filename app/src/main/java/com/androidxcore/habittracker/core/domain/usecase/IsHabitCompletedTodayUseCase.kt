package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.util.Result
import java.util.Calendar
import javax.inject.Inject

class IsHabitCompletedTodayUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): Boolean {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return when (val result = repository.isCompletedToday(habitId, start, end)) {
            is Result.Success -> result.data
            is Result.Error -> false
        }
    }
}
