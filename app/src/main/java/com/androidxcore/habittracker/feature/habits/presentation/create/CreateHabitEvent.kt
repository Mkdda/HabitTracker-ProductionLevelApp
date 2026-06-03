package com.androidxcore.habittracker.feature.habits.presentation.create

sealed interface CreateHabitEvent {
    data object NavigateBack : CreateHabitEvent
    data object HabitCreated : CreateHabitEvent
}