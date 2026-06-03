package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.error.DataError
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.repository.ReminderScheduler
import com.androidxcore.habittracker.core.domain.util.EmptyResult
import javax.inject.Inject

class DeleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(habitId: String): EmptyResult<DataError.Local> {
        reminderScheduler.cancel(habitId)
        return repository.deleteHabitById(habitId)
    }
}
