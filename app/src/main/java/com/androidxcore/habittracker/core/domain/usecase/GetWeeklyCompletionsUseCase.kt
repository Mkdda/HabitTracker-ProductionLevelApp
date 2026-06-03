package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.util.Result
import java.util.Calendar
import javax.inject.Inject

class GetWeeklyCompletionsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): List<Boolean> {
        val result = mutableListOf<Boolean>()
        val calendar = Calendar.getInstance()

        repeat(7) { daysAgo ->
            val target = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = target.timeInMillis
            val end = start + 86_400_000L - 1

            val completed = when (val r = repository.isCompletedToday(habitId, start, end)) {
                is Result.Success -> r.data
                is Result.Error -> false
            }
            result.add(completed)
        }

        return result.reversed()
    }
}
