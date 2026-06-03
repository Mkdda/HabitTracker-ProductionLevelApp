package com.androidxcore.habittracker.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val useDarkTheme: Boolean = false,
    val showCompletedHabits: Boolean = true,
    val defaultReminderTime: String = "08:00",
    val hasCompletedOnboarding: Boolean = false
)