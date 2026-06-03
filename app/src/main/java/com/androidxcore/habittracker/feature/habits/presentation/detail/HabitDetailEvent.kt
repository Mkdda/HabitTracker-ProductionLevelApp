package com.androidxcore.habittracker.feature.habits.presentation.detail

import com.androidxcore.habittracker.core.presentation.UiText

sealed interface HabitDetailEvent {
    data object NavigateBack : HabitDetailEvent
    data class ShowError(val message: UiText) : HabitDetailEvent
}