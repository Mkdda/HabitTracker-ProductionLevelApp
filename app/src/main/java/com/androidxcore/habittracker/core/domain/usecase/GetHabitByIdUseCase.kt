package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.error.DataError
import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import com.androidxcore.habittracker.core.domain.util.Result
import javax.inject.Inject

class GetHabitByIdUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(id: String): Result<Habit, DataError.Local> =
        repository.getById(id)
}
