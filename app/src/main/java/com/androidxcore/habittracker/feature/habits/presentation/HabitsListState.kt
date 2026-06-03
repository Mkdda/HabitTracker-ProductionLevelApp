package com.androidxcore.habittracker.feature.habits.presentation

import com.androidxcore.habittracker.core.presentation.UiText
import com.androidxcore.habittracker.feature.habits.presentation.model.HabitUi

data class HabitsListState(
    val habits: List<HabitUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null
)