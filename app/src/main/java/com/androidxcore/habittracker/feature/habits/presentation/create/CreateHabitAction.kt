package com.androidxcore.habittracker.feature.habits.presentation.create

sealed interface CreateHabitAction {
    data class OnNameChange(val name: String) : CreateHabitAction
    data class OnEmojiChange(val emoji: String) : CreateHabitAction
    data class OnFrequencyChange(val frequency: Int) : CreateHabitAction
    data class OnReminderTimeChange(val time: String) : CreateHabitAction
    data object OnReminderToggle : CreateHabitAction
    data object OnSaveClick : CreateHabitAction
    data object OnBackClick : CreateHabitAction
}