package com.androidxcore.habittracker.feature.habits.presentation.model

data class HabitDetailUi(
    val id: String,
    val name: String,
    val emoji: String,
    val frequencyLabel: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val streakUnit: String,
    val completionRate: Float,
    val isCompletedToday: Boolean,
    val reminderTime: String?
)
