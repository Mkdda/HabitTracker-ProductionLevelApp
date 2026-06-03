package com.androidxcore.habittracker.feature.habits.presentation.create

import com.androidxcore.habittracker.core.presentation.UiText

data class CreateHabitState(
    val name: String = "",
    val emoji: String = "🎯",
    val frequencyPerWeek: Int = 7,
    val reminderTime: String? = null,
    val isReminderEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: UiText? = null
)