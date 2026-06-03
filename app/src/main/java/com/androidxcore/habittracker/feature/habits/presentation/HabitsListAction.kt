package com.androidxcore.habittracker.feature.habits.presentation

sealed interface HabitsListAction {
    data class OnHabitClick(val habitId: String) : HabitsListAction
    data class OnCompleteHabit(val habitId: String) : HabitsListAction
    data object OnAddHabitClick : HabitsListAction
    data object OnRetry : HabitsListAction
}