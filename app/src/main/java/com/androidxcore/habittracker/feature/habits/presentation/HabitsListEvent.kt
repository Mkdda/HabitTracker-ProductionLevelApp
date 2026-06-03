package com.androidxcore.habittracker.feature.habits.presentation

import com.androidxcore.habittracker.core.presentation.UiText

sealed interface HabitsListEvent {
    data class NavigateToDetail(val habitId: String) : HabitsListEvent
    data object NavigateToCreateHabit : HabitsListEvent
    data class ShowError(val message: UiText) : HabitsListEvent

}