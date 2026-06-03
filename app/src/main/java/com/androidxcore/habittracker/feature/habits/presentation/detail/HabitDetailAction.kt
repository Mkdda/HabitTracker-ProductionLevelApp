package com.androidxcore.habittracker.feature.habits.presentation.detail

sealed interface HabitDetailAction {
    data object OnBackClick : HabitDetailAction
    data object OnCompleteClick : HabitDetailAction
    data object OnDeleteClick : HabitDetailAction
    data object OnConfirmDelete : HabitDetailAction
    data object OnDismissDelete : HabitDetailAction
    data object OnRetry : HabitDetailAction
}
