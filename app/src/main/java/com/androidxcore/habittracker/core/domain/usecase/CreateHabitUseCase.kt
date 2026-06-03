package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.error.DataError
import com.androidxcore.habittracker.core.domain.error.HabitError
import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.repository.ReminderScheduler
import com.androidxcore.habittracker.core.domain.util.EmptyResult
import com.androidxcore.habittracker.core.domain.util.Error
import com.androidxcore.habittracker.core.domain.util.Result
import java.util.UUID
import javax.inject.Inject

sealed interface CreateHabitError : Error {
    data class Validation(val error: HabitError) : CreateHabitError
    data class Storage(val error: DataError.Local) : CreateHabitError
}

class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(
        name: String,
        emoji: String,
        frequencyPerWeek: Int,
        reminderTime: String?
    ): EmptyResult<CreateHabitError> {

        if (name.isBlank()) {
            return Result.Error(CreateHabitError.Validation(HabitError.EMPTY_NAME))
        }
        if (name.length > 50) {
            return Result.Error(CreateHabitError.Validation(HabitError.NAME_TOO_LONG))
        }
        if (frequencyPerWeek !in 1..7) {
            return Result.Error(CreateHabitError.Validation(HabitError.INVALID_FREQUENCY))
        }

        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            emoji = emoji,
            frequencyPerWeek = frequencyPerWeek,
            reminderTime = reminderTime,
            createdAt = System.currentTimeMillis()
        )

        return when (val result = repository.upsertHabit(habit)) {
            is Result.Success -> {
                if (reminderTime != null) {
                    runCatching {
                        val parts = reminderTime.split(":")
                        val hour = parts[0].toInt()
                        val minute = parts[1].toInt()
                        reminderScheduler.schedule(habit.id, habit.name, hour, minute)
                    }
                }
                Result.Success(Unit)
            }
            is Result.Error -> Result.Error(CreateHabitError.Storage(result.error))
        }
    }
}
