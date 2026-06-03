package com.androidxcore.habittracker.core.domain.usecase

import com.androidxcore.habittracker.core.domain.model.Habit
import com.androidxcore.habittracker.core.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<List<Habit>> = repository.observeHabits()
}
