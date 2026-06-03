package com.androidxcore.habittracker.feature.habits.presentation.detail

import com.androidxcore.habittracker.core.presentation.UiText
import com.androidxcore.habittracker.feature.habits.presentation.model.HabitDetailUi

data class HabitDetailState(
    val habit: HabitDetailUi? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val isCompleting: Boolean = false,
    val showDeleteDialog: Boolean = false
)
